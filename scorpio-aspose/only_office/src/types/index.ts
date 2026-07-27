/** 文档信息 */
export interface DocumentInfo {
  id: string
  name: string
  type: 'word' | 'pdf'
  fileName: string
  fileSize: number
  fileUrl: string
  createdAt: string
  updatedAt: string
}

/** OnlyOffice编辑器配置 */
export interface OnlyOfficeConfig {
  document: {
    fileType: string
    key: string
    title: string
    url: string
  }
  documentType: string
  editorConfig: {
    callbackUrl: string
    lang: string
    mode: string
    user: {
      id: string
      name: string
    }
  }
}

/** PDF预览状态 */
export interface PdfViewerState {
  currentPage: number
  totalPages: number
  scale: number
  searchTerm: string
}
