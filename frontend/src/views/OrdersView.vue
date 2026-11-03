<template>
  <div class="orders page-shell">
    <div class="page-header">
      <h1>我的订单</h1>
      <router-link to="/products/list">继续购物</router-link>
    </div>

    <!-- 错误提示 -->
    <p v-if="error" class="state error">{{ error }}</p>

    <!-- 加载中 -->
    <p v-if="loading" class="state">加载中...</p>

    <!-- 订单列表 -->
    <div v-else-if="orders && orders.length > 0" class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <!-- 订单号、订单时间、支付状态 -->
        <div class="order-header">
          <div>
            <h3>订单号：{{ order.orderNo }}</h3>
            <p class="order-time">{{ order.createTime }}</p>
          </div>
          <span class="order-status">{{ statusText(order.status) }}</span>
        </div>

        <!-- 订单金额、收货人、电话、地址 -->
        <div class="order-info">
          <p>订单金额：¥{{ order.totalAmount }}</p>
          <p>收货人：{{ order.receiver }}</p>
          <p>联系电话：{{ order.phone }}</p>
          <p>收货地址：{{ order.address }}</p>
        </div>

        <!-- 按钮 -->
        <div class="order-actions">
          <button
            v-if="order.status === 0"
            class="btn btn-primary compact"
            @click="handlePay(order)"
          >立即支付</button>
          <button
            v-if="order.status === 1"
            class="btn btn-primary compact"
            @click="handleConfirm(order)"
          >确认收货</button>
          <button
            class="btn btn-primary compact"
            @click="handleDetail(order)"
          >查看详情</button>
        </div>
      </article>
    </div>

    <!--没有订单的情况-->
    <div v-else class="empty-card">
      <p>当前还没有订单，先去下单体验一下吧</p>
      <router-link to="/products/list" class="btn btn-primary">去下单</router-link>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../utils/http'
import { getUser } from '../utils/auth'

export default {
  setup() {
    const router = useRouter()
    const user = getUser()

    const orders = ref([])       // 订单列表
    const error = ref('')        // 错误信息
    const loading = ref(false)   // 是否显示加载中

    // 状态映射：0=待支付 1=已支付 2=已完成 3=已取消
    const statusMap = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消' }
    const statusText = (status) => statusMap[status] || '未知'

    // 定义函数：加载所有的订单
    const fetchOrders = async () => {
      // 未登录
      if (!user || !user.id) {
        router.push('/login')
        return
      }

      loading.value = true
      error.value = ''
      try {
        const response = await http.get(`/orders/user/${user.id}`)
        orders.value = response.data || []
      } catch (err) {
        error.value = err.message || '获取订单列表失败'
      } finally {
        loading.value = false
      }
    }

    // 立即支付
    const handlePay = (order) => {
      console.log('立即支付', order.id)
    }

    // 确认收货
    const handleConfirm = (order) => {
      console.log('确认收货', order.id)
    }

    // 查看详情
   const handleDetail = (order) => {
      console.log('点击了查看详情，order:', order)
      console.log('order.id:', order.id)
      router.push(`/orders/${order.id}`)
    }

    


    onMounted(fetchOrders)

    return {
      orders, error, loading,
      statusText,
      handlePay, handleConfirm, handleDetail
    }
  }
}
</script>

<style scoped>
.orders {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
}

.page-header a {
  color: #3b82f6;
  text-decoration: none;
  font-size: 14px;
}

.state {
  text-align: center;
  padding: 20px;
  color: #64748b;
}

.state.error {
  color: red;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  padding-bottom: 16px;
  margin-bottom: 14px;
}

.order-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  word-break: break-all;
}

.order-time {
  color: #64748b;
  margin-top: 6px;
  font-size: 13px;
}

.order-status {
  flex-shrink: 0;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  background: #fef3c7;
  color: #b45309;
}

.order-info p {
  margin: 8px 0;
  color: #64748b;
  font-size: 14px;
}

.order-actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn {
  padding: 8px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: #3b82f6;
  color: #fff;
}

.btn.compact {
  padding: 6px 16px;
  font-size: 13px;
}

.empty-card {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 16px;
}

.empty-card p {
  color: #64748b;
  margin-bottom: 16px;
}

@media (max-width: 720px) {
  .order-header,
  .order-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
