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
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.type === 'word'"
            type="primary"
            link
            @click.stop="openDocument(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.type === 'pdf'"
            type="primary"
            link
            @click.stop="openDocument(row)"
          >
            预览
          </el-button>
          <el-button type="success" link @click.stop="$emit('download', row)">
            下载
          </el-button>
          <el-button type="danger" link @click.stop="$emit('delete', row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Document } from '@element-plus/icons-vue'
import type { DocumentInfo } from '@/types'

defineProps<{
  documents: DocumentInfo[]
}>()

defineEmits<{
  (e: 'download', doc: DocumentInfo): void
  (e: 'delete', doc: DocumentInfo): void
}>()

const router = useRouter()

function openDocument(doc: DocumentInfo) {
  if (doc.type === 'pdf') {
    router.push({ name: 'PdfViewer', params: { id: doc.id } })
    return
  }
  router.push({ name: 'Editor', params: { id: doc.id } })
}

function handleRowClick(row: DocumentInfo) {
  openDocument(row)
}

function getFileIconColor(type: string): string {
  return type === 'pdf' ? '#f56c6c' : '#409eff'
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  return `${(bytes / 1024 ** i).toFixed(1)} ${units[i]}`
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  if (/^\d{4}-\d{2}-\d{2} /.test(dateStr)) {
    return dateStr
  }
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) {
    return dateStr
  }
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
