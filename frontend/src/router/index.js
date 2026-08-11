/**
 * 用户端的路由配置（页面跳转的相关配置）
 * 
 */
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

//用户端的路由配置
const routes = [
    {  //首页的配置
        path: '/',
        name: 'Home',
        componet: ()=>import('../views/HomeView.vue')
    },
    
]