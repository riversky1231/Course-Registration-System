<template>
  <header class="page-header">
    <div class="page-header-meta">
      <p class="eyebrow">{{ caption }}</p>
      <h1>{{ title }}</h1>
      <p v-if="description" class="page-header-desc">{{ description }}</p>
    </div>
    <div class="page-header-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, default: "" },
  caption: { type: String, default: "" },
  description: { type: String, default: "" },
});
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.page-header-meta h1 {
  margin: 6px 0 6px;
  font-size: 24px;
  color: var(--ink);
}

.page-header-desc {
  margin: 0;
  color: var(--ink-soft);
  font-size: 13px;
  max-width: 620px;
}

.page-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
