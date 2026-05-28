/**
 * 文章相关 API 请求封装
 * 包含文章 CRUD、分类管理、搜索等功能的后端接口调用
 * @author aceFelix
 */
import request from '@/utils/request'
import { cachedRequest } from '@/utils/apiClient'

/** 获取分类列表 */
export const getCategoryListService = () => request.get('/category/list')

/** 获取分类列表（带缓存，10分钟） */
export const getCategoryListCached = () =>
  cachedRequest('category-list', { url: '/category/list', method: 'GET' }, 10 * 60 * 1000)

/** 新增分类 */
export const addCategoryService = (categoryData) => request.post('/category', categoryData)

/** 更新分类 */
export const updateCategoryService = (categoryData) => request.put('/category', categoryData)

/** 删除分类 */
export const deleteCategoryService = (categoryId) =>
  request.delete('/category', { params: { id: categoryId } })

/** 获取文章列表（分页） */
export const articleListService = (params) => request.get('/article', { params })

/** 发布新文章 */
export const addArticleService = (articleData) => request.post('/article', articleData)

/** 获取文章详情 */
export const getArticleDetailService = (id) => request.get('/article/detail', { params: { id } })

/** 更新文章 */
export const updateArticleService = (articleData) => request.put('/article', articleData)

/** 删除文章 */
export const deleteArticleService = (id) => request.delete('/article', { params: { id } })

/** 全文搜索文章 */
export const searchArticleService = (params) => request.get('/search', { params })

/** 搜索建议（输入补全） */
export const searchSuggestService = (prefix) => request.get('/search/suggest', { params: { prefix } })

/** 获取热门搜索关键词 */
export const getHotKeywordsService = (topN = 10) => request.get('/search/hot-keywords', { params: { topN } })
