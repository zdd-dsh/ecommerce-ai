<template>
    <div class="login-page">
        <div class="login-card">
            <h2>用户登录</h2>
            <form @submit.prevent="handleLogin">
                <div class="form-group">
                    <label>用户名</label>
                    <input type="text" v-model="form.username"  placeholder="请输入用户名" required />
                </div>
                 <div class="form-group">
                     <label>密码</label>
                    <input type="password" v-model="form.password"  placeholder="请输入密码" required />
                </div>
                <button class="login-btn" type="submit">
                    {{ loading ? '登录中...' : '登录' }}
                </button>

            </form>
            <p class="register-link">
                还没有账号? <router-link to="/register">立即注册</router-link>
            </p>
        </div>
    </div>
</template>
    
<script setup>
    import { ref } from 'vue'
    import { useRouter } from 'vue-router'
    import { setToken, setUser } from '../utils/auth'
    import http from '../utils/http'


    //创建路由实例，用于跳转页面 （router包下的index.js）
    const router = useRouter()

    //loading是布尔类型的结果 
    const loading = ref(false)
    //用户名和密码输入框的值显示
    const form = ref({
        username: '',
        password: ''
    })
    //被async修饰的函数，永远返回Promise对象
    //点击登录按钮，登录页面表单提交执行的函数
    const handleLogin = async () =>{
        loading.value = true//将按钮由登录 变成登陆中
        try{ //捕获异常
            //asnyc用来修饰函数，配合await实现异步同步化写法
            //向服务器端（后端）发送post请求
            //参数1：请求地址 参数2 ：请求的数据
           const res =  await http.post('/auth/login',form.value)
           //判断res的返回结果
           if(res.success){ //用户名和密码正确的情况
                //保存Token
                setToken(res.data.token)
                //保存user
                setUser(res.data.user)
                //使用路由跳转的首页
                await router.push('/')
                window.location.reload()
           }else{ //用户名和密码不正确的情况
                alert(res.message || '登录失败')
            }
        }catch(error){//处理异常

            alert('登录失败'+(error.response?.data?.message || '网络错误'))
        }finally{ //总会执行
            loading.value = false //将按钮由登录中变成登录
        }
    }
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 100px);
}
.login-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
}
.login-card h2 { text-align: center; margin-bottom: 30px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: bold; }
.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.login-btn {
  width: 100%;
  padding: 12px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}
.login-btn:hover { background: #66b1ff; }
.login-btn:disabled { background: #a0cfff; cursor: not-allowed; }
.c { text-align: center; margin-top: 20px; color: #666; }
.register-link a { color: #409eff; }
.error-msg { color: #f56c6c; text-align: center; margin-top: 10px; }
</style>