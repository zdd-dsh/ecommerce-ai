package com.example.ecommerce;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.mapper.CartItemMapper;
import com.example.ecommerce.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CartTest {
    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private CartService cartService;
    @Test
    void Test01(){
//        CartItem Item = cartItemMapper.selectById(2L);
//        System.out.println(Item);

//        CartItem item = cartItemMapper.selectByUserAndProduct(8L, 3L);
//        System.out.println(item);

//        List<CartItemResponse> list=cartItemMapper.selectCartByUserId(2L);
//        System.out.println(list);

//            CartItem cartItem = new CartItem();
//            cartItem.setUserId(1L);
//            cartItem.setProductId(6L);
//            cartItem.setQuantity(1);
//            int i=cartItemMapper.insert(cartItem);
//            System.out.println(i);

        cartItemMapper.updateQuantity(3L,1L,2);

    }
    @Test
    void Test02(){
//        List<CartItemResponse> resp =cartService.getCart(2L);
//        System.out.println(resp);
        Long userId = 2L;                //数据库中有用户id为2的用户
        CartItemRequest request =new CartItemRequest();
        request.setProductId(3L);        //数据库中商品id为3
        request.setQuantity(5);          //加入购物车商品数量
        cartService.addItem(userId,request);

    }
    @Test
    void test03(){
        Long userId = 3L;
        CartItemRequest request =new CartItemRequest();
        request.setProductId(3L);
        request.setQuantity(5);
        cartService.addItem(userId,request);
    }
    @Test

    void test04(){
        CartItemRequest request =new CartItemRequest();
        request.setProductId(6L);
        request.setQuantity(2);
        cartService.updateItem(1L, 3L, request);
    }
    @Test
    void test05(){
        cartService.removeItem(16L, 5L);

    }

}
