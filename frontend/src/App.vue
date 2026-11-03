<template>
  <div id="app">
    <!--导航栏-->
    <nav class="navbar">
  <div class="nav-container">
    <router-link to="" class="nav-logo">🛒 电商平台</router-link>
    <div class="nav-links">
      <router-link to="/">首页</router-link>
      <router-link to="/products/list">商品</router-link>
       
      <!-- 登录成功以后 -->
      <template v-if="isLoggedIn">
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">订单</router-link>
        <span class="nav-user">{{ username}}</span>
        <button @click="logout" class="nav-btn">退出</button>
      </template>

      <!-- 没有登陆 -->
      <template v-else>
        <router-link to="/login">登录</router-link>
        <router-link to="/register">注册</router-link>

      </template>

     


      
    </div>
  </div>
</nav>
    <!--路由出口-->
    <!-- 路由视图出口，匹配到的页面组件会渲染在这里 -->
     <main class="main-content">
      <router-view />
     </main>
  </div>
</template>

<script setup>
  import { computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { getToken, getUser , removeUser, removeToken } from './utils/auth'
  
  //创建路由实例 用于跳转页面（router包下的index.js）
  const router = useRouter()



  //1.isLoggedIn变量
  // 思路：computed()计算属性获得用户的登录信息，给isLoggedIn变量赋值
  // 计算属性，判断用户是否已登录，有token即为登录状态
  const isLoggedIn = computed(() => !!getToken())

  //2.username变量
  const username = computed(() => {
    //获取存储的用户信息（用户对象）
    const user = getUser()
    //如果user不为空，将user.usernane返回，如果user为空，fanhui''
    return user ? user.username : ''
})

  //3.loginout事件
  const logout = ()=>{
    //删除token数据
    removeToken()
    //删除用户数据
    removeUser()
    //通过路由跳转到登录页面
    router.push('/login')
    window.location.reload()
}

</script>