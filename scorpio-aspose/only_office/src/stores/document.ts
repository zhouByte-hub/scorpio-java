import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DocumentInfo } from '@/types'
import { deleteDocument, fetchDocumentList } from '@/api/document'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref<DocumentInfo[]>([])
  const loading = ref(false)

  async function loadDocuments() {
    loading.value = true
    try {
      documents.value = await fetchDocumentList()
    } finally {
      loading.value = false
    }
  }

  function addDocument(doc: DocumentInfo) {
    const index = documents.value.findIndex((item) => item.id === doc.id)
    if (index >= 0) {
      documents.value.splice(index, 1, doc)
    } else {
      documents.value.unshift(doc)
    }
  }

  async function removeDocument(fileId: string) {
    await deleteDocument(fileId)
    documents.value = documents.value.filter((item) => item.id !== fileId)
  }

  function getDocumentById(fileId: string): DocumentInfo | undefined {
    return documents.value.find((item) => item.id === fileId)
  }

  return {
    documents,
    loading,
    loadDocuments,
    addDocument,
    removeDocument,
    getDocumentById,
  }
})
