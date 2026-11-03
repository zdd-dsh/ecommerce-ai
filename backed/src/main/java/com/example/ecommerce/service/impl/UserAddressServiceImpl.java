package com.example.ecommerce.service.impl;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.UserAddress;
import com.example.ecommerce.mapper.UserAddressMapper;
import com.example.ecommerce.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    //在业务层，要用到Dao数据访问层，引入UserAddressMapper
    private final UserAddressMapper userAddressMapper;



    public UserAddressServiceImpl(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }
    /**
     * 根据用户id查询所有的地址（当前用户下会有多个地址）
     * @param userId 用户id
     * @return List集合，存储多个地址
     */
    @Override
    public List<UserAddress> getByUserId(Long userId) {
        //直接调用Dao数据访问层，获取数据
        return userAddressMapper.selectByUserId(userId);
    }



    /**
     * 根据主键id查询对应的地址（一个地址）
     * @param id 主键id
     * @return 地址
     */
    @Override
    public UserAddress getById(Long id) {
        //直接调用Dao数据访问层，获取数据
        return userAddressMapper.selectById(id);
    }

    /**
     * 新增用户地址
     * @param userId      用户id
     * @param request     AddressRequest类，收货地址请求的DTO类（收货人、电话、省、市等等）
     * @return            UserAddress类型，表示新增的地址
     */
    @Override
    @Transactional  //事务注解
    public UserAddress create(Long userId, AddressRequest request) {
        //对DTO中的request内容使用trim去掉空格
        UserAddress userAddress=toEntity(userId, request);
        //判断是否设为默认，用户主动指定 或者 当前没有任何默认地址（第一个自动默认）
        if(Boolean.TRUE.equals(request.getIsDefault()) || getDefaultAddress(userId)==null){
            System.out.println("11111111");
            //清除所有旧的默认地址
            userAddressMapper.clearDefaultByUserId(userId);
            //把当前新增地址设置为默认
            userAddress.setIsDefault(1);
        }else {
            System.out.println("00000000");
            userAddress.setIsDefault(0);

        }
        userAddressMapper.insert(userAddress);
        return userAddressMapper.selectById(userAddress.getId());
        //通过Da0数据访问层，查询新增的用户地址，然后返回
    }


    private UserAddress toEntity(Long userId, AddressRequest request){
        //创建UserAddress对象
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setReceiver(request.getReceiver().trim());
        userAddress.setPhone(request.getPhone().trim());
        userAddress.setProvince(request.getProvince().trim());
        userAddress.setCity(request.getCity().trim());
        userAddress.setDistrict(request.getDistrict().trim());
        userAddress.setDetailAddress(request.getDetailAddress().trim());
        return userAddress;
    }






    @Override
    @Transactional
    public UserAddress update(Long userId, Long id, AddressRequest request) {
        //1）校验地址归属权
        UserAddress existing=getOwnedAddress(userId,id);
        System.out.println("existing"+existing);
        //2)DTO 双端空格的校验
        UserAddress address = toEntity(userId, request);
        address.setId(existing.getId());
        System.out.println("address="+address);
        //3)默认地址处理
        if(Boolean.TRUE.equals(request.getIsDefault())){   //判断是否是默认地址
            //显示设为默认地址：先清除其他默认，再设置当前默认
            userAddressMapper.clearDefaultByUserId(userId);
            address.setIsDefault(1);
        }else if(request.getIsDefault() == null){    //修改时没有选择默认地址，保留原状态
            address.setIsDefault(existing.getIsDefault() == null ? 0 : existing.getIsDefault());
        }else{
            //显示传false，取消默认
            address.setIsDefault(0);    //0=false 取消默认
        }
        userAddressMapper.update(address);
        return userAddressMapper.selectById(id);
    }

    @Override
    public void delete(Long userId, Long id) {
        //1）校验地址归属权
        UserAddress existing = getOwnedAddress(userId, id);
        //2）删除地址（Dao数据访问层）
        userAddressMapper.delete(id,userId);
        //3）如果删除的是默认地址，自动将第一个剩余地址设置为默认地址，尽量避免处于"没有默认地址"的状态
        if(existing.getIsDefault() !=null && existing.getIsDefault() == 1){
            //if条件成立，说明要删除的该地址是默认地址
            List<UserAddress> addresses= userAddressMapper.selectByUserId(userId);
            //从集合中再选一个地址设置为默认地址
            if(!addresses.isEmpty()){
                //调用setDefault方法，重新选出一个默认地址
                setDefault(userId, addresses.get(0).getId());
            }
        }
    }


    @Override
    public void setDefault(Long userId, Long id) {
        //1）校验地址归属权
        UserAddress existing=getOwnedAddress(userId, id);
    //2）清除该用户下所有默认地址
        userAddressMapper.clearDefaultByUserId(userId);
    //3）将目标地址设置为默认地址
        existing.setIsDefault(1);
    //调用Dao数据访问层，将修改的数据更新
        userAddressMapper.update(existing);

    }

    @Override
    public UserAddress getOwnedAddress(Long userId, Long id){
        //调用Dao数据访问层，根据主键id查询用户地址
        UserAddress address=userAddressMapper.selectById(id);
        //不存在 或 不属于当前用户
        if(address == null || !userId.equals(address.getUserId())){
            throw new BusinessException(Result.NOT_FOUND_CODE,"收货地址不存在");
        }
        return address;
    }



    /**
     * 查询默认地址
     * @param userId    用户id
     * @return          返回用户地址
     */
    @Override
    public UserAddress getDefaultAddress(Long userId) {
        //调用Dao数据访问层，获取数据
        UserAddress userAddress= userAddressMapper.selectDefaultByUserId(userId);
        return userAddress;
    }

}