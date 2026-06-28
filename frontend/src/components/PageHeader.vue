<template>
  <header class="page-header">
    <div class="page-header-meta">
      <span class="ph-accent"></span>
      <div class="ph-text">
        <p class="eyebrow">{{ caption }}</p>
        <h1>{{ title }}</h1>
        <p v-if="description" class="page-header-desc">{{ description }}</p>
      </div>
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
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
  position: relative;
}

.page-header::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 72px;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--brand), var(--brand-3));
}

.page-header-meta {
  display: flex;
  align-items: stretch;
  gap: 14px;
}

.ph-accent {
  width: 5px;
  flex: none;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--brand), var(--brand-3));
  box-shadow: 0 6px 16px rgba(91, 91, 240, 0.28);
}

.page-header-meta h1 {
  margin: 6px 0 6px;
  font-size: 24px;
  color: var(--ink);
  font-weight: 800;
}

.page-header-desc {
  margin: 0;
  color: var(--ink-soft);
  font-size: 13px;
  max-width: 620px;
  line-height: 1.6;
}

.page-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
