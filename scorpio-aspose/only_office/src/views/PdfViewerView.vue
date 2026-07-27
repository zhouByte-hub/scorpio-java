<template>
  <div class="pdf-viewer-view">
    <div class="viewer-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/')">返回</el-button>
      <span class="doc-title">{{ docTitle }}</span>
    </div>

    <div class="viewer-body">
      <PdfPreview
        :source="pdfUrl"
        @loaded="handleLoaded"
        @error="handleError"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PdfPreview from '@/components/PdfPreview.vue'
import { getDocumentUrl } from '@/api/document'

const route = useRoute()
const docId = computed(() => route.params.id as string)

const pdfUrl = ref('')
const docTitle = ref('未命名PDF')

onMounted(() => {
  pdfUrl.value = getDocumentUrl(docId.value)
  docTitle.value = `PDF文档-${docId.value}.pdf`
})

function handleLoaded(totalPages: number) {
  ElMessage.success(`PDF加载完成，共 ${totalPages} 页`)
}

function handleError(message: string) {
  ElMessage.error(message)
}
</script>

<style scoped>
.pdf-viewer-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.viewer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.doc-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.viewer-body {
  flex: 1;
  overflow: hidden;
}
</style>
