package com.example.ecommerce.service.impl;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.PagedResponse;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.ProductQueryRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.utils.RedisUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);


    private static final long PRODUCT_CACHE_TTL_SECONDS = 3600L;

    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final Map<Integer,String> CATEGORY_LABELS = createCategoryLabels();


    private static final Map<Integer,String> createCategoryLabels(){
        Map<Integer,String> map = new HashMap<Integer,String>();
        map.put(1,"手机数码");
        map.put(2,"电脑办公");
        map.put(3,"智能家电");
        map.put(4,"居家生活");
        map.put(5,"运动户外");
        map.put(6,"影音娱乐");












        return map;

    }

    public ProductServiceImpl(ProductMapper productMapper, RedisUtil redisUtil) {
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
    }


    @Override
    public void warmUpCache() {
        getAll();
    }

    @Override
    public Product getById(Long id) {
        //定义在redis中存储的key
        String key = "product:"+id; //product1  product5  product 8
        try{
            //先判断redis中是否有缓存的单个商品
            //如果redis中有缓存的商品，一定是优先从redis缓存中获取
            if(redisUtil.exists(key)){
                log.info("从缓存中获取商品");
                return objectMapper.readValue(String.valueOf(redisUtil.get(key)), Product.class);
            }

            //调用Dao数据持久层
            Product product=productMapper.selectById(id);
            if(product!=null){
                log.info("从数据库获取商品");
                //将商品数据存入redis中
                redisUtil.set(key, objectMapper.writeValueAsString(product), PRODUCT_CACHE_TTL_SECONDS);
            }
            return product;
        }catch (Exception ex){
            //ex.printStackTrace();//给程序员看
            log.warn("读取单个商品缓存失败:{}",ex.getMessage());
            return productMapper.selectById(id);
        }
    }


    @Override
    public List<Product> getAll() {
        //定义一个Redis，缓存键名key字符串，内容: products:all
        String key ="products:";
        try{
            if(redisUtil.exists(key)){
                log.info("从redis中读取数据");
                return objectMapper.readValue(String.valueOf(redisUtil.get(key)), new TypeReference<List<Product>>() {});
            }
            List<Product> products= productMapper.selectAll();
            if(products != null){
                log.info("存入redis");
                redisUtil.set(key, objectMapper.writeValueAsString(products), PRODUCT_CACHE_TTL_SECONDS);
            }
            return products;
        }catch (Exception ex){
            log.warn("读取商品缓存失败:{}",ex.getMessage());
            return productMapper.selectAll();
        }

    }

    @Override
    public PagedResponse<Product> query(ProductQueryRequest request) {
        log.info("request的值为:{}",request);
        ProductQueryRequest safeRequest = (request == null ? new ProductQueryRequest() : request);

        //业务规则校验，价格区间不能反向
        if(safeRequest.getMinPrice() != null && safeRequest.getMaxPrice() != null
                && safeRequest.getMinPrice().compareTo(safeRequest.getMaxPrice())>0){
            log.info("价格区间校验执行了");
            throw new BusinessException(Result.BAD_REQUEST_CODE,"最低价格不能高于最高价格");
        }

        safeRequest.setPage(safeRequest.getNormalizedPage());
        safeRequest.setPageSize(safeRequest.getNormalizedPageSize());
        safeRequest.setKeyword(safeRequest.getSanitizedKeyword());
        safeRequest.setSortBy(safeRequest.getNormalizedSortBy());
        safeRequest.setSortDirection(safeRequest.getNormalizedSortDirection());

        System.out.println("safeRequest:"+safeRequest);
        List<Product> records= productMapper.selectPage(safeRequest);
        long total = productMapper.countPage(safeRequest);

        //封装统一分页响应 PageResponse  当前页的数据列表、总记录数、当前页、每页条数
        return PagedResponse.of(records, total, safeRequest.getNormalizedPage(), safeRequest.getNormalizedPageSize());
    }


    @Override
    public void save(Product product) {
        Product normalized= normalizeProduct(product,false);
        productMapper.insert(product);
        evictProductCache(null,normalized)  ;
    }
    private void evictProductCache(Product previous, Product current){
        //调用evictCache方法，删除redis缓存中key为products:all的内容
        evictCache("products:all");
        //调用evictCache方法，删除商品详情的中的缓存数据（变更前商品、变更后的商品）
        //变更前商品   previous
        if(previous != null && previous.getId() != null){
            evictCache("product:"+previous.getId());
        }
        //变更后商品   current
        if(current != null && current.getId() != null){
            evictCache("product:"+current.getId());
        }
        // 使用evictCache方法，删除分类列表中的缓存数据（变更前的商品、变更后的商品）
//变更前商品   previous
        if(previous != null && previous.getCategoryId() != null){
            evictCache("products:category:"+previous.getCategoryId()); //redis中key的格式 products:category:4
        }
//变更后商品   current
        if(current != null && current.getCategoryId() != null){
            evictCache("products:category:"+current.getCategoryId());
        }

    }


    /*
     * 安全删除指定缓存键（Redis中）
     */
    private void evictCache(String... keys){
        if(keys == null){
            return; //什么都不做，结束该方法
        }
        for(String key:keys){
            try{
                redisUtil.delete(key);
            }catch (Exception ex){
                log.warn("Failed to evict cache key:{}", key, ex.getMessage());
            }
        }

    }

    private Product normalizeProduct(Product product, boolean required){
        if(product == null){
            throw new IllegalStateException("信息商品不能为空");
        }
        if(required && product.getId() == null){
            throw new IllegalStateException("商品的ID不能为空");
        }

        Product normalized = new Product();
        // ✅关键：把id复制到规范化对象
        normalized.setId(product.getId());
        // ✅merchantId也要复制，不要丢失
        normalized.setMerchantId(product.getMerchantId());

        //商品字段规范化处理
        normalized.setName(requireText(product.getName(), "商品名称不能为空"));
        normalized.setDescription(optionalText(product.getDescription()));
        normalized.setPrice(normalizePrice(product.getPrice()));
        normalized.setStock(normalizeStock(product.getStock()));
        normalized.setImage(optionalText(product.getImage()));
        normalized.setCategoryId(normalizeCategoryId(product.getCategoryId()));
        normalized.setStatus(normalizeStatus(product.getStatus()));

        //注意：merchantId由商家中心模块在调用时设置，此处不强制校验
        return normalized;
    }


    private  String requireText(String value, String message){
        //调用optionalText()方法，去掉内容中的两端空格
        String normalized = optionalText(value);
        if(normalized == null || normalized.isEmpty()){
            throw new IllegalStateException(message);
        }
        return normalized;
    }


    /*
     * 对字符串文本内容进行规范化处理
     * 如果value的值为null，返回null，如果value的值不为null，去掉value的值的两端空格
     * value=null   value->null
     * value=" zhangsan "  value->"zhangsan"
     * @param value  字符串文本内容
     * @return       规范化后的字符串
     */
    public String optionalText(String value){
        if(value == null){
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
    private BigDecimal normalizePrice(BigDecimal price){
        //1、非空
        if(price == null){
            throw new IllegalStateException("商品价格不能为空");
        }
        //2、非负
        if(price.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalStateException("商品价格不能小于0");
        }
        //3、保留2位小数  参数1：小数点后保留2位  参数2：HALF_UP 四舍五入
        return price.setScale(2, RoundingMode.HALF_UP);
    }
    private Integer normalizeStock(Integer stock){
        //1、非空
        if(stock == null){
            throw new IllegalStateException("商品库存不能为空");
        }
        //2、非负
        if(stock < 0){
            throw new IllegalStateException("商品库存不能小于0");
        }
        return stock;
    }
    private Integer normalizeCategoryId(Integer categoryId){
        if(categoryId == null || !CATEGORY_LABELS.containsKey(categoryId)){
            throw new IllegalStateException("商品分类无效");
        }
        return categoryId;
    }
    private Integer normalizeStatus(Integer status){
        int normalized = status == null ? 1 : status;
        if(normalized != 0 && normalized !=1){
            throw new IllegalStateException("商品状态无效");
        }
        return normalized;
    }



    @Override
    @Transactional //事务注解 保证全部成功或全部撤回，避免部分导入导致数据不一致
    public int importProducts(List<Product> products) {
        //List<Product> ps1;  //这种情况ps1=null
        //List<Product> ps2 = new ArrayList<Product>();  //这种情况ps2.isEmpty是空
        if(products == null || products.isEmpty()){
            throw new IllegalStateException("没有可导入的商品数据");
        }

        //创建一个空集合
        List<Product> normalizedProducts = new ArrayList<>();
        //遍历products，将products要批量导入的商品先进行规范化处理，并重新放入normalizedProducts集合中
        for(Product p:products){
            //将遍历的每一个商品p进行规范化处理，并将规范化处理后的商品放入新的集合normalizedProducts
            normalizedProducts.add(normalizeProduct(p, false));
        }

        //将新集合中的商品（规范化处理后的）批量的插入数据库中（交给Dao数据持久层）
        for(Product product:normalizedProducts){
            //调用Dao数据持久层，将商品插入
            productMapper.insert(product);
        }
        evictProductCachesForBatch(normalizedProducts);
        return normalizedProducts.size();
    }
    private void evictProductCachesForBatch(List<Product> products){
        //调用evictCache方法，删除redis缓存中key为products:all的内容
        evictCache("products:all");

        //清除category_id在redis中的缓存数据
        if(products !=null){
            //创建一个Set集合（特点：不能存储重复数据） 存储不重复的category_id的值（因为数据库中category_id有重复的数据）
            Set<Integer> categoryIds = new HashSet<Integer>();
            //使用for循环遍历products集合，取出每一个商品
            for(Product product:products){
                if(product != null && product.getCategoryId() != null){
                    //获取category_id，放入categoryIds集合中（过滤重复）
                    categoryIds.add(product.getCategoryId());
                }
            }
            //遍历categoryIds集合，调用evictCache方法，让其失效缓存
            for(Integer categoryId:categoryIds){
                evictCache("products:category:"+categoryId);
            }
        }
    }



    @Override
    @Transactional
    public void update(Product product) {
        //判断product是否为空null，如果为null，直接返回null; 如果不为null，返回商品的主键Id
        //判断可以使用if语句完成，也可以使用三目运算符完成
    /*
    Long productId;
    if(product == null){
        productId = null;
    }else{
        productId = product.getId();
    }
    */
        Long productId = (product == null ? null : product.getId());
        //判断productId为null，返回null; 如果productId不为空，调用Dao数据访问层，根据Id查询商品
        //说明：previous 更改前的商品（也可以理解为直接从数据库中查询出来的商品）
        Product previous = (productId == null ? null : productMapper.selectById(productId));

        //将参数的product商品进行规范化处理
        //true表示还要验证商品的id是否为null
        //说明：normalizeProduct 是规范化以后更改后的商品（要修改的商品）
        Product normalizeProduct = normalizeProduct(product, true);

        //调用Dao数据持久层，将要修改的商品（normalizeProduct）进行修改操作
        productMapper.update(normalizeProduct);

        //失效缓存
        evictProductCache(previous, normalizeProduct);
    }

    @Override
    public void delete(Long id) {
        //1、调用Dao数据持久层，根据id完成删除的操作
        Product product = productMapper.selectById(id);  //根据id查询商品
        productMapper.delete(id);   //根据id删除商品
        //2、清除缓存redis中的数据（保证redis中的缓存数据和数据库中的数据的一致性）
        //参数1：变更前的商品    参数2：变更后的商品
        evictProductCache(product, null);
    }

}
