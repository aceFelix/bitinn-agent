<!--
  文章卡片骨架屏
  数据加载中的占位动画，支持自定义骨架颜色
  @author aceFelix
-->
<script setup>
defineProps({
  rgbColor: { type: String, default: '249, 115, 22' }
})
</script>

<template>
  <div class="skeleton-card" :style="{ '--sk-rgb': rgbColor }">
    <div class="skeleton-header">
      <div class="skeleton-avatar"></div>
      <div class="skeleton-meta">
        <div class="skeleton-line short"></div>
        <div class="skeleton-line tiny"></div>
      </div>
    </div>
    <div class="skeleton-body">
      <div class="skeleton-text-block">
        <div class="skeleton-line long"></div>
        <div class="skeleton-line medium"></div>
        <div class="skeleton-line medium"></div>
      </div>
      <div class="skeleton-cover"></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-card {
  border-radius: 12px;
  padding: 20px;

  .skeleton-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .skeleton-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(90deg, #{'rgba(var(--sk-rgb), 0.06)'} 0%, #{'rgba(var(--sk-rgb), 0.14)'} 20%, #{'rgba(var(--sk-rgb), 0.06)'} 40%, #{'rgba(var(--sk-rgb), 0.03)'} 100%);
    background-size: 200% 100%;
    animation: shimmer 1.8s ease-in-out infinite;
    border: 1px solid #{'rgba(var(--sk-rgb), 0.08)'};
  }

  .skeleton-meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .skeleton-body {
    display: flex;
    gap: 20px;
  }

  .skeleton-text-block {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .skeleton-cover {
    width: 200px;
    height: 120px;
    border-radius: 8px;
    flex-shrink: 0;
    background: linear-gradient(90deg, #{'rgba(var(--sk-rgb), 0.04)'} 0%, #{'rgba(var(--sk-rgb), 0.1)'} 25%, #{'rgba(var(--sk-rgb), 0.04)'} 50%, #{'rgba(var(--sk-rgb), 0.03)'} 100%);
    background-size: 200% 100%;
    animation: shimmer 1.8s ease-in-out infinite;
  }

  .skeleton-line {
    height: 14px;
    border-radius: 6px;
    background: linear-gradient(90deg, #{'rgba(var(--sk-rgb), 0.06)'} 0%, #{'rgba(var(--sk-rgb), 0.14)'} 25%, #{'rgba(var(--sk-rgb), 0.06)'} 50%, #{'rgba(var(--sk-rgb), 0.03)'} 75%, #{'rgba(var(--sk-rgb), 0.06)'} 100%);
    background-size: 200% 100%;
    animation: shimmer 1.8s ease-in-out infinite;

    &.short { width: 80px; height: 13px; }
    &.tiny { width: 50px; height: 11px; }
    &.long { width: 85%; height: 20px; }
    &.medium { width: 70%; }
  }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@media (max-width: 768px) {
  .skeleton-body {
    flex-direction: column;
  }

  .skeleton-cover {
    width: 100%;
    height: 140px;
  }
}
</style>
