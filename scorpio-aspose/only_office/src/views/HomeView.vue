<template>
  <div class="home-view">
    <div class="page-header">
      <h2>文档管理</h2>
      <el-button type="primary" @click="showUpload = true">
        <el-icon><Plus /></el-icon>
        上传文档
      </el-button>
    </div>

    <!-- 上传区域 -->
    <el-dialog v-model="showUpload" title="上传文档" width="560px" :close-on-click-modal="false">
      <FileUpload @success="handleUploadSuccess" @error="handleUploadError" />
    </el-dialog>

    <!-- 文件列表 -->
    <el-card shadow="never" class="file-card">
      <template #header>
        <div class="card-header">
          <span>文档列表</span>
          <el-button link type="primary" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-empty v-if="!loading && documents.length === 0" description="暂无文档，请上传" />
      <FileList v-else :documents="documents" @download="handleDownload" />
      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="5" animated />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import FileUpload from '@/components/FileUpload.vue'
import FileList from '@/components/FileList.vue'
import { useDocumentStore } from '@/stores/document'
import { getDocumentUrl } from '@/api/document'
import type { DocumentInfo } from '@/types'

const store = useDocumentStore()
const showUpload = ref(false)

const documents = computed(() => store.documents)
const loading = computed(() => store.loading)

onMounted(() => {
  store.loadDocuments()
})

function refreshList() {
  store.loadDocuments()
}

function handleUploadSuccess(doc: DocumentInfo) {
  showUpload.value = false
  store.addDocument(doc)
}

function handleUploadError(message: string) {
  ElMessage.error(message ?? '上传失败')
}

function handleDownload(doc: DocumentInfo) {
  const url = getDocumentUrl(doc.id)
  const link = document.createElement('a')
  link.href = url
  link.download = doc.name
  link.click()
}
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.file-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-wrapper {
  padding: 16px 0;
}
</style>
