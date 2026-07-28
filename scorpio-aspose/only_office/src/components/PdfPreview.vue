<template>
  <div class="pdf-preview">
    <div class="pdf-toolbar">
      <el-button-group>
        <el-button :icon="ZoomOut" :disabled="pageWidth <= minWidth" @click="zoomOut">缩小</el-button>
        <el-button class="scale-display">{{ Math.round(zoomPercent) }}%</el-button>
        <el-button :icon="ZoomIn" :disabled="pageWidth >= maxWidth" @click="zoomIn">放大</el-button>
      </el-button-group>
      <el-button-group class="ml-12">
        <el-button :icon="ArrowLeft" :disabled="currentPage <= 1" @click="prevPage">上一页</el-button>
        <el-button class="page-display">{{ currentPage }} / {{ totalPages || '-' }}</el-button>
        <el-button :icon="ArrowRight" :disabled="currentPage >= totalPages" @click="nextPage">下一页</el-button>
      </el-button-group>
<!--      <el-input-number-->
<!--        v-model="jumpPage"-->
<!--        class="jump-input ml-12"-->
<!--        :min="1"-->
<!--        :max="Math.max(totalPages, 1)"-->
<!--        controls-position="right"-->
<!--        @change="handleJump"-->
<!--      />-->
      <el-button class="ml-12" @click="fitWidth">适应宽度</el-button>
      <el-button @click="resetScale">实际大小</el-button>
    </div>
    <div ref="containerRef" class="pdf-container">
      <VuePdfEmbed
        v-if="pageWidth > 0"
        ref="pdfRef"
        :source="source"
        :page="currentPage"
        :width="pageWidth"
        :scale="renderScale"
        @rendered="handleRendered"
        @loading-failed="handleError"
        @rendering-failed="handleError"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { ZoomIn, ZoomOut, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineProps<{
  source: string
}>()

const emit = defineEmits<{
  (e: 'loaded', totalPages: number): void
  (e: 'error', message: string): void
}>()

/** PDF 默认页宽（pt），用作 100% 基准 */
const BASE_PAGE_WIDTH = 612
const minWidth = 320
const maxWidth = 2400
const zoomStep = 80

const pdfRef = ref<{ doc?: { numPages?: number } } | null>(null)
const containerRef = ref<HTMLDivElement>()
const currentPage = ref(1)
const totalPages = ref(0)
const pageWidth = ref(0)
const jumpPage = ref(1)
const fittedOnce = ref(false)

const renderScale = Math.min(2, window.devicePixelRatio || 1.5)
const zoomPercent = computed(() => (pageWidth.value / BASE_PAGE_WIDTH) * 100)

onMounted(async () => {
  await nextTick()
  fitWidth()
})

function handleRendered() {
  const pages = pdfRef.value?.doc?.numPages ?? 1
  totalPages.value = pages
  if (!fittedOnce.value) {
    fittedOnce.value = true
    fitWidth()
  }
  emit('loaded', pages)
}

function handleError(err: unknown) {
  const message = err instanceof Error ? err.message : 'PDF 加载失败'
  ElMessage.error(message)
  emit('error', message)
}

function zoomIn() {
  pageWidth.value = Math.min(maxWidth, pageWidth.value + zoomStep)
}

function zoomOut() {
  pageWidth.value = Math.max(minWidth, pageWidth.value - zoomStep)
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value -= 1
    jumpPage.value = currentPage.value
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1
    jumpPage.value = currentPage.value
  }
}

function fitWidth() {
  const width = containerRef.value?.clientWidth ?? 1000
  pageWidth.value = Math.max(minWidth, Math.min(maxWidth, width - 24))
}

function resetScale() {
  pageWidth.value = BASE_PAGE_WIDTH
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
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.ml-12 {
  margin-left: 0;
}

.scale-display,
.page-display {
  min-width: 80px;
}

.jump-input {
  width: 120px;
}

.pdf-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 12px 8px 24px;
  background: #525659;
}

.pdf-container :deep(canvas),
.pdf-container :deep(.vue-pdf-embed) {
  max-width: none;
}
</style>
