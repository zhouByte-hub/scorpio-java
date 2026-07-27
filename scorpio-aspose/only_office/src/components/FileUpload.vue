<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
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
          支持 Word (.doc/.docx) 和 PDF (.pdf) 文件，单文件不超过100MB
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
import axios from 'axios'
import type { DocumentInfo } from '@/types'

const emit = defineEmits<{
  (e: 'success', doc: DocumentInfo): void
  (e: 'error', message: string): void
}>()

const uploadRef = ref()
const uploading = ref(false)
const uploadProgress = ref(0)

const ALLOWED_EXTENSIONS = ['doc', 'docx', 'pdf']
const MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB

/** 后端统一响应结构 */
interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

function beforeUpload(file: UploadRawFile): boolean {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    ElMessage.error('仅支持 Word (.doc/.docx) 和 PDF (.pdf) 文件')
    return false
  }
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

async function handleUpload(options: UploadRequestOptions) {
  const file = options.file
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  // 根据文件类型选择后端上传路径
  const uploadUrl = extension === 'pdf' ? '/aspose/pdf/detect/upload' : '/aspose/word/detect/upload'

  uploading.value = true
  uploadProgress.value = 0

  const formData = new FormData()
  formData.append('file', file)

  try {
    const { data } = await axios.post<ApiResponse<string>>(uploadUrl, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress(event) {
        if (event.total) {
          uploadProgress.value = Math.round((event.loaded / event.total) * 100)
        }
      },
    })

    uploadProgress.value = 100
    uploading.value = false
    ElMessage.success('文件上传成功')

    const filePath: string = data.data
    const fileName = filePath.split('/').pop() ?? file.name
    const fileId = fileName.includes('.') ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName
    const ext = fileName.includes('.') ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : ''

    const doc: DocumentInfo = {
      id: fileId,
      name: fileName,
      type: ext === 'pdf' ? 'pdf' : 'word',
      fileName,
      fileSize: file.size,
      fileUrl: `/aspose/documents/${fileId}/download`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }

    emit('success', doc)
  } catch {
    uploading.value = false
    ElMessage.error('文件上传失败，请重试')
    emit('error', '上传失败')
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
