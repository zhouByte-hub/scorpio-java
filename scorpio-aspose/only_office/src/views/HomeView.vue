<template>
  <div class="home-view">
    <div class="page-header">
      <h2>文档管理</h2>
      <el-button type="primary" @click="showUpload = true">
        <el-icon><Plus /></el-icon>
        上传文档
      </el-button>
    </div>

    <el-dialog v-model="showUpload" title="上传文档" width="560px" :close-on-click-modal="false">
      <FileUpload @success="handleUploadSuccess" @error="handleUploadError" />
    </el-dialog>

    <el-card shadow="never" class="file-card">
      <template #header>
        <div class="card-header">
          <span>文档列表</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              clearable
              placeholder="搜索文件名"
              class="search-input"
              :prefix-icon="Search"
            />
            <el-button link type="primary" @click="refreshList">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="5" animated />
      <el-empty v-else-if="filteredDocuments.length === 0" description="暂无文档，请上传" />
      <FileList
        v-else
        :documents="filteredDocuments"
        @download="handleDownload"
        @delete="handleDelete"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FileUpload from '@/components/FileUpload.vue'
import FileList from '@/components/FileList.vue'
import { useDocumentStore } from '@/stores/document'
import { getDocumentUrl } from '@/api/document'
import type { DocumentInfo } from '@/types'

const store = useDocumentStore()
const router = useRouter()
const showUpload = ref(false)
const keyword = ref('')

const documents = computed(() => store.documents)
const loading = computed(() => store.loading)
const filteredDocuments = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return documents.value
  return documents.value.filter((doc) => doc.name.toLowerCase().includes(key))
})

onMounted(() => {
  store.loadDocuments().catch((error: unknown) => {
    const message = error instanceof Error ? error.message : '加载文档列表失败'
    ElMessage.error(message)
  })
})

function refreshList() {
  store.loadDocuments().catch((error: unknown) => {
    const message = error instanceof Error ? error.message : '刷新失败'
    ElMessage.error(message)
  })
}

function handleUploadSuccess(doc: DocumentInfo) {
  showUpload.value = false
  store.addDocument(doc)
  ElMessage.success('上传成功，正在打开...')
  if (doc.type === 'pdf') {
    router.push({ name: 'PdfViewer', params: { id: doc.id } })
    return
  }
  router.push({ name: 'Editor', params: { id: doc.id } })
}

function handleUploadError(message: string) {
  ElMessage.error(message || '上传失败')
}

function handleDownload(doc: DocumentInfo) {
  const link = document.createElement('a')
  link.href = getDocumentUrl(doc.id, 'attachment')
  link.download = doc.name
  link.click()
}

async function handleDelete(doc: DocumentInfo) {
  try {
    await ElMessageBox.confirm(`确认删除「${doc.name}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await store.removeDocument(doc.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    const message = error instanceof Error ? error.message : '删除失败'
    ElMessage.error(message)
  }
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
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
}
</style>
