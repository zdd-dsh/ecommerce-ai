<template>
  <div class="home">
    <!--全部商品-->
    <section class="section">
      <h2>🛒全部商品</h2>
      <p class="section-desc">精选好物，品质保障</p>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else class="product-grid">
        <!--全部商品卡片都放在v-for循环里面，不要写额外的外部卡片-->
        <div v-for="product in products" :key="product.id" class="product-card" @click="goToProduct(product.id)">
          <img :src="resolveImage(product)" :title="product.name" class="product-img" />
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="product-price">{{ formatPrice(product.price) }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
//导入
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { resolveProductImage } from '../utils/productCatalog'
import { formatPrice } from '../utils/formatters'
import http from '../utils/http'

//定义变量 -- 控制显示加载中模块，还是显示所有商品模块
//loading=true 显示加载中    loading=false 显示商品模块
const loading = ref(true)

//定义路由，为了路径的跳转
const router = useRouter()

//定义数组，为了存储商品信息
const products = ref([]) //空数组

//解析商品的图片
const resolveImage = (product) => resolveProductImage(product)

//核心：加载商品列表数据
//onMounted是vue的生命周期函数，自动加载执行
onMounted(async()=>{
  //思路：向后端发送请求，获取所有的商品数据，然后显示在该页面中
  try{
    //向服务器端发送get请求
    const res= await http.get('/products')
    //将返回的结果中的data中的商品赋值给products
    products.value = res.data || []
  }catch(e){
    console.error('加载商品失败',e)
  }finally{ 
    loading.value = false
  }
})

const goToProduct =  (id)=>{
  console.log("商品的主键"+id)
  router.push(`/products/${id}`)
}

</script>

<style scoped>
.home { max-width: 1200px; margin: 0 auto; padding: 20px; }
.section { margin-bottom: 40px; }
.section h2 { font-size: 24px; margin-bottom: 8px; }
.section-desc { color: #666; margin-bottom: 20px; }
.loading { text-align: center; padding: 40px; color: #999; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover { transform: translateY(-4px); }
.product-img { width: 100%; height: 160px; object-fit: cover; background:#eee; }
.product-info { padding: 12px; }
.product-info h3 { font-size: 14px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { color: #f56c6c; font-size: 18px; font-weight: bold; }
</style>
