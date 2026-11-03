<template>
  <div class="products page-shell">
    <!--页面头部-->
    <div class="page-header">
      <div>
        <span class="eyebrow">All Products</span>
        <h1>全部商品</h1>
        <p class="page-subtitle">支持关键词、分类、价格区间和排序筛选，分页预览更轻松。</p>
      </div>
      <span class="list-meta">共{{pagination.total}}件</span>
    </div>

    <!--筛选区域-->
    <section class="filter-card">
      <div class="filter-grid">
        <input v-model="filters.keyword" type="text" placeholder="搜索商品名称或描述"/>
        <select v-model="filters.categoryId">
            <option value="">全部分类</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{c.label}}</option>
        </select>
        <input v-model.number="filters.minPrice" type="number" min="0" placeholder="最低价"/>
        <input v-model.number="filters.maxPrice" type="number" min="0" placeholder="最高价"/>
        <select v-model="filters.sortBy">
            <option value="latest">最新上架</option>
            <option value="price">价格排序</option>
            <option value="stock">库存排序</option>
            <option value="name">名称排序</option>
        </select>
        <select v-model="filters.sortDirection">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
        </select>
        </div>
        <div class="filter-actions">
        <button class="btn btn-primary compact" @click="fetchProducts(1)">开始筛选</button>
        <button class="btn btn-secondary compact" @click="resetFilter">重置条件</button>
        </div>
    </section>

    <!--状态显示：错误、加载中、空数据（三态互斥）-->
        <p v-if="error" class="state error">{{error}}</p>
        <p v-else-if="loading" class="state">加载中....</p>
        <div v-else-if="!products.length" class="emptyy-card">
        <p>当前筛选条件下暂无商品，试试调整关键词或价格区间</p>
        </div>

        <!--商品网格（全部替换为正常英文短横线 - ）-->
    <div v-else class="product-grid">
    <article v-for="product in products" :key="product.id" class="product-card">
        <div class="product-image-wrap">
        <img class="product-image" :src="resolveImage(product)" :title="product.name" />
        <span class="pill pill-info">xx</span>
        </div>
        <div class="product-content">
        <div class="product-head">
            <h3>{{ product.name }}</h3>
            <span class="stock-tag">库存{{product.stock}}</span>
        </div>
        <p class="description">{{product.description}}</p>
        <div class="product-footer">
            <p class="price">¥{{product.price}}</p>
            <div class="action-buttons">
            <button class="btn btn-primary" @click="goToProduct(product.id)">查看详情</button>
            <!--管理员专属按钮，变量名修正为isAdminUser-->
            <template v-if="isAdminUser">
                <button class="btn btn-secondary compact" @click="editProduct(product.id)">编辑</button>
                <button class="btn btn-danger compact">删除</button>
            </template>
            </div>
        </div>
        </div>
    </article>
    </div>

    <<!--分页器-->
    <div v-if="pagination.totalPages>1" class="pagination-bar">
      <button class="btn btn-secondary compact" @click="changePage(pagination.page-1)">上一页</button>
      <span>第 {{pagination.page}} / {{pagination.totalPages}} 页</span>
      <button class="btn btn-secondary compact" @click="changePage(pagination.page+1)">下一页</button>
    </div>


    </div>
</template>

<script>
//引入类库
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../utils/http'
import { isAdmin } from '../utils/auth'
import { openAdminWindow } from '../utils/appLinks'
import { PRODUCT_CAGEGORIES, resolveProductImage } from '../utils/productCatalog'

/**
 * 工厂函数：创建默认筛选条件对象
 * 为什么在此处使用工厂函数？重置筛选时(重置条件按钮)需要恢复到默认值，工厂函数保证每次都返回完整的对象，避免遗漏字段
 */
const createDefaultFilters = () => ({
  keyword: '',      //搜索关键字
  categoryId: '',   //商品分类
  minPrice: '',     //最低价格
  maxPrice: '',     //最高价格
  sortBy: 'latest', //排序字段（latest默认最新上架，因为根据修改日期降序排列  price价格排序  stock库存排序  name商品名排序）
  sortDirection: 'desc' //排序方向  desc降序  asc升序
})

export default{
  name:'ProductsView',
  setup(){
    //创建路由，方便路径跳转
    const router = useRouter()

    //1、响应式状态
    const products = ref([])         //根据条件分页查询后的商品列表 数组类型 []空数组
    const loading = ref(false)       //加载状态 true显示加载中 false不显示加载中
    const error = ref('')            //错误信息
    const filters = ref(createDefaultFilters()) //筛选条件
    const categories = PRODUCT_CAGEGORIES       //分类列表的常量 {id:1, label:'手机数码'}, {id:2, label:'电脑办公'}, ...
    const pagination = ref({         //分页信息
      page: 1,        //第几页，初始为1，表示第1页
      pageSize: 8,    //每页显示8个商品
      total: 0,       //商品总数
      totalPages: 0   //总页数
    })

    //2、定义计算属性 -- 控制管理员
    const isAdminUser = computed(()=>isAdmin())

    //重置筛选条件
    const resetFilter = ()=>{
      filters.value = createDefaultFilters()
      fetchProducts(1)
    }

    //3、工具函数（路由指定跳转地址、商品图片的处理函数、打开管理员窗口）
    const goToProduct = (id)=>router.push(`/products/${id}`)
    const resolveImage = (product) => resolveProductImage(product)
    //管理员相关
    const editProduct = (productId)=>openAdminWindow(`/products/${productId}/edit`)

    //4、核心方法：获取商品列表（分页查询）
    //page参数，表示第几页 如果page没有传值，给page赋值1
    const fetchProducts = async (page=1) => {
    loading.value=true      //显示加载中
    error.value= ''         //清空错误信息
    try{
        //向后端发送get请求，获取分页查询的商品数据
        //response是接收请求的返回结果
        const response = await http.get('/products/query',{
            params:{
              page,                            //第几页
              pageSize:pagination.value.pageSize,  //每页显示8个商品
              keyword:filters.value.keyword || undefined,       //搜索关键字
              categoryId:filters.value.categoryId || undefined, //商品分类
              minPrice:filters.value.minPrice || undefined,     //最低价格
              maxPrice:filters.value.maxPrice || undefined,     //最高价格
              sortBy:filters.value.sortBy,                     //排序字段
              sortDirection:filters.value.sortDirection       //排序方向
            }
        })
        //判断response.data结果，避免data为null时抛错误
        const pageData = response.data
        products.value=pageData? pageData.items: []

        //展开运算符合并旧数据和新数据，只更新后端返回的字段
        pagination.value ={
          ...pagination.value,
          page: pageData? pageData.page : page,
          total: pageData? pageData.total: 0,
          //小数向上取整  3.1->4   3.7->4     12 / 8
          totalPages: Math.ceil( (pageData ? pageData.total : 0 )/ pagination.value.pageSize)
        }


        }catch(err){
        error.value = err.message || '获取商品列表失败'
        }finally{
        loading.value=false //最后再让加载中隐藏
        }
    }

    onMounted(()=>fetchProducts(1))

    const changePage = (page)=> fetchProducts(page)

    //返回变量或函数（只用return后才可以在页面中使用）
   //返回变量或函数（只用return后才可以在页面中使用）
return{
    products, 
    loading, 
    error, 
    filters, 
    categories, 
    pagination, 
    isAdminUser, 
    goToProduct, 
    resolveImage, 
    editProduct, 
    resetFilter, 
    changePage,
    fetchProducts  // 补上这一行！！
}

  }
}
</script>

<style scoped>
.filter-card {
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}
.filter-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}
.filter-grid input, .filter-grid select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(248, 251, 255, 0.92);
}
.filter-actions, .pagination-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-top: 16px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 20px;
}
.list-meta {
  display: inline-flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(79, 124, 255, 0.08);
  color: var(--primary-strong);
  font-weight: 700;
}
.product-card {
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 40px rgba(15, 23, 42, 0.1);
}
.product-image-wrap { position: relative; padding: 16px 16px 0; }
.product-image-wrap .pill { position: absolute; top: 24px; left: 24px; }
.product-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 18px;
}
.product-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 18px 20px;
}
.product-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.product-head h3 { font-size: 20px; }
.stock-tag {
  flex-shrink: 0;
  padding: 7px 10px;
  border-radius: 999px;
  background: rgba(32, 198, 200, 0.1);
  color: #0f8d8f;
  font-size: 12px;
  font-weight: 700;
}
.description { min-height: 52px; color: var(--text-soft); line-height: 1.75; }
.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.price { color: var(--danger); font-size: 28px; font-weight: 800; }
.action-buttons { display: flex; gap: 8px; flex-wrap: wrap; }
.btn-danger { background: var(--danger, #ef4444); color: white; border: none; }
.btn-danger:hover { background: #dc2626; }
.pagination-bar { justify-content: center; }

@media (max-width: 1100px) {
  .filter-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .filter-grid, .product-footer, .product-head {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
