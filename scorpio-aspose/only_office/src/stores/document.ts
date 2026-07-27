import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DocumentInfo } from '@/types'
import { fetchDocumentList } from '@/api/document'

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
    documents.value.unshift(doc)
  }

  return { documents, loading, loadDocuments, addDocument }
})
