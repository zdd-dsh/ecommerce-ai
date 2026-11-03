//分类常量
export const PRODUCT_CAGEGORIES = [
  {id:1, label:'手机数码'},
  {id:2, label:'电脑办公'},
  {id:3, label:'智能家电'},
  {id:4, label:'居家生活'},
  {id:5, label:'运动户外'},
  {id:6, label:'影音娱乐'}
]

export function getCategoryLabel(categoryId){
  const cat = PRODUCT_CAGEGORIES.find(c => c.id === Number(categoryId))
  return cat ? cat.label : '未分类'
}

export function resolveProductImage(product) {
  if (!product) return getSvgPlaceholder('商品')

  // ❗放弃后端返回的image文件名，直接根据分类返回public真实存在图片
  switch(product.categoryId){
   case 1: //手机数码
    return '/images/images/products/free/digital-smartphone-alt.jpg'
    case 2: //电脑办公
    return '/images/images/products/free/office-laptop-alt.jpg'
    case 3: //智能家电
    return '/images/images/products/free/appliance-airfryer.jpg'
    case 4: //居家生活
    return '/images/images/products/free/home-living-room.jpg'
    case 5: //运动户外
    return '/images/images/products/free/sports-gym.jpg'
    case 6: //影音娱乐
    return '/images/images/products/free/audio-speaker-detail.jpg'
    default:
    return getSvgPlaceholder(product.name)

  }
}

// 内部：生成svg占位图
function getSvgPlaceholder(text){
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(
    `<svg width="336" height="256" xmlns="http://www.w3.org/2000/svg">
    <rect width="336" height="256" rx="28" fill="#E7F0FF"/>
    <text x="168" y="128" text-anchor="middle" fill="#4F7CFF" font-size="20">${text || '商品'}</text>
    </svg>`
  )}`
}
