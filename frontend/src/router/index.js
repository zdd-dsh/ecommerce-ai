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
        component: ()=>import('../views/HomeView.vue')
    },
    
    {  //登录页面的配置
        path: '/login',
        name: 'Login',
        component: ()=>import('../views/LoginView.vue')
    },
    {  //注册页面的配置
        path: '/register',
        name: 'Register',
        component: ()=>import('../views/RegisterView.vue')
    },
    {
        path: '/products/:id',
        name: 'products',
        component: ()=>import('../views/ProductDetailView.vue')
    },
    {
        path: '/products/list',
        name: 'productsV',
        component: ()=>import('../views/ProductView.vue')
    },
    //购物车页面的配置
    {
        path: '/cart',
        name: 'cart',
        component: ()=>import('../views/CartView.vue'),
        meta:{ requiresAuth:true }
    },
    //结算的配置
    {
        path: '/checkout',
        name: 'checkout',
        component: ()=>import('../views/CheckOutView.vue'),
        meta:{ requiresAuth:true }
    },
     {
        path: '/AddressBook',
        name: 'AddressBook',
        component: ()=>import('../views/AddressBook.vue'),
        meta:{ requiresAuth:true }
    },
    {
    //订单列表页面的配置
    path: '/orders',
    name: 'orders',
    component: ()=>import('../views/OrdersView.vue'),
    meta:{ requiresAuth:true }
    },
    {
    // 订单详情页面的配置
    path: '/orders/:id',
    name: 'order-detail',
    component: ()=>import('../views/OrderDetailView.vue'),
    meta:{ requiresAuth:true }
    }

]

//创建一个路由，名为router
const router = createRouter({
    history:createWebHistory(),
    routes
})

//路由守卫 检查登录状态
router.beforeEach((to, from, next) => {
   if(to.meta.requiresAuth && !getToken()){
       //需要登录但是没有token，跳转到login页面
       next('/login')
   }else{
       next()
   }
})

export default router
