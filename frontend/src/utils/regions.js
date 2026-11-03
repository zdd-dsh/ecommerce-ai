// src/utils/regions.js
export const REGION_DATA = [
  {
    name: '上海市',
    cities: [
      {
        name: '上海市',
        districts: ['浦东新区', '徐汇区', '静安区', '宝山区', '闵行区']
      }
    ]
  },
  {
    name: '广东省',
    cities: [
      {
        name: '广州市',
        districts: ['天河区', '越秀区', '海珠区', '白云区', '番禺区']
      },
      {
        name: '深圳市',
        districts: ['南山区', '福田区', '宝安区', '龙岗区', '龙华区']
      }
    ]
  },
  {
    name: '浙江省',
    cities: [
      {
        name: '杭州市',
        districts: ['上城区', '拱墅区', '西湖区', '滨江区', '余杭区']
      },
      {
        name: '宁波市',
        districts: ['海曙区', '江北区', '镇海区', '北仑区', '鄞州区']
      }
    ]
  },
  {
    name: '山东省',
    cities: [
      {
        name: '青岛市',
        districts: ['市南区', '市北区', '黄岛区', '崂山区', '城阳区']
      },
      {
        name: '济南市',
        districts: ['历下区', '市中区', '槐荫区', '天桥区', '历城区']
      }
    ]
  },
  {
    name: '北京市',
    cities: [
      {
        name: '北京市',
        districts: ['东城区', '西城区', '朝阳区', '海淀区', '丰台区']
      }
    ]
  },
  {
    name: '江苏省',
    cities: [
      {
        name: '南京市',
        districts: ['玄武区', '秦淮区', '建邺区', '鼓楼区', '栖霞区']
      },
      {
        name: '苏州市',
        districts: ['姑苏区', '吴中区', '相城区', '虎丘区', '吴江区']
      }
    ]
  }
]
/**
 * 根据省份获取城市列表
 * @param provinceName 省份名
 */
export const getCities = (provinceName) => {
  const targetProvince = REGION_DATA.find((province) => province.name === provinceName)
  return targetProvince ? targetProvince.cities : []
}

/**
 * 根据省份名、城市名 获取区县列表
 * @param provinceName 省份名称
 * @param cityName 城市名称
 */
export const getDistricts = (provinceName, cityName) => {
  const targetProvince = REGION_DATA.find(p => p.name === provinceName)
  if (!targetProvince) return []
  const targetCity = targetProvince.cities.find(c => c.name === cityName)
  return targetCity ? targetCity.districts : []
}
