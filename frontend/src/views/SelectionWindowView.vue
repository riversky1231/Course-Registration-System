<template>
  <div>
    <PageHeader :title="meta.label" :caption="meta.caption" :description="meta.description">
      <template #actions>
        <el-input
          v-model.trim="mod.keyword.value"
          class="header-search"
          :placeholder="meta.searchPlaceholder"
          prefix-icon="Search"
          clearable
          @keyup.enter="mod.search"
          @clear="mod.search"
        />
        <el-button type="primary" :icon="'Plus'" @click="mod.openCreate">新增时间窗口</el-button>
      </template>
    </PageHeader>

    <div v-loading="mod.loading.value">
      <el-empty v-if="!mod.rows.value.length" description="尚未配置选退课时间窗口" />

      <div v-else class="window-grid">
        <article v-for="win in mod.rows.value" :key="win.id" class="window-card" :class="{ 'is-drop': win.actionType === 'DROP' }">
          <div class="window-top">
            <div class="window-type">
              <el-icon><component :is="win.actionType === 'DROP' ? 'RemoveFilled' : 'CirclePlusFilled'" /></el-icon>
              <span>{{ win.actionType === "DROP" ? "退课窗口" : "选课窗口" }}</span>
            </div>
            <el-tag :type="statusOf(win).type" effect="dark" round size="small">{{ statusOf(win).label }}</el-tag>
          </div>

          <h3>{{ win.name || "未命名窗口" }}</h3>

          <div class="window-time">
            <div class="time-point">
              <span>开始</span>
              <strong>{{ formatDateTime(win.startTime) }}</strong>
            </div>
            <div class="time-arrow"><el-icon><Right /></el-icon></div>
            <div class="time-point">
              <span>结束</span>
              <strong>{{ formatDateTime(win.endTime) }}</strong>
            </div>
          </div>

          <p class="window-desc">{{ win.description || "无窗口说明。" }}</p>

          <div class="window-actions">
            <el-button :icon="'Edit'" size="small" @click="mod.openEdit(win)">编辑</el-button>
            <el-button type="danger" plain size="small" :icon="'Delete'" @click="mod.remove(win, '删除')">删除</el-button>
          </div>
        </article>
      </div>
    </div>

    <RecordDialog
      v-model="mod.dialog.visible"
      view="selectionWindows"
      :mode="mod.dialog.mode"
      :form="mod.dialog.form"
      :loading="mod.loading.value"
      @save="mod.save"
    />
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import PageHeader from "@/components/PageHeader.vue";
import RecordDialog from "@/components/RecordDialog.vue";
import { useModule } from "@/composables/useModule";
import { VIEW_META } from "@/constants/modules";
import { formatDateTime } from "@/utils/format";

const meta = VIEW_META.selectionWindows;

const mod = useModule("selectionWindows", {
  emptyForm: () => ({ actionType: "SELECT", name: "", startTime: "", endTime: "", enabled: true, description: "" }),
  normalize: (form) => ({ ...form, enabled: Boolean(form.enabled) }),
});

function statusOf(win) {
  if (!win.enabled) return { label: "已停用", type: "info" };
  if (win.active) return { label: "生效中", type: "success" };
  return { label: "未开放", type: "warning" };
}

onMounted(mod.load);
</script>

<style scoped>
.header-search { width: 280px; max-width: 50vw; }

.window-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 18px;
}

.window-card {
  position: relative;
  padding: 20px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  border-top: 4px solid var(--brand);
}

.window-card.is-drop { border-top-color: #d97706; }

.window-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.window-type { display: inline-flex; align-items: center; gap: 8px; font-weight: 600; color: var(--brand); }
.is-drop .window-type { color: #d97706; }
.window-type .el-icon { font-size: 20px; }

.window-card h3 { margin: 0 0 16px; font-size: 18px; }

.window-time {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: var(--bg-2);
  margin-bottom: 14px;
}

.time-point { flex: 1; }
.time-point span { display: block; font-size: 11px; color: var(--ink-faint); margin-bottom: 2px; }
.time-point strong { font-size: 13px; }
.time-arrow { color: var(--brand); font-size: 18px; }

.window-desc { margin: 0 0 16px; font-size: 13px; color: var(--ink-soft); line-height: 1.6; }
.window-actions { display: flex; gap: 8px; }
</style>
