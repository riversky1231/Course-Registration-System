import { reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { listModule, createModule, updateModule, removeModule } from "@/api";
import { ENDPOINTS } from "@/constants/modules";
import { clonePlain, emptyPage } from "@/utils/format";

// 模块通用列表 + 分页 + 搜索 + 增删改逻辑，供各业务页面复用。
export function useModule(viewKey, options = {}) {
  const endpoint = ENDPOINTS[viewKey];
  const rows = ref([]);
  const loading = ref(false);
  const keyword = ref("");
  const pager = reactive({ page: 1, pageSize: 10, total: 0, totalPages: 0 });

  const dialog = reactive({
    visible: false,
    mode: "create",
    form: {},
    feedback: { type: "info", text: "" },
  });

  async function load() {
    if (!endpoint) return;
    loading.value = true;
    try {
      const params = {
        keyword: keyword.value,
        page: pager.page,
        pageSize: pager.pageSize,
        ...(options.extraParams ? options.extraParams() : {}),
      };
      const data = await listModule(endpoint, params);
      rows.value = data?.items || [];
      pager.total = data?.total || 0;
      pager.totalPages = data?.totalPages || 0;
      pager.page = data?.page || pager.page;
      pager.pageSize = data?.pageSize || pager.pageSize;
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      loading.value = false;
    }
  }

  function search() {
    pager.page = 1;
    return load();
  }

  function handlePageChange(page) {
    pager.page = page;
    return load();
  }

  function handleSizeChange(size) {
    pager.pageSize = size;
    pager.page = 1;
    return load();
  }

  function openCreate() {
    dialog.mode = "create";
    dialog.form = options.emptyForm ? options.emptyForm() : {};
    dialog.feedback = { type: "info", text: "" };
    dialog.visible = true;
  }

  function openEdit(row) {
    dialog.mode = "edit";
    dialog.form = options.prepareForm ? options.prepareForm(clonePlain(row)) : clonePlain(row);
    dialog.feedback = { type: "info", text: "" };
    dialog.visible = true;
  }

  function closeDialog() {
    dialog.visible = false;
    dialog.form = {};
    dialog.feedback = { type: "info", text: "" };
  }

  async function save() {
    const payload = options.normalize
      ? options.normalize(dialog.form, dialog.mode)
      : clonePlain(dialog.form);
    loading.value = true;
    try {
      if (dialog.mode === "create") {
        await createModule(endpoint, payload);
      } else {
        await updateModule(endpoint, dialog.form.id, payload);
      }
      await load();
      if (options.onSaved) await options.onSaved();
      if (options.keepDialogOpenOnSave) {
        dialog.feedback = { type: "success", text: options.savedMessage || "保存成功" };
      } else {
        closeDialog();
      }
      ElMessage.success("保存成功");
    } catch (error) {
      if (options.keepDialogOpenOnSave) {
        dialog.feedback = { type: "error", text: error.message };
      }
      ElMessage.error(error.message);
    } finally {
      loading.value = false;
    }
  }

  async function remove(row, actionLabel = "删除") {
    try {
      await ElMessageBox.confirm(
        `确认${actionLabel}当前记录吗？此操作不可撤销。`,
        `${actionLabel}确认`,
        { type: "warning", confirmButtonText: actionLabel, cancelButtonText: "取消" }
      );
    } catch (error) {
      return;
    }
    loading.value = true;
    try {
      await removeModule(endpoint, row.id);
      await load();
      if (options.onSaved) await options.onSaved();
      ElMessage.success(`${actionLabel}成功`);
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      loading.value = false;
    }
  }

  return {
    endpoint,
    rows,
    loading,
    keyword,
    pager,
    dialog,
    load,
    search,
    handlePageChange,
    handleSizeChange,
    openCreate,
    openEdit,
    closeDialog,
    save,
    remove,
  };
}

export { emptyPage };
