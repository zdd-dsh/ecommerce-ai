package com.example.ecommerce.controller;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="订单接口", description = "订单创建，查询，状态管理")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<List<Order>> getAll(Authentication authentication){
        //从Authentication对象中提取当前用户信息
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        List<Order> orders= user.isAdmin() ? orderService.getAll() : orderService.getByUserId(user.getId());
        return Result.success(orders);

    }
    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id, Authentication authentication){
        //调用业务层，获取数据
        Order order = orderService.getById(id);
        //校验
        assertOrderAccess(authentication, order);
        return Result.success(order);
    }

    @GetMapping("/{id}/detail")
    public Result<OrderDetailResponse> getDetail(@PathVariable Long id, Authentication authentication){
        //调用业务层，获取数据
        OrderDetailResponse detail = orderService.getDetailById(id);
        assertOrderAccess(authentication, detail.getOrder());
        //返回结果
        return Result.success(detail);
    }
    @GetMapping("/orderNo/{orderNo}")
    public Result<Order> getByOrderId(@PathVariable String orderNo, Authentication authentication){
        //调用业务层，获取数据
        Order order = orderService.getByOrderNo(orderNo);
        //校验
        assertOrderAccess(authentication, order);
        //返回结果
        return Result.success(order);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getByUserId(@PathVariable Long userId, Authentication authentication){
        //获取登录的用户信息
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        //不是管理员登录 并且 登录的用户id和参数中的用户id不一致
        if(!user.isAdmin() && !user.getId().equals(userId)){
            throw new BusinessException(Result.FORBIDDEN_CODE,"无权查看他人订单");
        }
        //调用业务层，获取数据
        List<Order> orders = orderService.getByUserId(userId);
        return Result.success(orders);
    }

    @PostMapping
    public Result<OrderDetailResponse> save(@RequestBody OrderRequest orderRequest, Authentication authentication){
        //获取用户登录信息
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        //判断
        if(orderRequest == null || orderRequest.getOrder() == null){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"Order payload cannot be empty");
        }
        //获取订单
        Order order= orderRequest.getOrder();
        if(!user.isAdmin()){ //不是管理员，是普通用户
            //强制绑定，防伪造  user是登录的用户信息
            order.setUserId(user.getId());
        }else if(order.getUserId() == null){ //提交的订单中没有用户的信息
            order.setUserId(user.getId());
        }
        //调用业务层，获取数据
        OrderDetailResponse resp = orderService.save(order, orderRequest.getOrderItems());
        //返回结果
        return Result.success(resp);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Order order, Authentication authentication){
        //判断订单
        if(order == null || order.getId() == null){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"Order or OrderId is required");
        }
        //调用业务层，获取数据
        Order existing = orderService.getById(order.getId());
        //校验
        assertOrderAccess(authentication, existing);
        //获取登录用户信息
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        //普通用户的条件分支
        if(!user.isAdmin()){
            confirmReceipt(existing,order);  //普通用户只能确认收货
            orderService.update(existing);
            return Result.success(); //返回结果，程序到此结束，不往下执行
        }
        //管理员的情况 -- 可以修改订单
        Order merged = mergeAdminOrderUpdate(existing,order);
        //调用业务层的方法，完成订单的修改
        orderService.update(merged);
        return Result.success();
    }


    //私有方法：普通用户确认收货
    private void confirmReceipt(Order existing, Order request){
        Integer target =request.getTargetStatus() != null ? request.getTargetStatus() : request.getStatus();
        //状态 0=待支付  1=已支付  2=已完成  3=已取消
        if(target == null || target !=2 ){
            throw new BusinessException(Result.FORBIDDEN_CODE,"用户智能确认收货");
        }
        if(existing.getStatus() !=null && existing.getStatus() == 2){ //数据库中查询出来的订单
            return; //到此结束
        }
        if(existing.getStatus() == null || existing.getStatus() !=1){
            throw new BusinessException(Result.CONFLICT_CODE,"只有已支付订单才能确认收货");
        }
        //设置订单状态为已完成
        existing.setStatus(2);
    }

    private Order mergeAdminOrderUpdate(Order existing, Order request){
        Order merged = new Order();
        merged.setId(existing.getId());                //id不可变
        merged.setUserId(existing.getUserId());        //userId不可变
        //request.getOrderNo()为空，使用查询出来的原值，不修改    request.getOrderNo()不为空，前端传该字段，要覆盖
        merged.setOrderNo(request.getOrderNo() == null? existing.getOrderNo() : request.getOrderNo());
        merged.setTotalAmount(request.getTotalAmount() == null? existing.getTotalAmount() : request.getTotalAmount());
        merged.setStatus(request.getStatus() == null? existing.getStatus() : request.getStatus());
        merged.setAddress(request.getAddress() == null? existing.getAddress() : request.getAddress());
        merged.setPhone(request.getPhone() == null? existing.getPhone() : request.getPhone());
        merged.setReceiver(request.getReceiver() == null? existing.getReceiver() : request.getReceiver());
        return merged;
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication){
        //调用业务层，根据订单id查询订单
        Order order = orderService.getById(id);
        //校验
        assertOrderAccess(authentication, order);
        //调用业务层，完成删除的操作
        orderService.delete(id);
        //返回结果
        return Result.success();
    }


    //私有方法：订单校验和用户订单的校验
    private void assertOrderAccess(Authentication authentication, Order order){
        //判断订单是否为空
        if(order == null){
            throw new BusinessException(Result.NOT_FOUND_CODE,"Order does not exist");
        }
        //获取登录用户信息
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        //判断用户和订单的关系校验
        //非管理员，并且处理的不是当前用户的订单
        if(!user.isAdmin() && !user.getId().equals(order.getUserId())){
            //public static final int FORBIDDEN_CODE = 403;   //无权限
            throw new BusinessException(Result.FORBIDDEN_CODE,"无权访问此订单");
        }
    }
    public static class OrderRequest{
        private Order order;
        private List<OrderItem> orderItems;

        public Order getOrder() {
            return order;
        }
        public void setOrder(Order order) {
            this.order = order;
        }
        public List<OrderItem> getOrderItems() {
            return orderItems;
        }
        public void setOrderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
        }
    }


}
