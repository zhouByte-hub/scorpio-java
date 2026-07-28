<template>
  <div class="file-upload">
    <el-upload
      :http-request="handleUpload"
      :before-upload="beforeUpload"
      :show-file-list="false"
      :multiple="false"
      :drag="true"
      accept=".doc,.docx,.pdf"
    >
      <div class="upload-dragger-content">
        <el-icon class="upload-icon" :size="48"><UploadFilled /></el-icon>
        <div class="upload-text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <div class="upload-tip">
          支持 Word (.doc/.docx) 和 PDF (.pdf)，单文件不超过 100MB
        </div>
      </div>
    </el-upload>
    <el-progress
      v-if="uploading"
      :percentage="uploadProgress"
      :stroke-width="6"
      class="upload-progress"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadRawFile, UploadRequestOptions } from 'element-plus'
import { uploadDocument } from '@/api/document'
import type { DocumentInfo } from '@/types'

const emit = defineEmits<{
  (e: 'success', doc: DocumentInfo): void
  (e: 'error', message: string): void
}>()

const uploading = ref(false)
const uploadProgress = ref(0)

const ALLOWED_EXTENSIONS = ['doc', 'docx', 'pdf']
const MAX_FILE_SIZE = 100 * 1024 * 1024

function beforeUpload(file: UploadRawFile): boolean {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    ElMessage.error('仅支持 Word (.doc/.docx) 和 PDF (.pdf) 文件')
    return false
  }
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

async function handleUpload(options: UploadRequestOptions) {
  uploading.value = true
  uploadProgress.value = 0
  try {
    const doc = await uploadDocument(options.file, (percent) => {
      uploadProgress.value = percent
    })
    uploadProgress.value = 100
    ElMessage.success('文件上传成功')
    emit('success', doc)
  } catch (error) {
    const message = error instanceof Error ? error.message : '上传失败'
    ElMessage.error(message)
    emit('error', message)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-dragger-content {
  padding: 40px 20px;
  text-align: center;
}

.upload-icon {
  color: #c0c4cc;
  margin-bottom: 12px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.upload-progress {
  margin-top: 12px;
}
</style>
