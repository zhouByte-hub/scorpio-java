<template>
  <div class="editor-view">
    <div class="editor-header">
      <div class="left">
        <el-button :icon="ArrowLeft" @click="goHome">返回</el-button>
        <div class="title-block">
          <h3>{{ documentInfo?.name || '文档编辑' }}</h3>
          <span class="status" :class="{ dirty: dirty, saving }">
            {{ statusText }}
          </span>
        </div>
      </div>
      <div class="right">
        <el-button :disabled="!documentInfo" @click="handleDownload">下载</el-button>
        <el-button type="primary" :loading="saving" :disabled="!dirty || loading" @click="handleSave">
          保存
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="editor-body">
      <DocxEditor
        v-if="!loading && htmlReady"
        v-model="htmlContent"
        @change="markDirty"
      />
      <el-empty v-else-if="!loading" description="文档内容为空" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DocxEditor from '@/components/DocxEditor.vue'
import {
  fetchDocumentDetail,
  fetchDocumentHtml,
  getDocumentUrl,
  saveDocumentHtml,
} from '@/api/document'
import type { DocumentInfo } from '@/types'

const route = useRoute()
const router = useRouter()

const fileId = computed(() => String(route.params.id || ''))
const documentInfo = ref<DocumentInfo | null>(null)
const htmlContent = ref('')
const htmlReady = ref(false)
const loading = ref(true)
const saving = ref(false)
const dirty = ref(false)

const statusText = computed(() => {
  if (saving.value) return '保存中...'
  if (dirty.value) return '未保存'
  return '已保存'
})

onMounted(async () => {
  await loadDocument()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

async function loadDocument() {
  if (!fileId.value) {
    ElMessage.error('文档 ID 无效')
    return
  }
  loading.value = true
  htmlReady.value = false
  try {
    documentInfo.value = await fetchDocumentDetail(fileId.value)
    if (documentInfo.value.type !== 'word') {
      await router.replace({ name: 'PdfViewer', params: { id: fileId.value } })
      return
    }
    const result = await fetchDocumentHtml(fileId.value)
    htmlContent.value = result.html || ''
    htmlReady.value = true
    dirty.value = false
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载文档失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

function markDirty() {
  dirty.value = true
}

async function handleSave() {
  if (!fileId.value || saving.value) return
  saving.value = true
  try {
    const saved = await saveDocumentHtml(fileId.value, htmlContent.value)
    documentInfo.value = saved
    dirty.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存失败'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

function handleDownload() {
  if (!documentInfo.value) return
  const link = document.createElement('a')
  link.href = getDocumentUrl(documentInfo.value.id, 'attachment')
  link.download = documentInfo.value.name
  link.click()
}

async function goHome() {
  if (dirty.value) {
    try {
      await ElMessageBox.confirm('当前有未保存的修改，确认离开？', '提示', {
        type: 'warning',
        confirmButtonText: '离开',
        cancelButtonText: '取消',
      })
    } catch {
      return
    }
  }
  router.push({ name: 'Home' })
}

function handleKeydown(event: KeyboardEvent) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 's') {
    event.preventDefault()
    if (dirty.value) {
      handleSave()
    }
  }
}
</script>

<style scoped>
.editor-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
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

.title-block h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.status {
  margin-top: 2px;
  display: block;
  font-size: 12px;
  color: #67c23a;
}

.status.dirty {
  color: #e6a23c;
}

.status.saving {
  color: #409eff;
}

.editor-body {
  flex: 1;
  min-height: 0;
}
</style>
