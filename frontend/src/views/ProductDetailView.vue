<template>
  <div class="page-shell">
    <!--页面头-->
    <div class="page-head">
      <div>
        <span>Product Detail</span>
        <h1>商品详情</h1>
        <p>确认商品信息与购买数量，加入购物车或直接进入结算。</p>
      </div>
      <router-link to="/products/list">返回商品列表</router-link>
    </div>

    <!--三态互斥：加载中 / 错误 / 商品详情-->
    <p v-if="loading">加载中...</p>
    <p v-if="error" class="text-danger">{{ error }}</p>

    <!--只有product不为null，才渲染商品详情-->
    <div class="product-detail" v-if="product">
      <!--左侧：商品图片-->
      <div class="media-panel">
        <img class="product-image" :src="resolveImage(product)" alt="">
      </div>

      <!--右侧：商品信息-->
      <div class="product-details">
        <!--商品分类-->
        <span class="pill pill-info">{{ getCategoryLabel(product.categoryId) }}</span>
        <!--商品名称-->
        <h2>{{ product.name }}</h2>
        <!--商品价格-->
        <p class="price">¥{{ product.price }}</p>
        <!-- 库存、可加入购物车 -->
        <div class="detail-tags">
          <span class="detail-tag">库存{{ product.stock }}</span>
          <span class="detail-tag soft">可加入购物车</span>
        </div>
        <!--商品的描述-->
        <p class="description">{{ product.description }}</p>
        <!--购买数量和输入框-->
        <div class="quantity-card">
          <label for="quantity">购买数量</label>
          <input id="quantity" type="number" min="1" :max="product.stock" v-model.number="quantity"/>
        </div>
        <!--操作按钮：加入购物车、立即下单-->
        <div class="actions">
            <button class="btn btn-secondary" @click="addToCart">加入购物车</button>
            <button class="btn btn-primary" @click="buyNow">立即下单</button>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
    import { ref, onMounted } from 'vue'
import { useRoute, useRouter} from 'vue-router'
import { isLoggedIn } from '../utils/auth'
import { getCategoryLabel, resolveProductImage } from '../utils/productCatalog'
import { alertMessage } from '../utils/modal'
import http from '../utils/http'
export default{
    name:'ProductDetailView',
    setup(){
        //1、路由设置
        const route = useRoute()
        const router = useRouter()
        console.log(route.params.id)
        //2、响应式状态
        const error = ref('')
        const loading = ref(false)
        const quantity = ref(1)
        const product = ref(null)
        //3、解析商品的图片
        const resolveImage = (product) => resolveProductImage(product)
        //4、请求商品详情
        const fetchProduct = async ()=>{
            loading.value = true
            error.value=''
            try{
                const response = await http.get(`/products/${route.params.id}`)
                product.value = response.data
                console.log(product.value)
            }catch(err){
                error.value = err.message || '获取商品详情失败'
            }finally{
                loading.value=false
            }
        }
        //5、挂载后自动请求
        onMounted(fetchProduct)

        const addToCart = async ()=>{
          console.log(1000)
          //修复：未登录才直接return
          if(!ensureLogin()) return
          if(!product.value){
            await alertMessage('商品信息尚未加载，请稍后再试')
            return
          }
          //将商品添加到购物车中 向服务器端发送请求
          try{
            await http.post('/cart/items',{
              productId:product.value.id,
              quantity:normalizedQuantity()
            })
            await alertMessage('已加入购物车')
          }catch(err){
            await alertMessage(err.message || '加入购物车失败')
          }
        }
        //定义函数：获取商品购买的数量
        const normalizedQuantity = ()=>{
          const current = Number(quantity.value || 1)
          return current > 0 ? current : 1
        }
        //立即下单的函数
        const buyNow = async ()=>{
          //在点击立即下单之前，要先验证是否登录
          if(!ensureLogin()) return
          //没有商品的情况
          if(!product.value){
            await alertMessage('商品信息尚未加载，请稍微再试')
            return
          }
          try{
            //直接点击立即下单，要先将商品添加到购物车中，然后跳转到订单结算页面
            //1、向服务器端发送请求  URL: /api/cart/items  post请求
            //服务器端参数: CartItemRequest request（包含: productId  quantity）
            await http.post('/cart/items',{
              productId:product.value.id,  //商品的id
              quantity:normalizedQuantity()  //获取输入框中输入的商品数量
            })
            //2、跳转到订单结算的页面
            router.push('/checkout')
          }catch(err){
            await alertMessage(err.message || '创建下单信息失败')
          }
        }

        
        const ensureLogin = ()=>{
          if(isLoggedIn()){
            return true
          }else{
            router.push('/login')
            return false
          }
        }
        return{
            loading,
            error,
            product,
            quantity,
            resolveImage,
            getCategoryLabel,
            addToCart,
            buyNow
        }
    }
}

</script>

<style scoped>
/*
 * 详情页采用左右两栏布局：
 * - 左侧 (.media-panel)：商品图片，固定宽度 380px
 * - 右侧 (.product-details)：商品信息，弹性填充剩余空间
 * 960px 以下变为单列布局（上下排列）
 */
.product-detail {
  display: flex;
  gap: 30px;
  padding: 26px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(18px);
  box-shadow: var(--shadow-card);
}
.media-panel {
  flex: 0 0 380px;
  padding: 18px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(79, 124, 255, 0.08), rgba(32, 198, 200, 0.08));
}
.product-image {
  width: 100%;
  height: 380px;
  object-fit: cover;
  border-radius: 18px;
}
.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.product-details h2 {
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.12;
}
.price {
  font-size: 38px;
  font-weight: 800;
  color: var(--danger);
}
.detail-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.detail-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(79, 124, 255, 0.08);
  color: var(--primary-strong);
  font-size: 13px;
  font-weight: 700;
}
.detail-tag.soft {
  background: rgba(32, 198, 200, 0.1);
  color: #0f8d8f;
}
.description { color: var(--text-soft); line-height: 1.85; }
.quantity-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(79, 124, 255, 0.05);
}
.quantity-card label { font-weight: 700; }
.quantity-card input {
  width: 120px;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: white;
}
.actions { display: flex; gap: 12px; margin-top: 8px; }

@media (max-width: 960px) {
  .product-detail { flex-direction: column; }
  .media-panel { flex-basis: auto; }
}
@media (max-width: 720px) {
  .actions { flex-direction: column; }
}
</style>
