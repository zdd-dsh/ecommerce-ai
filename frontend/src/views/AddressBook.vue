<template>
  <div :class="embedded ? 'embedded-address-book' : 'page-shell'">
    <div v-if="!embedded" class="page-header">
      <div>
        <span class="eyebrow">Address Book</span>
        <h1>收货地址管理</h1>
        <p class="page-subtitle">维护常用收货人和三级联动地址，结算时可直接选择</p>
      </div>
      <router-link to="" class="link-inline">去结算页</router-link>
    </div>
    <div class="address-layout">
      <!-- 左侧：地址表单 -->
      <section class="address-form-card">
        <h2>{{ editingId ? '编辑地址' : '新增地址' }}</h2>
        <form class="address-form" @submit.prevent="submitAddress">
          <!-- 收货人+手机号（两列） -->
          <div class="form-row">
            <div class="form-group">
              <label>收货人</label>
              <input v-model="form.receiver" type="text" placeholder="请输入收货人姓名" required maxlength="20" @blur="validateField('receiver')"/>
              <p class="field-error">{{ fieldErrors.receiver }}</p>
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input v-model="form.phone" type="tel" placeholder="请输入11位手机号" required maxlength="11" @blur="validateField('phone')"/>
              <p class="field-error">{{ fieldErrors.phone }}</p>
            </div>
          </div>
          <!-- 省市区三级联动 -->
          <div class="form-row area-row">
            <div class="form-group">
              <label>省份</label>
              <select v-model="form.province" required @blur="validateField('province')">
                <option value="">请选择省份</option>
                <option v-for="p in provinces" :key="p.name" :value="p.name">{{ p.name }}</option>
              </select>
              <p class="field-error">{{ fieldErrors.province }}</p>
            </div>
            <div class="form-group">
              <label>城市</label>
              <select v-model="form.city" required @blur="validateField('city')">
                <option value="">请选择城市</option>
                <option v-for="c in cities" :key="c.name" :value="c.name">{{ c.name }}</option>
              </select>
              <p class="field-error">{{ fieldErrors.city }}</p>
            </div>
            <div class="form-group">
              <label>区县</label>
              <select v-model="form.district" required @blur="validateField('district')">
                <option value="">请选择区县</option>
                <option v-for="d in districts" :key="d" :value="d">{{ d }}</option>
              </select>
              <p class="field-error">{{ fieldErrors.district }}</p>
            </div>
          </div>
          <!-- 详细地址 -->
          <div class="form-group">
            <label>详细地址</label>
            <textarea v-model="form.detailAddress" rows="3" placeholder="请输入街道、门牌号等信息" required maxlength="200" @blur="validateField('detailAddress')"></textarea>
            <p class="field-error">{{ fieldErrors.detailAddress }}</p>
          </div>
          <!-- 复选框：是否设为默认 -->
          <label class="checkbox-line">
            <input v-model="form.isDefault" type="checkbox" />
            <span>设置默认地址</span>
          </label>
          <!-- 按钮部分 -->
          <div class="form-actions">
            <button class="btn btn-primary" :disabled="submitting">{{ editingId ? '保存修改' : '新增地址' }}</button>
          </div>
          <p class="error">{{ error }}</p>
        </form>
      </section>

      <!-- 右侧：我的地址 -->
      <section class="address-list-card">
        <div class="section-head">
          <h2>我的地址</h2>
          <span class="pill pill-info">{{ addresses.length }}条</span>
        </div>
        <p v-if="loading" class="state">加载中...</p>
        <div v-else-if="addresses.length === 0" class="empty-card">
          <p>还没有收货地址，请先增加一条</p>
        </div>
        <div v-else>
          <div v-for="item in addresses" :key="item.id" class="address-item">
            <div class="address-item-head">
              <div>
                <h3>{{ item.receiver }}</h3>
                <p>{{ item.phone }}</p>
              </div>
              <span v-if="item.isDefault">默认地址</span>
            </div>
            <p class="address-text">{{ fullAddress(item) }}</p>
            <div class="address-actions">
              <button class="btn btn-ghost compact" @click="editAddress(item)">编辑</button>
              <button class="btn btn-danger compact" @click="delAddress(item.id)">删除</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import http from '../utils/http'
import { REGION_DATA, getCities, getDistricts } from '../utils/regions'
import { confirmDialog } from '../utils/modal'

const createEmptyForm = () => ({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

export default {
  name: 'AddressBookView',
  props: {
    embedded: {
      type: Boolean,
      default: false
    }
  },
  setup() {
    // 1）响应式状态数据
    const addresses = ref([])
    const loading = ref(false)
    const error = ref('')
    const submitting = ref(false)
    const editingId = ref(null)
    const form = ref(createEmptyForm())
    const fieldErrors = reactive({
      receiver: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: ''
    })

    // 2）省市区联动
    const provinces = REGION_DATA
    const cities = computed(() => getCities(form.value.province))
    const districts = computed(() => getDistricts(form.value.province, form.value.city))

    // 3）表单验证
    const phonePattern = /^1\d{10}$/

    const validateField = (field) => {
      switch (field) {
        case 'receiver':
          fieldErrors.receiver = form.value.receiver ? '' : '请输入收货人姓名'
          break
        case 'phone':
          if (!form.value.phone) {
            fieldErrors.phone = '请输入手机号'
          } else if (!phonePattern.test(form.value.phone)) {
            fieldErrors.phone = '手机号格式不正确'
          } else {
            fieldErrors.phone = ''
          }
          break
        case 'province':
          fieldErrors.province = form.value.province ? '' : '请选择省份'
          break
        case 'city':
          fieldErrors.city = form.value.city ? '' : '请选择城市'
          break
        case 'district':
          fieldErrors.district = form.value.district ? '' : '请选择区县'
          break
        case 'detailAddress':
          fieldErrors.detailAddress = form.value.detailAddress ? '' : '请输入详细地址'
          break
      }
    }

    // 整体校验：返回第一个错误信息，没有错误返回空字符串
    const validateForm = () => {
      validateField('receiver')
      validateField('phone')
      validateField('province')
      validateField('city')
      validateField('district')
      validateField('detailAddress')
      for (const msg of Object.values(fieldErrors)) {
        if (msg) {
          return msg
        }
      }
      return ''
    }

    // 工具函数：组合省+市+区+详细地址
    const fullAddress = (address) => `${address.province}${address.city}${address.district}${address.detailAddress}`

    // 重置表单
    const resetForm = () => {
      form.value = createEmptyForm()
      editingId.value = null
      Object.keys(fieldErrors).forEach(k => fieldErrors[k] = '')
    }

    // 加载地址列表
    const fetchAddresses = async () => {
      loading.value = true
      try {
        const res = await http.get('/addresses')
        addresses.value = res.data
      } catch (err) {
        error.value = '加载地址失败'
      } finally {
        loading.value = false
      }
    }

    // 提交保存
    const submitAddress = async () => {
      const errorMsg = validateForm()
      if (errorMsg) return
      submitting.value = true
      error.value = ''
      try {
        const { id, ...payload } = form.value
        if (editingId.value) {
          await http.put(`/addresses/${editingId.value}`, payload)
        } else {
          await http.post('/addresses', payload)
        }
        await fetchAddresses()
        resetForm()
      } catch (err) {
        error.value = err.message || '保存失败'
      } finally {
        submitting.value = false
      }
    }

    // 编辑
    const editAddress = (item) => {
      editingId.value = item.id
      form.value = {
        receiver: item.receiver,
        phone: item.phone,
        province: item.province,
        city: item.city,
        district: item.district,
        detailAddress: item.detailAddress,
        isDefault: item.isDefault
      }
    }

    // 删除
    const delAddress = async (id) => {
      const ok = await confirmDialog('确认删除这条地址吗？')
      if (!ok) return
      await http.delete(`/addresses/${id}`)
      fetchAddresses()
    }

    onMounted(() => {
      fetchAddresses()
    })

    return {
      addresses, loading, error, submitting, editingId,
      form, fieldErrors,
      provinces, cities, districts,
      validateField, fetchAddresses, fullAddress, submitAddress,
      editAddress, delAddress
    }
  }
}
</script>

<style scoped>
.embedded-address-book {
  display: block;
}
.address-layout {
  display: grid;
  grid-template-columns: minmax(340px, 420px) minmax(0, 1fr);
  gap: 24px;
}
.address-form-card,
.address-list-card {
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}
.address-form,
.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.area-row {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
label {
  font-weight: 700;
}
input,
select,
textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(248, 251, 255, 0.92);
}
.checkbox-line {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.checkbox-line input {
  width: auto;
}
.form-actions,
.address-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.field-error {
  margin: 0;
  font-size: 13px;
  color: var(--danger);
}
.address-item {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 20px;
  background: rgba(248, 251, 255, 0.85);
  margin-bottom: 12px;
}
.address-item.active {
  border-color: rgba(22, 163, 74, 0.28);
  box-shadow: 0 10px 24px rgba(22, 163, 74, 0.08);
}
.address-item-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.address-item-head p,
.address-text {
  color: var(--text-soft);
}
.address-text {
  margin-bottom: 14px;
  line-height: 1.7;
}
@media (max-width: 1080px) {
  .address-layout {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 720px) {
  .form-row,
  .area-row {
    grid-template-columns: 1fr;
  }
}
</style>
