<template>
  <div class="order-detail page-shell">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <p class="page-tag">Order Detail</p>
        <h1>订单详情</h1>
        <p class="page-subtitle">查看订单信息、收货地址与商品明细。</p>
      </div>
      <router-link to="/orders">返回订单列表</router-link>
    </div>

    <div class="detail-layout">
      <!-- 错误提示 -->
      <p v-if="error" class="state error">{{ error }}</p>
      <!-- 加载中 -->
      <p v-if="loading" class="state">加载中...</p>
      <template v-else-if="detail">
        <section class="detail-card">
          <div class="detail-head">
            <div>
              <h3>订单号：{{ detail.order.orderNo }}</h3>
              <p>{{ detail.order.createTime }}</p>
            </div>
            <span>
              {{ statusText(detail.order.status) }}
            </span>
          </div>
          <!--订单信息（收货人、电话、地址、订单金额）-->
          <div class="detail-info">
            <p>收货人：{{ detail.order.receiver }}</p>
            <p>联系电话：{{ detail.order.phone }}</p>
            <p>收货地址：{{ detail.order.address }}</p>
            <p>订单金额：¥{{ detail.order.totalAmount }}</p>
          </div>
          <!--立即支付、确认收货-->
          <div class="detail-actions">
            <button class="btn btn-primary compact">立即支付</button>
            <button class="btn btn-primary compact">确认收货</button>
          </div>
        </section>
        <!--商品明细-->
        <section class="detail-card">
          <h2>商品明细</h2>
          <div v-for="item in detail.orderItems" :key="item.id" class="item-row">
            <div>
              <h3>{{ item.productName }}</h3>
              <p>数量：{{ item.quantity }}</p>
            </div>
            <div class="item-price">¥{{ item.price }}</div>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>
<script>
//引入相关的库或函数
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../utils/http'
import { alertMessage } from '../utils/modal'
export default{
  name: 'OrderDetailView',
  setup(){
    //定义变量
    const route = useRoute()       //获取请求参数
    const detail = ref(null)       //某一个订单详情
    const loading = ref(false)     //是否显示加载中
    const error = ref('')          //错误信息
    //状态映射：0=待支付 1=已支付 2=已完成 3=已取消
    const statusMap = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消' }
    const statusText = (status) => statusMap[status] || '未知'
    //获取订单详情
    const fetchDetail = async () => {
      loading.value = true
      error.value = ''
      try {
        const response = await http.get(`/orders/${route.params.id}/detail`)
        detail.value = response.data
      } catch (err) {
        error.value = err.message || '获取订单详情失败'
      } finally {
        loading.value = false
      }
    }
    //页面加载时执行
    onMounted(fetchDetail)
    //返回
    return{
      route, detail, loading, error, statusText
    }
  }
}
</script>
<style scoped>
/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}
.page-tag {
  font-size: 13px;
  color: #94a3b8;
  margin: 0 0 4px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.page-header h1 {
  font-size: 24px;
  margin: 0;
}
.page-subtitle {
  color: #64748b;
  margin: 6px 0 0;
  font-size: 14px;
}
.page-header a {
  color: #3b82f6;
  text-decoration: none;
  font-size: 14px;
}

.state {
  text-align: center;
  padding: 20px;
  color: var(--text-soft, #64748b);
}
.state.error {
  color: red;
}
.detail-layout { display: flex; flex-direction: column; gap: 20px; }
.detail-card {
  padding: 24px; border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px; background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}
.detail-head {
  display: flex; justify-content: space-between; align-items: center; gap: 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  padding-bottom: 16px; margin-bottom: 14px;
}
.detail-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  word-break: break-all;
}
.detail-head p {
  color: var(--text-soft, #64748b);
  margin-top: 6px;
  font-size: 13px;
}
.detail-info p { margin: 8px 0; color: var(--text-soft); }
.detail-actions {
  margin-top: 18px; display: flex; justify-content: flex-end; gap: 10px;
}
.item-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 0; border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}
.item-row:last-child { border-bottom: none; }
.item-row h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}
.item-row p {
  margin: 4px 0 0;
  color: var(--text-soft, #64748b);
  font-size: 13px;
}
.item-price { font-size: 18px; font-weight: 800; }
@media (max-width: 720px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  .detail-head, .detail-actions, .item-row {
    flex-direction: column; align-items: stretch;
  }
}
</style>
