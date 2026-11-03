/**
 * Token管理工具
 * 使用localStorage存储token和用户信息
 * 
 */
const TOKEN_KEY = 'ecommerce_token'
const USER_KEY = 'ecommerce_user'

export const isLoggedIn = ()=>Boolean(getToken())

export const isAdmin = (user=getUser()) =>{
  return (user? user.role : '').toUpperCase() === 'ADMIN'
}

/**
 * 获取Token
 */
export function getToken() {
    return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token){
    localStorage.setItem(TOKEN_KEY,token)
}

/**
 * 移除Token
 */
export function removeToken(){
    localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUser(){
    const user = localStorage.getItem(USER_KEY)
    return user ? JSON.parse(user) : null
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
export function removeUser(){
    localStorage.removeItem(USER_KEY)
}

/**
 * 清空全部登录信息（登出）
 */
export function clearAuth(){
  removeToken()
  removeUser()
}
