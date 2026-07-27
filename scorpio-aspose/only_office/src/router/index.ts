import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '文档管理' },
  },
  {
    path: '/editor/:id',
    name: 'Editor',
    component: () => import('@/views/EditorView.vue'),
    meta: { title: '文档编辑' },
  },
  {
    path: '/pdf-viewer/:id',
    name: 'PdfViewer',
    component: () => import('@/views/PdfViewerView.vue'),
    meta: { title: 'PDF预览' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 在线文档编辑器`
  }
})

export default router
