<!--
  消息弹窗气泡
  点击导航栏消息图标时弹出，展示各类消息的未读数量
  @author aceFelix
-->
<script setup>
defineProps({
  visible: Boolean
})

const emit = defineEmits(['navigate', 'close'])

const tabs = [
  { id: 'like', label: '点赞', icon: 'like', count: 12 },
  { id: 'favorite', label: '收藏', icon: 'favorite', count: 5 },
  { id: 'comment', label: '评论', icon: 'comment', count: 8 },
  { id: 'dm', label: '私信', icon: 'dm', count: 3 },
  { id: 'follower', label: '新增粉丝', icon: 'follower', count: 2 },
  { id: 'system', label: '系统通知', icon: 'system', count: 1 }
]

const handleClick = (tabId) => {
  emit('navigate', tabId)
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="popover-card"
      @mouseenter="$emit('popoverEnter')"
      @mouseleave="$emit('popoverLeave')"
    >
      <div class="popover-header">
        <h3 class="popover-title">通知</h3>
        <span class="popover-total">共 31 条未读</span>
      </div>
      <div class="popover-list">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          class="popover-item"
          @click="handleClick(tab.id)"
        >
          <div class="popover-item-left">
            <div class="popover-item-icon" :class="tab.id">
              <svg v-if="tab.icon === 'like'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
              </svg>
              <svg v-else-if="tab.icon === 'favorite'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              <svg v-else-if="tab.icon === 'comment'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <svg v-else-if="tab.icon === 'dm'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
              <svg v-else-if="tab.icon === 'follower'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="8.5" cy="7" r="4"/>
                <line x1="20" y1="8" x2="20" y2="14"/>
                <line x1="23" y1="11" x2="17" y2="11"/>
              </svg>
              <svg v-else-if="tab.icon === 'system'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
            </div>
            <span class="popover-item-label">{{ tab.label }}</span>
          </div>
          <div class="popover-item-right">
            <span v-if="tab.count > 0" class="popover-item-count">{{ tab.count > 99 ? '99+' : tab.count }}</span>
            <svg class="popover-item-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
        </button>
      </div>
      <div class="popover-footer">
        <button class="popover-view-all" @click="handleClick('all')">
          查看全部通知
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="scss">
.popover-card {
  position: fixed;
  top: 70px;
  right: calc((100vw - 1400px) / 2 + 100px);
  width: 340px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  z-index: 9998;
  animation: popoverSlideIn 0.2s ease-out;
}

@keyframes popoverSlideIn {
  from {
    opacity: 0;
    transform: translateY(-8px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.popover-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f0f0f0;

  .popover-title {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0;
  }

  .popover-total {
    font-size: 12px;
    color: #999;
  }
}

.popover-list {
  display: flex;
  flex-direction: column;
  padding: 6px 0;
}

.popover-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
  text-align: left;

  &:hover {
    background: rgba(249, 115, 22, 0.04);
    padding-left: 24px;
  }
}

.popover-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.popover-item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 18px;
    height: 18px;
  }

  &.like { background: rgba(255, 71, 87, 0.1); color: #FF4757; }
  &.favorite { background: rgba(255, 184, 0, 0.1); color: #FFB800; }
  &.comment { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
  &.dm { background: rgba(16, 185, 129, 0.1); color: #10B981; }
  &.follower { background: rgba(139, 92, 246, 0.1); color: #8B5CF6; }
  &.system { background: rgba(249, 115, 22, 0.1); color: #F97316; }
}

.popover-item-label {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.popover-item-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.popover-item-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: #F97316;
  border-radius: 10px;
  color: white;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.popover-item-arrow {
  width: 16px;
  height: 16px;
  stroke: #ccc;
  transition: all 0.2s;

  .popover-item:hover & {
    stroke: #F97316;
  }
}

.popover-footer {
  border-top: 1px solid #f0f0f0;
  padding: 10px 20px;

  .popover-view-all {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 8px 0;
    border: none;
    border-radius: 6px;
    background: transparent;
    font-size: 13px;
    font-weight: 500;
    color: #F97316;
    cursor: pointer;
    transition: all 0.2s;

    svg {
      width: 14px;
      height: 14px;
      transition: transform 0.2s;
    }

    &:hover {
      background: rgba(249, 115, 22, 0.06);

      svg {
        transform: translateX(2px);
      }
    }
  }
}

@media (max-width: 992px) {
  .popover-card {
    right: 16px;
  }
}
</style>
