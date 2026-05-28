/**
 * 应用入口文件
 * 初始化 Vue 应用，注册 Element Plus、Pinia、Vue Router 等核心插件
 * @author aceFelix
 */
import { createApp } from 'vue'
import App from './App.vue'

// Element Plus 按需导入（只引入用到的组件，大幅减少包体积）
import {
  ElMessage,
  ElMessageBox,
  ElLoading,
  ElDrawer,
  ElDialog,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElInput,
  ElButton,
  ElForm,
  ElFormItem,
  ElTag,
  ElAvatar,
  ElCard,
  ElTabs,
  ElTabPane,
  ElPagination,
  ElPopover,
  ElTooltip,
  ElBadge,
  ElScrollbar,
} from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import router from '@/router'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-persistedstate-plugin'
import '@/styles/colors.css'

const pinia = createPinia()
pinia.use(createPersistedState())

const app = createApp(App)

app.use(pinia)
app.use(router)
app.mount('#app')

// 主动暴露 ElMessage 等到 window，方便全局调用
window.ElMessage = ElMessage
window.ElMessageBox = ElMessageBox
window.ElLoading = ElLoading
window.ElDrawer = ElDrawer
window.ElDialog = ElDialog
