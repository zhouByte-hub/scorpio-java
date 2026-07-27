import axios from 'axios'
import type { DocumentInfo } from '@/types'

const api = axios.create({
  baseURL: '/aspose',
  timeout: 30000,
})

/** 后端统一响应结构 */
interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

/** 获取文档列表 */
export async function fetchDocumentList(): Promise<DocumentInfo[]> {
  const { data } = await api.get<ApiResponse<DocumentInfo[]>>('/documents')
  return data.data ?? []
}

/** 获取文档下载地址 */
export function getDocumentUrl(fileId: string): string {
  return `/aspose/documents/${fileId}/download`
}

/** 获取OnlyOffice编辑器配置 */
export async function fetchEditorConfig(docId: string): Promise<Record<string, any>> {
  const { data } = await api.get<ApiResponse<Record<string, any>>>(`/documents/${docId}/editor-config`)
  return data.data
}
