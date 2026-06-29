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
        <el-button :icon="'Search'" @click="mod.search">搜索</el-button>
        <el-button type="primary" :icon="'Plus'" @click="mod.openCreate">新增管理员</el-button>
      </template>
    </PageHeader>

    <el-card class="panel" shadow="never" v-loading="mod.loading.value">
      <el-empty v-if="!mod.rows.value.length" description="暂无管理员账号" />
      <template v-else>
        <el-table :data="mod.rows.value" class="data-table" stripe>
          <el-table-column label="管理员" min-width="200">
            <template #default="{ row }">
              <div class="admin-cell">
                <el-avatar :size="36" class="admin-avatar">{{ initials(row.name || row.username) }}</el-avatar>
                <div>
                  <strong>{{ row.name || "未命名" }}</strong>
                  <span>{{ row.username }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="tele" label="联系电话" min-width="160">
            <template #default="{ row }">{{ safeText(row.tele) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="160" align="center">
            <template #default="{ row }">
              <el-button type="primary" link :icon="'Edit'" @click="mod.openEdit(row)">编辑</el-button>
              <el-button type="danger" link :icon="'Delete'" @click="mod.remove(row, '删除')">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager" v-if="mod.pager.total > 0">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="mod.pager.total"
            :current-page="mod.pager.page"
            :page-size="mod.pager.pageSize"
            :page-sizes="pageSizeOptions"
            @current-change="mod.handlePageChange"
            @size-change="mod.handleSizeChange"
          />
        </div>
      </template>
    </el-card>

    <RecordDialog
      v-model="mod.dialog.visible"
      view="admins"
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
import { VIEW_META, PAGE_SIZE_OPTIONS } from "@/constants/modules";
import { safeText, initials } from "@/utils/format";

const meta = VIEW_META.admins;
const pageSizeOptions = PAGE_SIZE_OPTIONS;

const mod = useModule("admins", {
  emptyForm: () => ({ username: "", password: "", name: "", tele: "" }),
});

onMounted(mod.load);
</script>

<style scoped>
.header-search { width: 280px; max-width: 50vw; }
.admin-cell { display: flex; align-items: center; gap: 12px; }
.admin-avatar { background: linear-gradient(140deg, var(--brand), var(--brand-3)); color: #fff; font-weight: 700; flex: none; }
.admin-cell strong { display: block; font-size: 14px; }
.admin-cell span { font-size: 12px; color: var(--ink-soft); }
</style>
