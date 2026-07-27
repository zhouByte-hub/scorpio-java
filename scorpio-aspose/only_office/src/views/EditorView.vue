<template>
  <div class="editor-view">
    <div class="editor-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/')">返回</el-button>
      <span class="doc-title">{{ docTitle }}</span>
      <el-tag v-if="editorMode === 'view'" type="info" size="small">预览模式</el-tag>
      <el-tag v-else type="success" size="small">编辑模式</el-tag>
      <div class="header-actions">
        <el-switch
          v-model="isEditMode"
          active-text="编辑"
          inactive-text="预览"
          @change="toggleMode"
        />
      </div>
    </div>

    <div class="editor-body">
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        closable
        @close="errorMessage = ''"
      />
      <div v-if="!errorMessage" class="editor-wrapper">
        <OnlyOfficeEditor
          :document-url="documentUrl"
          :document-title="docTitle"
          :document-type="docType"
          :editor-mode="editorMode"
          @document-ready="handleReady"
          @document-save="handleSave"
          @error="handleError"
        />
      </div>
      <el-empty v-if="!documentUrl && !errorMessage" description="文档加载中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import OnlyOfficeEditor from '@/components/OnlyOfficeEditor.vue'
import { getDocumentUrl } from '@/api/document'

const route = useRoute()
const docId = computed(() => route.params.id as string)

const documentUrl = ref('')
const docTitle = ref('未命名文档')
const docType = ref('word')
const editorMode = ref<'edit' | 'view'>('edit')
const isEditMode = ref(true)
const errorMessage = ref('')

onMounted(() => {
  // 根据文档ID构建文档URL
  documentUrl.value = getDocumentUrl(docId.value)
  docTitle.value = `文档-${docId.value}.docx`
  docType.value = 'word'
})

function toggleMode(val: boolean) {
  editorMode.value = val ? 'edit' : 'view'
}

function handleReady() {
  ElMessage.success('文档加载完成')
}

function handleSave() {
  ElMessage.success('文档保存成功')
}

function handleError(message: string) {
  errorMessage.value = message
  ElMessage.error(message)
}
</script>

<style scoped>
.editor-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.editor-header {
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

.header-actions {
  margin-left: auto;
}

.editor-body {
  flex: 1;
  overflow: hidden;
}

.editor-wrapper {
  width: 100%;
  height: 100%;
}
</style>
