<template>
  <div class="checkout page-shell">
    <div class="page-header">
      <div>
        <span class="eyebow">Checkout</span>
        <h1>提交订单</h1>
        <p class="page-subtitle">请选择已维护好的收货地址并核对商品金额，确认无误后提交订单</p>
      </div>
      <div class="checkout-links">
        <router-link to="/cart#address-management">去购物车管理收货地址</router-link>
        <router-link to="/cart" class="link-inline">返回购物车</router-link>
      </div>
    </div>
    <!-- 错误信息 -->
    <p v-if="error" class="state error" style="color:red">{{ error }}</p>
    <!-- 加载中 -->
    <p v-if="loading" class="state">加载中...</p>
    <div v-if="!loading" class="checkout-layout">
      <!-- 左侧：地址选择 + 提交按钮 -->
      <section class="checkout-form">
        <div class="section-head">
          <h2>收货地址</h2>
          <span class="pill pill-info">{{ addresses.length }}条</span>
        </div>
        <!-- 地址为空 -->
        <div v-if="addresses.length === 0" class="empty-card">
          <p>你还没有收货地址，请先去购物车页面的地址管理区域新增地址</p>
          <router-link to="/cart#address-management">去管理收货地址</router-link>
        </div>
        <!-- 地址列表 -->
        <div v-else class="address-list">
          <label
            v-for="addr in addresses"
            :key="addr.id"
            class="address-option"
            :class="{ active: selectedAddressId === addr.id }"
          >
            <input
              type="radio"
              name="address"
              :value="addr.id"
              v-model="selectedAddressId"
              @change="selectAddress(addr.id)"
            />
            <div class="address-option-body">
              <div class="address-option-head">
                <div>
                  <h3>{{ addr.receiver }}</h3>
                  <p>{{ addr.phone }}</p>
                </div>
                <span v-if="addr.isDefault" class="pill pill-success">默认地址</span>
              </div>
              <!-- 改动：用 myFullAddress 拼接省市区+详细地址 -->
              <p>{{ myFullAddress(addr) }}</p>
            </div>
          </label>
        </div>
        <!-- 提交按钮 -->
        <button
          class="btn btn-primary submit-btn"
          type="button"
          :disabled="submitting || addresses.length === 0 || cartItems.length === 0"
          @click="submitOrder"
        >
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </section>
      <!-- 右侧：订单商品列表 -->
      <section class="checkout-summary">
        <div class="summary-head">
          <h2>订单商品</h2>
          <span class="pill pill-info">共{{ totalCount }}件</span>
        </div>
        <!-- 购物车为空 -->
        <div v-if="cartItems.length === 0" class="empty-card">
          <p>购物车为空，暂时无法结算</p>
          <router-link to="/products/list">去购物</router-link>
        </div>
        <!-- 商品列表 -->
        <template v-else>
          <div v-for="item in cartItems" :key="item.id" class="summary-item">
            <div>
              <h3>{{ item.productName }}</h3>
              <p>x{{ item.quantity }} &nbsp;¥{{ item.price.toFixed(2) }}</p>
            </div>
            <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
          <div class="summary-total">
            <span>合计</span>
            <span>¥{{ totalAmount.toFixed(2) }}</span>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../utils/http'
import { alertMessage } from '../utils/modal'

export default {
  name: 'CheckOutView',
  setup() {
    const router = useRouter()
    const cartItems = ref([])          // 购物车的商品列表
    const addresses = ref([])          // 地址列表
    const selectedAddressId = ref(null) // 选中的地址id
    const submitting = ref(false)      // 提交中状态
    const error = ref('')              // 错误信息
    const loading = ref(false)         // 加载中状态

    // 计算属性：商品总件数
    const totalCount = computed(() =>
      cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
    )

    // 计算属性：订单合计金额
    const totalAmount = computed(() =>
      cartItems.value.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0
      )
    )

    // 计算属性：当前选中的地址对象
    const selectedAddress = computed(() =>
      addresses.value.find(addr => addr.id === selectedAddressId.value) || null
    )

    // 新增：拼接完整地址（省+市+区+详细地址）
    const myFullAddress = (address) =>
      `${address.province || ''}${address.city || ''}${address.district || ''}${address.detail || ''}`

    // 加载收货地址列表
    const loadAddresses = async () => {
      try {
        const res = await http.get('/addresses')
        addresses.value = res.data || []
        if (addresses.value.length > 0 && !selectedAddressId.value) {
          selectedAddressId.value = addresses.value[0].id
        }
      } catch (e) {
        error.value = '加载地址失败，请稍后重试'
      }
    }

    // 加载购物车商品
    const loadCartItems = async () => {
      try {
        const res = await http.get('/cart')
        cartItems.value = res.data || []
      } catch (e) {
        error.value = '加载购物车失败，请稍后重试'
      }
    }

    // 选择地址
    const selectAddress = (id) => {
      selectedAddressId.value = id
    }

    // 改动：提交订单，接口改为 /cart/checkout
    const submitOrder = async () => {
      // 前置校验：购物车为空
      if (cartItems.value.length === 0) {
        error.value = '购物车为空，无法提交订单'
        return
      }
      // 前置校验：未选择地址
      if (!selectedAddressId.value) {
        error.value = '请选择收货地址'
        return
      }

      error.value = ''
      submitting.value = true

      try {
        const payload = {
          cartItemIdList: cartItems.value.map(item => item.id),
          addressId: selectedAddressId.value
        }
        // 改动：/order/createFromCart → /cart/checkout
        const res = await http.post('/cart/checkout', payload)
        alertMessage('下单成功！')
        router.push('/order/list')
      } catch (e) {
        error.value = e.response?.data?.message || '提交订单失败，请稍后重试'
      } finally {
        submitting.value = false
      }
    }

    onMounted(() => {
      loading.value = true
      Promise.all([loadAddresses(), loadCartItems()]).finally(() => {
        loading.value = false
      })
    })

    // 暴露给模板使用（新增 myFullAddress）
    return {
      cartItems,
      addresses,
      selectedAddressId,
      selectedAddress,
      submitting,
      error,
      loading,
      totalCount,
      totalAmount,
      selectAddress,
      submitOrder,
      myFullAddress
    }
  }
}
</script>

<style scoped>
.checkout-links { display: flex; gap: 12px; flex-wrap: wrap; }
.checkout-layout { display: grid; grid-template-columns: 1.3fr 1fr; gap: 24px; }
.checkout-form, .checkout-summary {
  padding: 24px; border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px; background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}
.section-head, .summary-head {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; margin-bottom: 18px;
}
.address-list { display: flex; flex-direction: column; gap: 14px; }
.address-option {
  display: flex; gap: 14px; padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px; background: rgba(248, 251, 255, 0.9);
  cursor: pointer;
}
.address-option.active {
  border-color: rgba(79, 124, 255, 0.28);
  box-shadow: 0 12px 28px rgba(79, 124, 255, 0.08);
}
.address-option input { margin-top: 6px; }
.address-option-body { flex: 1; }
.address-option-head {
  display: flex; justify-content: space-between;
  gap: 12px; margin-bottom: 8px;
}
.address-option-body p { color: var(--text-soft); line-height: 1.7; }
.submit-btn { width: 100%; margin-top: 18px; }
.summary-item, .summary-total {
  display: flex; justify-content: space-between; align-items: center;
  gap: 16px; padding: 12px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}
.summary-total { border-bottom: none; margin-top: 8px; font-size: 20px; }
.summary-item h3 { margin-bottom: 6px; }
.summary-item p { color: var(--text-soft); }
@media (max-width: 960px) { .checkout-layout { grid-template-columns: 1fr; } }
</style>
