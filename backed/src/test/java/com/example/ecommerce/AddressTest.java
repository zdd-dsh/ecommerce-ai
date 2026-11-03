package com.example.ecommerce;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.UserAddress;
import com.example.ecommerce.mapper.UserAddressMapper;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.service.UserAddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AddressTest {
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private UserAddressService userAddressService;
    @Test
    public void Test01() {
//        UserAddress userAddress = userAddressMapper.selectById(1L);
//        System.out.println(userAddress);
//        List<UserAddress> list=userAddressMapper.selectByUserId(2L);
//        System.out.println(list);
//        UserAddress userAddress = userAddressMapper.selectDefaultByUserId(1L);
//        System.out.println(userAddress);
//        UserAddress userAddress = new UserAddress();
//        userAddress.setUserId(2L);
//        userAddress.setReceiver("张三");
//        userAddress.setPhone("13822334456");
//        userAddress.setProvince("山东省");
//        userAddress.setCity("青岛市");
//        userAddress.setDistrict("黄岛区");
//        userAddress.setDetailAddress("华鲁中心");
//        userAddress.setIsDefault(0);
//        userAddressMapper.insert(userAddress);
//        System.out.println("插入成功");
        UserAddress userAddress = userAddressMapper.selectById(3L);
        System.out.println(userAddress);


    }
    @Test
    public void Test02() {
        AddressRequest request = new AddressRequest();
        request.setReceiver("  王五  ");
        request.setPhone("  13366669999  ");
        request.setProvince("    山东 ");
        request.setCity("    淄博");
        request.setDistrict("  不知道区2  ");
        request.setDetailAddress("  不知道详细地址2  ");
        request.setIsDefault(true);

        UserAddress userAddress=userAddressService.create( 3L, request);
        System.out.println("新插入的地址为: "+userAddress);

    }

}
