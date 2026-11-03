<template>
    <div class="register-page">
        <div class="register-card">
            <h2>用户注册</h2>
            <form @submit.prevent="handleRegister">
                <div class="form-group">
                    <label>用户名</label>
                    <input v-model="form.username" type="text" placeholder="4-20位字母,数字或下划线" required maxlength="20" />
                    <p v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</p>
                </div>
                <div class="form-group">
                    <label>昵称</label>
                    <input v-model="form.nickname" type="text" placeholder="请输入昵称" required />
                </div>
                <div class="form-group">
                    <label>密码</label>
                    <input v-model="form.password" type="password" placeholder="8-20位，需含大小写字母、数字" required maxlength="20"/>
                    <p v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</p>
                </div>
                <div class="form-group">
                    <label>确认密码</label>
                    <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" required />
                    <p v-if="fieldErrors.confirmPassword" class="field-error">{{ fieldErrors.confirmPassword }}</p>
                </div>
                <div class="form-group">
                    <label>邮箱</label>
                    <input v-model="form.email" type="email" placeholder="请输入邮箱" required />
                </div>
                <div class="form-group">
                    <label>手机号</label>
                    <input v-model="form.phone" type="tel" placeholder="请输入11位手机号" required maxlength="11" />
                    <p v-if="fieldErrors.phone" class="field-error">{{ fieldErrors.phone }}</p>
                </div>
                <button type="submit" class="register-btn" :disabled="loading">
                    {{ loading ? "注册中..." :"注册" }}
                </button>
            </form>
            <p class="login-link">已有账号？   <router-link to="/login">立即登录</router-link></p>
            <p class="error-msg" v-if="error">{{ error }}</p>
            <p class="success-msg" v-if="success">{{ success }}</p>
        </div>
    </div>
</template>


<script setup>
import {ref,reactive} from 'vue'
import {useRouter} from 'vue-router'
import  http from '../utils/http'

const router = useRouter()  //路由

const loading = ref(false)//初始值为false，false表示注册  true表示注册中
const error =ref('')//显示注册失败的信息
const success =ref('')//显示注册成功的信息

const form = ref({username:'', nickname:'',password:'',confirmPassword:'',email:'',phone:''})

const fieldErrors = reactive({
   username: '', password: '', confirmPassword: '', phone: ''

})

//正则表达式（用户名、密码、手机号）
//用户名（4‑20位英文、数字、下划线构成）
const usernamePattern = /^[A-Za-z0-9_]{4,20}$/
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$/
const phonePattern = /^1\d{10}$/

//使用正则表达式对用户名、密码、手机号进行匹配验证
const validateField = (field) => {
  switch(field){
    case 'username':
      fieldErrors.username = usernamePattern.test(form.value.username)? '' : '用户名需为4‑20位字母、数字或下划线'
      break
    case 'password':
      fieldErrors.password = passwordPattern.test(form.value.password)? '' : '密码需为8‑20位且包含大小写字母和数字'
      break
    case 'confirmPassword':
      fieldErrors.confirmPassword = form.value.password === form.value.confirmPassword? '' : '两次输入的密码不一致'
      break
    case 'phone':
      fieldErrors.phone = phonePattern.test(form.value.phone)? '' : '请输入正确的11位手机号'
      break
  }
}
//点击注册按钮执行的函数事件
// const handleRegister = ()=>{
//   console.log('你好啊')
// }
const handleRegister = async ()=>{
  error.value = ''        //显示注册失败的值初始化为空字符串
  success.value = ''      //显示注册成功的值初始化为空字符串

  //整体的校验
  Object.keys(fieldErrors).forEach(validateField)
  for(const msg of Object.values(fieldErrors)){
    if(msg){
      error.value = msg
      return
    }
  }

  loading.value = true

  //在前端向后端服务器发送post请求，完成注册的功能
  //async 用来修饰函数，配合 await 实现异步同步化写法
  try{
    await http.post('/auth/register',form.value)
    success.value = '注册成功，请登录'
    setTimeout(()=>router.push('/login'), 800)
  }catch(e){
    error.value = e.response?.data?.message || '注册失败'
  }finally{
    loading.value = false
  }
  

}



</script>






















<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 100px);
}
.register-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 500px;
}
.register-card h2 { text-align: center; margin-bottom: 30px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: bold; }
.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.register-btn {
  width: 100%;
  padding: 12px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}
.register-btn:hover { background: #66b1ff; }
.register-btn:disabled { background: #a0cfff; cursor: not-allowed; }
.login-link { text-align: center; margin-top: 20px; color: #666; }
.login-link a { color: #409eff; }
.field-error { color: #f56c6c; font-size: 12px; margin-top: 4px; }
.error-msg { color: #f56c6c; text-align: center; margin-top: 10px; }
.success-msg { color: #67c23a; text-align: center; margin-top: 10px; }
</style>