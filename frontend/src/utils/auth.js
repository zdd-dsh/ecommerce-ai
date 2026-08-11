/**
 * Token管理工具
 * 使用localStorage存储token和用户信息
 * 
 */
const TOKEN_KEY = 'ecommerce_token'
const USER_KEY = 'ecommmerce_user'
/**
 * 获取Token
 * 
 * 
 */
export function getToken() {
    return localStorage.getItem(TOKEN_KEY)
}
/**
 * 设置Token
 * 
 */
export function setToken(){
    localStorage.setItem(TOKEN_KEY,token)
}
/**
 * 移除Token
 * 
 */
export function moveToken(){
    localStorage.removeItem(TOKEN_KEY)
}
/**T
 * 获取用户信息
 * 
 */
export function getUser(){
    const user = localStorage.getItem(USER_KEY)
    user ? JSON.parse(user) : null
}
/**
 * 设置用户信息
 */
export function setUser(user){
    localStorage.setItem(USER_KEY,JSON.stringify(user))
}
/**
 * 移除用户信息
 */
export function moveUser(){
    localStorage.removeItem(USER_KEY)
}