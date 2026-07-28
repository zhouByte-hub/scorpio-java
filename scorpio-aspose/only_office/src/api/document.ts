import axios from 'axios'
import type { DocumentHtml, DocumentInfo } from '@/types'

const api = axios.create({
  baseURL: '/aspose',
  timeout: 60000,
})

interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

function unwrap<T>(response: ApiResponse<T>, fallbackMessage: string): T {
  if (response.code !== 200) {
    throw new Error(response.message || fallbackMessage)
  }
  return response.data
}

/** 获取文档列表 */
export async function fetchDocumentList(): Promise<DocumentInfo[]> {
  const { data } = await api.get<ApiResponse<DocumentInfo[]>>('/documents')
  return unwrap(data, '获取文档列表失败') ?? []
}

/** 获取文档详情 */
export async function fetchDocumentDetail(fileId: string): Promise<DocumentInfo> {
  const { data } = await api.get<ApiResponse<DocumentInfo>>(`/documents/${fileId}`)
  return unwrap(data, '获取文档详情失败')
}

/** 删除文档 */
export async function deleteDocument(fileId: string): Promise<void> {
  const { data } = await api.delete<ApiResponse<string>>(`/documents/${fileId}`)
  unwrap(data, '删除文档失败')
}

/** 上传文档 */
export async function uploadDocument(
  file: File,
  onProgress?: (percent: number) => void,
): Promise<DocumentInfo> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await api.post<ApiResponse<DocumentInfo>>('/documents/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress(event) {
      if (event.total && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100))
      }
    },
  })
  return unwrap(data, '上传失败')
}

/** 获取 Word 可编辑 HTML */
export async function fetchDocumentHtml(fileId: string): Promise<DocumentHtml> {
  const { data } = await api.get<ApiResponse<DocumentHtml>>(`/documents/${fileId}/html`)
  return unwrap(data, '加载文档内容失败')
}

/** 保存 Word HTML */
export async function saveDocumentHtml(fileId: string, html: string): Promise<DocumentInfo> {
  const { data } = await api.put<ApiResponse<DocumentInfo>>(`/documents/${fileId}/html`, {
    fileId,
    html,
  })
  return unwrap(data, '保存文档失败')
}

/** 文档下载地址 */
export function getDocumentUrl(fileId: string, disposition: 'inline' | 'attachment' = 'inline'): string {
  return `/aspose/documents/${fileId}/download?disposition=${disposition}`
}
