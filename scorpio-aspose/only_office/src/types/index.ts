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

/** 文档 HTML 内容 */
export interface DocumentHtml {
  fileId: string
  html: string
}

/** PDF 预览状态 */
export interface PdfViewerState {
  currentPage: number
  totalPages: number
  scale: number
}
