<template>
  <div ref="editorContainer" class="onlyoffice-editor"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'

const props = defineProps<{
  documentUrl: string
  documentTitle: string
  documentType: string
  editorMode?: 'edit' | 'view'
}>()

const emit = defineEmits<{
  (e: 'documentReady'): void
  (e: 'documentSave'): void
  (e: 'error', message: string): void
}>()

const editorContainer = ref<HTMLDivElement>()
let docEditor: any = null

function initEditor() {
  if (!editorContainer.value) return

  const config = {
    document: {
      fileType: getFileExtension(props.documentTitle),
      key: generateKey(),
      title: props.documentTitle,
      url: props.documentUrl,
      permissions: {
        edit: props.editorMode !== 'view',
        download: true,
        print: true,
      },
    },
    documentType: getDocumentType(),
    editorConfig: {
      callbackUrl: '',
      lang: 'zh-CN',
      mode: props.editorMode === 'view' ? 'view' : 'edit',
      user: {
        id: '1',
        name: '用户',
      },
      customization: {
        autosave: true,
        chat: false,
        comments: false,
        compactHeader: false,
        compactToolbar: false,
        forcesave: true,
        help: true,
        hideRightMenu: false,
        hideRulers: false,
        logo: {
          image: '',
          imageEmbedded: '',
        },
        toolbarNoTabs: false,
        uiTheme: 'theme-light',
      },
    },
    type: 'embedded',
    width: '100%',
    height: '100%',
    events: {
      onAppReady: () => {
        emit('documentReady')
      },
      onDocumentReady: () => {
        emit('documentReady')
      },
      onError: (event: any) => {
        emit('error', event?.data?.errorDescription ?? '编辑器加载失败')
      },
      onSave: () => {
        emit('documentSave')
      },
    },
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const win = window as any
  if (win.DocsAPI) {
    docEditor = new win.DocsAPI.DocumentEditor(editorContainer.value, config)
  }
}

function getFileExtension(fileName: string): string {
  return fileName.split('.').pop()?.toLowerCase() ?? 'docx'
}

function getDocumentType(): string {
  const ext = getFileExtension(props.documentTitle)
  if (['doc', 'docx', 'odt', 'rtf', 'txt', 'html', 'htm', 'mht'].includes(ext)) {
    return 'word'
  }
  if (['xls', 'xlsx', 'ods', 'csv'].includes(ext)) {
    return 'cell'
  }
  if (['ppt', 'pptx', 'odp'].includes(ext)) {
    return 'slide'
  }
  return 'word'
}

function generateKey(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 10)}`
}

function loadOnlyOfficeScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    const win = window as any
    if (win.DocsAPI) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = '/web-apps/apps/api/documents/editor.js'
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('OnlyOffice脚本加载失败，请确保Document Server已启动'))
    document.head.appendChild(script)
  })
}

watch(() => [props.documentUrl, props.documentTitle], () => {
  destroyEditor()
  initEditor()
})

onMounted(async () => {
  try {
    await loadOnlyOfficeScript()
    initEditor()
  } catch (err: any) {
    emit('error', err.message ?? '编辑器初始化失败')
  }
})

onBeforeUnmount(() => {
  destroyEditor()
})

function destroyEditor() {
  if (docEditor) {
    docEditor.destroyEditor()
    docEditor = null
  }
}
</script>

<style scoped>
.onlyoffice-editor {
  width: 100%;
  height: 100%;
  min-height: 600px;
}
</style>
