<template>
  <div class="docx-editor">
    <div v-if="editor" class="editor-toolbar">
      <el-button-group>
        <el-button :type="editor.isActive('bold') ? 'primary' : 'default'" @click="editor.chain().focus().toggleBold().run()">
          加粗
        </el-button>
        <el-button :type="editor.isActive('italic') ? 'primary' : 'default'" @click="editor.chain().focus().toggleItalic().run()">
          斜体
        </el-button>
        <el-button :type="editor.isActive('underline') ? 'primary' : 'default'" @click="editor.chain().focus().toggleUnderline().run()">
          下划线
        </el-button>
        <el-button :type="editor.isActive('strike') ? 'primary' : 'default'" @click="editor.chain().focus().toggleStrike().run()">
          删除线
        </el-button>
      </el-button-group>

      <el-button-group>
        <el-button :type="editor.isActive('heading', { level: 1 }) ? 'primary' : 'default'" @click="editor.chain().focus().toggleHeading({ level: 1 }).run()">
          H1
        </el-button>
        <el-button :type="editor.isActive('heading', { level: 2 }) ? 'primary' : 'default'" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()">
          H2
        </el-button>
        <el-button :type="editor.isActive('heading', { level: 3 }) ? 'primary' : 'default'" @click="editor.chain().focus().toggleHeading({ level: 3 }).run()">
          H3
        </el-button>
      </el-button-group>

      <el-button-group>
        <el-button :type="editor.isActive({ textAlign: 'left' }) ? 'primary' : 'default'" @click="editor.chain().focus().setTextAlign('left').run()">
          左对齐
        </el-button>
        <el-button :type="editor.isActive({ textAlign: 'center' }) ? 'primary' : 'default'" @click="editor.chain().focus().setTextAlign('center').run()">
          居中
        </el-button>
        <el-button :type="editor.isActive({ textAlign: 'right' }) ? 'primary' : 'default'" @click="editor.chain().focus().setTextAlign('right').run()">
          右对齐
        </el-button>
        <el-button :type="editor.isActive({ textAlign: 'justify' }) ? 'primary' : 'default'" @click="editor.chain().focus().setTextAlign('justify').run()">
          两端对齐
        </el-button>
      </el-button-group>

      <el-button-group>
        <el-button :type="editor.isActive('bulletList') ? 'primary' : 'default'" @click="editor.chain().focus().toggleBulletList().run()">
          无序列表
        </el-button>
        <el-button :type="editor.isActive('orderedList') ? 'primary' : 'default'" @click="editor.chain().focus().toggleOrderedList().run()">
          有序列表
        </el-button>
        <el-button @click="editor.chain().focus().unsetAllMarks().clearNodes().run()">
          清除格式
        </el-button>
      </el-button-group>
    </div>

    <div class="editor-paper">
      <editor-content v-if="editor" :editor="editor" class="editor-content" />
      <el-empty v-else description="编辑器初始化中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import TextAlign from '@tiptap/extension-text-align'
import TextStyle from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import Highlight from '@tiptap/extension-highlight'
import Link from '@tiptap/extension-link'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'

const props = withDefaults(defineProps<{
  modelValue: string
  editable?: boolean
}>(), {
  editable: true,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change'): void
}>()

const editor = useEditor({
  content: props.modelValue || '',
  editable: props.editable,
  extensions: [
    StarterKit,
    Underline,
    TextStyle,
    Color,
    Highlight,
    Image.configure({ inline: true, allowBase64: true }),
    Link.configure({ openOnClick: false }),
    TextAlign.configure({ types: ['heading', 'paragraph'] }),
    Placeholder.configure({ placeholder: '开始编辑文档...' }),
  ],
  onUpdate({ editor: current }) {
    emit('update:modelValue', current.getHTML())
    emit('change')
  },
})

watch(
  () => props.modelValue,
  (value) => {
    const current = editor.value
    if (!current || value === current.getHTML()) {
      return
    }
    current.commands.setContent(value || '', false)
  },
)

watch(
  () => props.editable,
  (value) => {
    editor.value?.setEditable(value)
  },
)

function getHtml(): string {
  return editor.value?.getHTML() ?? ''
}

defineExpose({ getHtml })
</script>

<style scoped>
.docx-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #eef1f6;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.editor-paper {
  flex: 1;
  overflow: auto;
  padding: 16px 20px 24px;
}

.editor-content {
  min-height: calc(100% - 32px);
  width: min(1200px, 96%);
  max-width: none;
  margin: 0 auto;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 4px;
}

.editor-content :deep(.tiptap) {
  min-height: calc(100vh - 180px);
  padding: 40px 56px;
  outline: none;
  line-height: 1.8;
  font-size: 16px;
  color: #303133;
}

.editor-content :deep(.tiptap *) {
  max-width: 100%;
}

.editor-content :deep(.tiptap p.is-editor-empty:first-child::before) {
  color: #c0c4cc;
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}

.editor-content :deep(.tiptap img) {
  max-width: 100%;
  height: auto;
}

.editor-content :deep(.tiptap table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.editor-content :deep(.tiptap td),
.editor-content :deep(.tiptap th) {
  border: 1px solid #dcdfe6;
  padding: 6px 10px;
}
</style>
