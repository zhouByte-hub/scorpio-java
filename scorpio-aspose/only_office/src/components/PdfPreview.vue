<template>
  <div class="pdf-preview">
    <div class="pdf-toolbar">
      <el-button-group>
        <el-button :icon="ZoomOut" @click="zoomOut" :disabled="scale <= 0.5">缩小</el-button>
        <el-button class="scale-display">{{ Math.round(scale * 100) }}%</el-button>
        <el-button :icon="ZoomIn" @click="zoomIn" :disabled="scale >= 3">放大</el-button>
      </el-button-group>
      <el-button-group class="ml-12">
        <el-button :icon="ArrowLeft" @click="prevPage" :disabled="currentPage <= 1">上一页</el-button>
        <el-button class="page-display">{{ currentPage }} / {{ totalPages }}</el-button>
        <el-button :icon="ArrowRight" @click="nextPage" :disabled="currentPage >= totalPages">下一页</el-button>
      </el-button-group>
      <el-input
        v-model="searchTerm"
        placeholder="搜索文档内容"
        class="search-input ml-12"
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
      />
    </div>
    <div class="pdf-container" ref="containerRef">
      <VuePdfEmbed
        ref="pdfRef"
        :source="pdfSource"
        :page="currentPage"
        :scale="scale"
        @rendered="handleRendered"
        @loading-failed="handleError"
        @rendering-failed="handleError"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { ZoomIn, ZoomOut, ArrowLeft, ArrowRight, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  source: string
}>()

const emit = defineEmits<{
  (e: 'loaded', totalPages: number): void
  (e: 'error', message: string): void
}>()

const pdfRef = ref()
const containerRef = ref<HTMLDivElement>()
const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1.5)
const searchTerm = ref('')

const pdfSource = computed(() => ({
  url: props.source,
  cMapUrl: 'https://unpkg.com/pdfjs-dist@3.11.174/cmaps/',
  cMapPacked: true,
}))

function handleRendered() {
  if (pdfRef.value) {
    totalPages.value = pdfRef.value.doc?.numPages ?? 1
    emit('loaded', totalPages.value)
  }
}

function handleError(err: any) {
  const message = err?.message ?? 'PDF加载失败'
  ElMessage.error(message)
  emit('error', message)
}

function zoomIn() {
  if (scale.value < 3) {
    scale.value = Math.min(3, scale.value + 0.25)
  }
}

function zoomOut() {
  if (scale.value > 0.5) {
    scale.value = Math.max(0.5, scale.value - 0.25)
  }
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

function handleSearch() {
  if (!searchTerm.value) return
  ElMessage.info('PDF搜索功能需要配合PDF.js底层API实现')
}
</script>

<style scoped>
.pdf-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  gap: 8px;
  flex-wrap: wrap;
}

.scale-display,
.page-display {
  cursor: default;
  min-width: 80px;
  text-align: center;
}

.search-input {
  width: 220px;
}

.pdf-container {
  flex: 1;
  overflow: auto;
  padding: 16px;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
}

.ml-12 {
  margin-left: 12px;
}
</style>
