/**
 * 前端公共格式化工具函数集合
 */

/**
 * 1、价格格式化函数
 */
export const formatPrice= (price) => Number(price || 0).toFixed(2)
/**
 * 2、日期格式化
 * toLocaleString()会根据浏览器的语言环境自动格式化日期
 * 中文环境：'2026/8/27 15:31:33'
 */
export const formatDate = (dateString) => {
  if(!dateString){
    return '-'
  }
  //使用Date对象解析并转换为本地时间字符串
  return new Date(dateString).toLocaleString()
}
