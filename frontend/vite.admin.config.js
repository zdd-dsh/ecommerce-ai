import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  //define:全局静态变量
  define:{
    //shop用来区分当前是商城前台项目（shop可以自定义）
    'import.meta.env.VITE_APP_VARIANT':JSON.stringify('shop')
  },
  plugins: [vue()],
//server 开发中服务器的相关配置
server:{
  //port：置为3001，区分商城前台的3000 后台管理系统页面：http://localhost:3001
  port:3001,
  //proxy:配置接口代理，解决前端本地开发跨域的问题
  proxy:{
    //匹配所有api开头的请求路径
    '/api':{
      //target 代理目标地址，后端 SpringBoot 服务地址，所有 /api 请求转发到 8080 后端 http://localhost:8080
      target:'http://localhost:8080',
      //changeOrigin: 开启跨域模式，解决跨域校验
      changeOrigin:true,
      //secure: 是否校验HTTP证书
      secure:true,
    }
  }
},
//打包产物输出到dist-admin文件夹中，不和前台默认文件夹冲突
build:{
    outDir:'dist-admin'
}
})
