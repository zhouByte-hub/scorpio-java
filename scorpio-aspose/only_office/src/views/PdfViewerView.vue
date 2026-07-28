<template>
  <div class="pdf-viewer-view">
    <div class="viewer-header">
      <div class="left">
        <el-button :icon="ArrowLeft" @click="router.push({ name: 'Home' })">返回</el-button>
        <h3>{{ documentInfo?.name || 'PDF 预览' }}</h3>
      </div>
      <div class="right">
        <el-button :disabled="!documentInfo" @click="handleDownload">下载</el-button>
      </div>
    </div>

    <div v-loading="loading" class="viewer-body">
      <PdfPreview
        v-if="pdfUrl"
        :source="pdfUrl"
        @loaded="handleLoaded"
        @error="handleError"
      />
      <el-empty v-else-if="!loading" description="无法加载 PDF" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PdfPreview from '@/components/PdfPreview.vue'
import { fetchDocumentDetail, getDocumentUrl } from '@/api/document'
import type { DocumentInfo } from '@/types'

const route = useRoute()
const router = useRouter()

const fileId = computed(() => String(route.params.id || ''))
const documentInfo = ref<DocumentInfo | null>(null)
const loading = ref(true)
const pdfUrl = ref('')

onMounted(async () => {
  if (!fileId.value) {
    ElMessage.error('文档 ID 无效')
    loading.value = false
    return
  }
  try {
    documentInfo.value = await fetchDocumentDetail(fileId.value)
    if (documentInfo.value.type !== 'pdf') {
      await router.replace({ name: 'Editor', params: { id: fileId.value } })
      return
    }
    pdfUrl.value = getDocumentUrl(fileId.value, 'inline')
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载 PDF 失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
})

function handleLoaded() {
  loading.value = false
}

function handleError(message: string) {
  loading.value = false
  ElMessage.error(message || 'PDF 预览失败')
}

function handleDownload() {
  if (!documentInfo.value) return
  const link = document.createElement('a')
  link.href = getDocumentUrl(documentInfo.value.id, 'attachment')
  link.download = documentInfo.value.name
  link.click()
}
</script>

<style scoped>
.pdf-viewer-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.left,
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.left h3 {
  margin: 0;
  font-size: 16px;
}

.viewer-body {
  flex: 1;
  min-height: 0;
}
</style>
