<template>
    <div class="cart page-shell">
        <!-- 页面头部区域 -->
        <div class="page-header">
            <div>
                <span class="eyebrow">Cart </span>
                <h2>购物车</h2>
                <p>核对商品信息，并在本页直接进入地址管理</p>
            </div>
            <button v-if="cartItems.length" class="btn btn-secondary compact" @click="clearCart()">清空购物车</button>
        </div>
        <!--错误信息、加载中、两栏布局和购物车为空4部分是互斥-->
        <p v-if="error" class="state error">{{error}}</p>
        <p v-else-if="loading" class="state">加载中...</p>
        <!-- 购物车所有商品的两栏布局 -->
         <div v-else-if="cartItems.length >0 " class="cart-layout">
             <!-- 左侧商品列表 -->
              <section class="cart-items">
                  <article v-for="item in cartItems" class="cart-item" :key="item.id">
                      <img :src="item.image" :alt="item.productName" class="item-image" />
                      <div class="item-info">
                          <h3>{{ item.productName }}</h3>
                          <p class="price">￥ {{ formatPrice(item.price) }}</p>
                          <p class="stock">库存{{item.stock}}</p>
                      </div>
                      <!--数量展示（带数量增减按钮） -->
                      <div class="item-quantity">
                        <button class="qty-btn" @click="updateQuantity(item,item.quantity-1)"
                        :disabled="item.quantity <=1">-</button>
                        <span>{{ item.quantity }}</span>
                        <button class="qty-btn" @click="updateQuantity(item,item.quantity+1)"
                        :disabled="item.quantity >= item.stock">+</button>
                      </div>
                      <div class="item-total">￥{{ formatPrice(item.subtotal) }}</div>
                      <button class="btn btn-danger compact remove-btn" @click="removeItem(item.id)">删除</button>
                  </article>
              </section>
             <!-- 右侧订单预览 -->
              <aside class="cart-summary">
                  <span>Summary</span>
                  <h2>订单预览</h2>
                  <p>确认商品数量和合计金额。</p>
                  <div class="summary-row">
                      <span>商品数量</span>
                      <strong>{{cartItems.length}}件</strong>
                  </div>
                  <div class="summary-row total">
                      <span>合计</span>
                      <strong>￥{{formatPrice(totalPrice)}}</strong>
                  </div>
                  <button class="btn btn-primary checkout-btn" @click="goCheckout">去结算</button>
              </aside>
         </div>
         <!-- 购物车为空 -->
          <div v-else class="empty-card">
            <br/>
            <br/>
            <p style="color: red;">购物车还是空的，先去挑选几件喜欢的商品吧</p>
            <router-link to="/products/list" class="btn btn-primary" style="color: red;">去购物</router-link>
          </div>
          <!-- 嵌入收获地址管理 -->
           <!-- 嵌入收获地址管理 -->
          <section class="address-entry-card">
              <div>
                  <span class="eyebrow">Address</span>
                  <h2>收货地址管理</h2>
                  <p>地址管理已整合到购物车页，点击下方超链接即可展开</p>
              </div>
              <a class="link-inline" @click="toggleAddressManager">
                  {{ showAddressManager ? '收起管理地址' : '点击显示管理地址' }}
              </a>

          </section>
          <section v-if="showAddressManager" class="address-embed-section" id="address-management">
            <AddressBook embedded />
          </section>
    </div>
</template>
<script>
  //引入资源文件
  import { resolveProductImage } from '../utils/productCatalog'
  import {ref,computed,onMounted} from 'vue'
  import {useRoute,useRouter} from 'vue-router'
  import http from '../utils/http'
  import {alertMessage,confirmDialog} from '../utils/modal'
  import {formatPrice} from '../utils/formatters'
  import AddressBook from './AddressBook.vue'
  export default{
    name:'CartView',
    components:{ AddressBook }, // ✅注册子组件
    setup() {
      //定义route （获取请求参数）和router（路由地址跳转）
      const route = useRoute()
      const router = useRouter()
      //1）响应式状态
      const cartItems =ref([])//购物车中的商品、数组类型[]
      const loading = ref(false)//是否显示加载中 true显示加载中 false不显示加载中
      const error = ref('')//显示错误信息
      const showAddressManager = ref(false)// ✅修复 ture → true
      //2)核心：获取购物车中的商品
      const fetchCart=async()=>{
        loading.value = true  //先显示加载中...
        error.value = ''    //先清空错误信息
        try{
          const response = await http.get('/cart')
          cartItems.value=response.data || []
        }catch(err){
          error.value=err.message || '获取购物车商品失败'
        }finally{
          loading.value = false   //最后购物车中的商品正常显示后，再隐藏加载中
        }
      }
      //3) 定义vue的生命周期函数（自动加载执行）
      onMounted(async()=> {
        //调用函数 获取购物车商品
        await fetchCart()
        //页面带#address‑manawgement锚点自动展开地址面板
        if(route.hash === '#address-management'){
          showAddressManager.value = true
        }

      })

      //地址管理切换
      const toggleAddressManager = async ()=>{
        //通过改变showAddressManager的true或false来控制地址的显示和隐藏
        showAddressManager.value = !showAddressManager.value
      }


      //4)计算总价
      const totalPrice= computed(()=>{
        //total变量用于累加求和
        return cartItems.value.reduce((total,item)=>{
          return total +Number(item.subtotal)
        },0)
      })
      //跳转到结算页面
      const goCheckout = ()=>{
        router.push('/checkout')
      }
     const updateQuantity= async (item, quantity)=>{
          console.log('新数量:', quantity)
          // 下限：不能小于1
          if(quantity <= 0){
              return
          }
          // 上限：只有增加时才校验库存（减号不受限，处理脏数据）
          if(quantity > item.quantity && quantity > item.stock){
              await alertMessage('超出库存上限')
              return
          }
          try{
              await http.put(`/cart/items/${item.id}`, {
                  productId: item.productId,
                  quantity: quantity
              })
              // ✅ 关键：请求成功后重新拉购物车数据，刷新页面
              await fetchCart()
          }catch(err){
              console.error(err)
              await alertMessage(err.response?.data?.message || '更新数量失败')
          }
      }
      const removeItem= async (id)=>{
       try{
        await http.delete(`/cart/items/${id}`)
        await fetchCart()
       }catch(err){
        await alertMessage(err.message || '删除商品失败')
       }
      }
      
      const clearCart= async ()  =>{
        const confirmed = await confirmDialog('确定要清空购物车吗')
        if(!confirmed){
          return
        }
        try{
          await http.delete(`/cart`)
          await fetchCart()
        }catch(err){
          await alertMessage(err.message || '清空购物车失败')
        }
      }
      //返回变量或函数
      return{
        cartItems,loading,error,showAddressManager,formatPrice,totalPrice, resolveProductImage, goCheckout, updateQuantity
        ,removeItem, clearCart, toggleAddressManager
      }
    }
  }
</script>
<style scoped>
/*
 * 【购物车页面样式】
 *
 * 主布局 (.cart-layout)：
 * - 左侧商品列表：1.7fr（占据更多空间）
 * - 右侧订单摘要：0.9fr（较窄）
 * - 960px 以下变为单列
 *
 * 购物车项 (.cart-item)：
 * - 水平排列：图片 + 信息 + 数量 + 小计 + 删除按钮
 * - 720px 以下变为垂直排列
 */
.cart {
  max-width: 1180px;
  margin: 0 auto;
}
.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(280px, 0.9fr);
  gap: 20px;
}
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}
.item-image {
  width: 92px;
  height: 92px;
  object-fit: cover;
  border-radius: 16px;
}
.item-info {
  flex: 1;
}
.item-info h3 {
  margin-bottom: 8px;
}
.price {
  color: var(--danger);
  font-weight: 800;
  margin: 6px 0;
}
.stock {
  color: var(--text-soft);
  font-size: 14px;
}
.item-quantity {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  background: rgba(79, 124, 255, 0.06);
}
.qty-btn {
  width: 34px;
  height: 34px;
  border: 1px solid var(--line);
  border-radius: 50%;
  background: white;
  cursor: pointer;
}
.qty-btn:disabled {
  color: #9aa4b2;
}
.item-total {
  width: 130px;
  text-align: right;
  font-size: 18px;
  font-weight: 800;
}
.remove-btn {
  flex-shrink: 0;
}
.cart-summary,
.address-entry-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px;
  height: fit-content;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}
.cart-summary h2,
.address-entry-card h2 {
  font-size: 28px;
}
.cart-summary p,
.address-entry-card p {
  color: var(--text-soft);
  line-height: 1.75;
}
.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}
.summary-row.total {
  margin-bottom: 6px;
  font-size: 20px;
}
.address-entry-card {
  margin-top: 24px;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.address-entry-card .link-inline {
  flex-shrink: 0;
}
.address-embed-section {
  margin-top: 24px;
}
@media (max-width: 960px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }
  .address-entry-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
@media (max-width: 720px) {
  .cart-item {
    flex-direction: column;
    align-items: stretch;
  }
  .item-total {
    width: auto;
    text-align: left;
  }
}
</style>
