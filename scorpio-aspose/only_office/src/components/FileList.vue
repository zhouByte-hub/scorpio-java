<template>
  <div class="file-list">
    <el-table :data="documents" stripe style="width: 100%" @row-click="handleRowClick">
      <el-table-column prop="name" label="文件名" min-width="240">
        <template #default="{ row }">
          <div class="file-name-cell">
            <el-icon :size="24" :color="getFileIconColor(row.type)">
              <Document />
            </el-icon>
            <span class="file-name">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'pdf' ? 'danger' : 'primary'" size="small">
            {{ row.type === 'pdf' ? 'PDF' : 'Word' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="大小" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="修改时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.type === 'word'"
            type="primary"
            link
            @click.stop="$router.push({ name: 'Editor', params: { id: row.id } })"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.type === 'pdf'"
            type="primary"
            link
            @click.stop="$router.push({ name: 'PdfViewer', params: { id: row.id } })"
          >
            预览
          </el-button>
          <el-button type="success" link @click.stop="handleDownload(row)">
            下载
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'
import type { DocumentInfo } from '@/types'

defineProps<{
  documents: DocumentInfo[]
}>()

const emit = defineEmits<{
  (e: 'download', doc: DocumentInfo): void
}>()

function handleRowClick(row: DocumentInfo) {
  // 预留行点击事件
  console.log('点击文档:', row.name)
}

function handleDownload(doc: DocumentInfo) {
  emit('download', doc)
}

function getFileIconColor(type: string): string {
  return type === 'pdf' ? '#f56c6c' : '#409eff'
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${units[i]}`
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<style scoped>
.file-list {
  width: 100%;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
