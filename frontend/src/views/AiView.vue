<template>
  <div>
    <PageHeader :title="meta.label" :caption="meta.caption" :description="meta.description">
      <template #actions>
        <el-tag v-if="!aiConfigured" type="warning" effect="light" round>演示模式（未配置密钥）</el-tag>
        <el-tag v-else type="success" effect="light" round>AI 已就绪</el-tag>
      </template>
    </PageHeader>

    <el-tabs v-model="activeTab" class="ai-tabs">
      <!-- 选课助手对话 -->
      <el-tab-pane label="选课助手" name="chat">
        <div class="chat-shell panel">
          <div ref="scrollEl" class="chat-stream">
            <div v-for="(msg, i) in messages" :key="i" class="chat-row" :class="msg.role">
              <div class="chat-avatar" :class="msg.role">
                <el-icon><component :is="msg.role === 'user' ? 'User' : 'MagicStick'" /></el-icon>
              </div>
              <div v-if="msg.role === 'user'" class="chat-bubble">{{ msg.content }}</div>
              <div v-else class="chat-bubble md" v-html="renderAssistant(msg, i)"></div>
            </div>
            <div v-if="chatLoading && !streaming" class="chat-row assistant">
              <div class="chat-avatar assistant"><el-icon><MagicStick /></el-icon></div>
              <div class="chat-bubble typing">正在思考…</div>
            </div>
          </div>

          <div class="chat-suggest">
            <el-tag
              v-for="s in suggestions"
              :key="s"
              class="suggest-chip"
              effect="plain"
              round
              @click="useSuggestion(s)"
            >{{ s }}</el-tag>
          </div>

          <div class="chat-input">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="问问选课相关的问题，例如：我大二适合选哪些专业课？"
              @keyup.enter.exact.prevent="send"
            />
            <el-button type="primary" :icon="'Promotion'" :loading="chatLoading" @click="send">发送</el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 智能课程推荐（仅学生） -->
      <el-tab-pane v-if="role === 'student'" label="智能推荐" name="recommend">
        <el-card class="panel" shadow="never">
          <div class="rec-head">
            <div>
              <p class="eyebrow">本学期推荐</p>
              <h3>一键生成推荐课表</h3>
              <p class="rec-desc">综合你的年级、已修课程、GPA 与学分上限，从可选课程中规划本学期课表。</p>
            </div>
            <el-button type="primary" :icon="'MagicStick'" :loading="recLoading" @click="loadRecommend">
              {{ recommendation ? "重新生成" : "生成推荐" }}
            </el-button>
          </div>

          <el-empty v-if="!recommendation && !recLoading" description="点击「生成推荐」获取本学期课表建议" :image-size="100" />

          <div v-if="recommendation" class="rec-body">
            <el-alert :type="recommendation.configured ? 'success' : 'warning'" :closable="false" show-icon :title="recommendation.summary" />
            <div class="rec-meta">
              <span>推荐课程 <strong>{{ recommendation.items.length }}</strong> 门</span>
              <span>合计学分 <strong>{{ formatNumber(recommendation.totalCredits) }}</strong></span>
            </div>
            <div class="rec-grid">
              <article v-for="(item, idx) in recommendation.items" :key="item.courseId || idx" class="rec-card">
                <div class="rec-card-top">
                  <span class="rec-index">{{ idx + 1 }}</span>
                  <el-tag size="small" effect="light" round>{{ item.type || "课程" }}</el-tag>
                  <span class="rec-credit">{{ formatNumber(item.credit) }} 学分</span>
                </div>
                <h4>{{ item.courseName }}</h4>
                <p>{{ item.reason }}</p>
              </article>
            </div>
            <el-alert v-if="recommendation.note" type="info" :closable="false" :title="recommendation.note" />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 课程简介/大纲生成（仅教师/管理员） -->
      <el-tab-pane v-if="role !== 'student'" label="课程大纲生成" name="syllabus">
        <div class="syllabus-layout">
          <el-card class="panel" shadow="never">
            <template #header><div class="panel-head"><div><p class="eyebrow">输入</p><h3>生成参数</h3></div></div></template>
            <el-form label-position="top" @submit.prevent="loadSyllabus">
              <el-form-item label="课程名称">
                <el-input v-model.trim="syllabusForm.courseName" placeholder="如：分布式系统" />
              </el-form-item>
              <el-form-item label="关键词（可选）">
                <el-input v-model.trim="syllabusForm.keywords" type="textarea" :rows="3" placeholder="如：一致性、容错、分布式事务、实战" />
              </el-form-item>
              <el-button type="primary" class="full-width" :icon="'MagicStick'" :loading="syllabusLoading" @click="loadSyllabus">生成简介与大纲</el-button>
            </el-form>
          </el-card>

          <el-card class="panel" shadow="never">
            <template #header>
              <div class="panel-head">
                <div><p class="eyebrow">输出</p><h3>生成结果</h3></div>
                <el-button v-if="syllabus" size="small" :icon="'CopyDocument'" @click="copyAll">复制全部</el-button>
              </div>
            </template>
            <el-empty v-if="!syllabus && !syllabusLoading" description="填写左侧参数后生成课程简介与教学大纲" :image-size="90" />
            <div v-if="syllabus" class="syllabus-result">
              <div class="syllabus-block">
                <div class="block-head"><strong>课程简介</strong><el-button text size="small" :icon="'CopyDocument'" @click="copyText(syllabus.jianjie)">复制</el-button></div>
                <p class="syllabus-intro">{{ syllabus.jianjie }}</p>
              </div>
              <div class="syllabus-block">
                <strong>教学大纲</strong>
                <ol class="syllabus-outline">
                  <li v-for="(line, i) in syllabus.outline" :key="i">{{ line }}</li>
                </ol>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import PageHeader from "@/components/PageHeader.vue";
import { aiApi, streamAiChat } from "@/api";
import { useAuthStore } from "@/stores/auth";
import { VIEW_META } from "@/constants/modules";
import { formatNumber } from "@/utils/format";
import { renderMarkdown } from "@/utils/markdown";

const auth = useAuthStore();
const meta = VIEW_META.assistant;
const role = computed(() => auth.role);

const activeTab = ref("chat");
const aiConfigured = ref(true);

// ===== 对话 =====
const scrollEl = ref(null);
const draft = ref("");
const chatLoading = ref(false);
const streaming = ref(false);
const streamingIndex = ref(-1);
const messages = reactive([
  { role: "assistant", content: `你好，${auth.displayName}！我是你的智能选课助手，可以根据你的选课数据给出建议。有什么想问的？` },
]);

const suggestions = computed(() => {
  if (role.value === "student") {
    return ["我这学期适合选哪些课？", "帮我分析一下选课冲突", "还差哪些先修课程？"];
  }
  if (role.value === "teacher") {
    return ["如何安排课程时间更合理？", "我的课程容量建议设多少？"];
  }
  return ["当前选课整体情况如何？", "有哪些课程还没排教师？"];
});

function useSuggestion(text) {
  draft.value = text;
  send();
}

// 助手气泡：Markdown 渲染；流式进行中的那条额外追加闪烁光标。
function renderAssistant(msg, index) {
  const html = renderMarkdown(msg.content);
  if (streaming.value && index === streamingIndex.value) {
    return `${html}<span class="stream-caret">▋</span>`;
  }
  return html;
}

async function scrollToBottom() {
  await nextTick();
  if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight;
}

async function send() {
  const question = draft.value.trim();
  if (!question || chatLoading.value) return;
  const history = messages.slice().map((m) => ({ role: m.role, content: m.content }));
  messages.push({ role: "user", content: question });
  draft.value = "";
  chatLoading.value = true;
  streaming.value = false;
  streamingIndex.value = -1;
  await scrollToBottom();

  try {
    const { configured } = await streamAiChat(
      { question, history },
      {
        onDelta: (token) => {
          if (!streaming.value) {
            streaming.value = true;
            messages.push({ role: "assistant", content: "" });
            streamingIndex.value = messages.length - 1;
          }
          messages[streamingIndex.value].content += token;
          scrollToBottom();
        },
      }
    );
    aiConfigured.value = configured;
    if (streamingIndex.value < 0) {
      messages.push({ role: "assistant", content: "（未收到内容，请重试）" });
    }
  } catch (error) {
    if (streamingIndex.value >= 0) {
      messages[streamingIndex.value].content += `\n\n（中断：${error.message}）`;
    } else {
      messages.push({ role: "assistant", content: `抱歉，出错了：${error.message}` });
    }
  } finally {
    chatLoading.value = false;
    streaming.value = false;
    streamingIndex.value = -1;
    await scrollToBottom();
  }
}

// ===== 推荐 =====
const recLoading = ref(false);
const recommendation = ref(null);

async function loadRecommend() {
  recLoading.value = true;
  try {
    const data = await aiApi.recommend();
    aiConfigured.value = data.configured;
    recommendation.value = data;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    recLoading.value = false;
  }
}

// ===== 大纲 =====
const syllabusLoading = ref(false);
const syllabus = ref(null);
const syllabusForm = reactive({ courseName: "", keywords: "" });

async function loadSyllabus() {
  if (!syllabusForm.courseName.trim()) return ElMessage.warning("请填写课程名称");
  syllabusLoading.value = true;
  try {
    const data = await aiApi.syllabus({ ...syllabusForm });
    aiConfigured.value = data.configured;
    syllabus.value = data;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    syllabusLoading.value = false;
  }
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text || "");
    ElMessage.success("已复制");
  } catch (error) {
    ElMessage.error("复制失败，请手动选择");
  }
}

function copyAll() {
  if (!syllabus.value) return;
  const text = `课程简介：\n${syllabus.value.jianjie}\n\n教学大纲：\n${(syllabus.value.outline || [])
    .map((line, i) => `${i + 1}. ${line}`)
    .join("\n")}`;
  copyText(text);
}

onMounted(scrollToBottom);
</script>

<style scoped>
.ai-tabs :deep(.el-tabs__item) { font-size: 15px; }

/* 对话 */
.chat-shell { display: flex; flex-direction: column; padding: 18px; height: calc(100vh - 250px); min-height: 460px; }
.chat-stream { flex: 1; overflow-y: auto; padding: 6px 6px 12px; display: flex; flex-direction: column; gap: 16px; }

.chat-row { display: flex; gap: 10px; max-width: 78%; }
.chat-row.user { align-self: flex-end; flex-direction: row-reverse; }

.chat-avatar {
  width: 36px;
  height: 36px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 11px;
  font-size: 18px;
  color: #fff;
}
.chat-avatar.assistant { background: linear-gradient(140deg, var(--brand), var(--brand-3)); }
.chat-avatar.user { background: linear-gradient(140deg, #2563eb, #38bdf8); }

.chat-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  background: var(--bg-2);
  border: 1px solid var(--line);
}
.chat-row.user .chat-bubble { background: linear-gradient(135deg, var(--brand), var(--brand-2)); color: #fff; border: none; }
.chat-bubble.typing { color: var(--ink-faint); }

.stream-caret {
  display: inline-block;
  margin-left: 1px;
  color: var(--brand);
  font-weight: 700;
  animation: caret-blink 1s steps(1) infinite;
}
@keyframes caret-blink {
  0%, 50% { opacity: 1; }
  50.01%, 100% { opacity: 0; }
}

/* 助手气泡内的 Markdown 排版 */
.chat-bubble.md { white-space: normal; }
.chat-bubble.md :deep(p) { margin: 0 0 8px; }
.chat-bubble.md :deep(p:last-child) { margin-bottom: 0; }
.chat-bubble.md :deep(ul),
.chat-bubble.md :deep(ol) { margin: 6px 0 8px; padding-left: 20px; }
.chat-bubble.md :deep(li) { margin: 3px 0; }
.chat-bubble.md :deep(li > p) { margin: 0; }
.chat-bubble.md :deep(strong) { color: var(--ink); font-weight: 700; }
.chat-bubble.md :deep(h1),
.chat-bubble.md :deep(h2),
.chat-bubble.md :deep(h3),
.chat-bubble.md :deep(h4) { margin: 10px 0 6px; font-size: 15px; line-height: 1.4; }
.chat-bubble.md :deep(code) {
  font-family: "JetBrains Mono", ui-monospace, "Courier New", monospace;
  font-size: 12.5px;
  padding: 1px 6px;
  border-radius: 6px;
  background: rgba(99, 102, 241, 0.12);
  color: var(--brand-3, #4338ca);
}
.chat-bubble.md :deep(pre) {
  margin: 8px 0;
  padding: 12px 14px;
  border-radius: 12px;
  background: #0f172a;
  color: #e2e8f0;
  overflow-x: auto;
}
.chat-bubble.md :deep(pre code) { background: none; color: inherit; padding: 0; }
.chat-bubble.md :deep(a) { color: var(--brand); text-decoration: underline; }
.chat-bubble.md :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 3px solid var(--brand);
  color: var(--ink-soft);
  background: var(--bg-2);
  border-radius: 0 8px 8px 0;
}
.chat-bubble.md :deep(table) { border-collapse: collapse; margin: 8px 0; font-size: 13px; }
.chat-bubble.md :deep(th),
.chat-bubble.md :deep(td) { border: 1px solid var(--line); padding: 6px 10px; }

.chat-suggest { display: flex; gap: 8px; flex-wrap: wrap; padding: 12px 4px; }
.suggest-chip { cursor: pointer; }
.suggest-chip:hover { color: var(--brand); border-color: var(--brand); }

.chat-input { display: flex; gap: 10px; align-items: flex-end; }
.chat-input .el-textarea { flex: 1; }

/* 推荐 */
.rec-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; }
.rec-head h3 { margin: 6px 0; }
.rec-desc { margin: 0; color: var(--ink-soft); font-size: 13px; max-width: 560px; }
.rec-body { display: grid; gap: 16px; }
.rec-meta { display: flex; gap: 22px; font-size: 14px; color: var(--ink-soft); }
.rec-meta strong { color: var(--brand); font-size: 18px; }

.rec-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
.rec-card { padding: 16px; border-radius: 16px; background: var(--bg-2); border: 1px solid var(--line); }
.rec-card-top { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.rec-index { width: 24px; height: 24px; display: grid; place-items: center; border-radius: 8px; background: var(--brand); color: #fff; font-size: 13px; font-weight: 700; }
.rec-credit { margin-left: auto; font-size: 12px; color: var(--ink-soft); }
.rec-card h4 { margin: 0 0 8px; font-size: 16px; }
.rec-card p { margin: 0; font-size: 13px; color: var(--ink-soft); line-height: 1.6; }

/* 大纲 */
.syllabus-layout { display: grid; grid-template-columns: 380px 1fr; gap: 20px; align-items: start; }
.syllabus-result { display: grid; gap: 18px; }
.syllabus-block .block-head { display: flex; align-items: center; justify-content: space-between; }
.syllabus-block strong { font-size: 15px; }
.syllabus-intro { margin: 8px 0 0; font-size: 14px; line-height: 1.8; color: var(--ink-soft); padding: 14px; background: var(--bg-2); border-radius: 12px; }
.syllabus-outline { margin: 10px 0 0; padding-left: 22px; display: grid; gap: 8px; }
.syllabus-outline li { font-size: 14px; line-height: 1.6; color: var(--ink-soft); }

@media (max-width: 900px) {
  .syllabus-layout { grid-template-columns: 1fr; }
  .chat-row { max-width: 90%; }
}
</style>
