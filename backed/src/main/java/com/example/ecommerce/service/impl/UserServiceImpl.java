package com.example.ecommerce.service.impl;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

//Service业务类
//💡 强调：业务层用到的数据，要通过数据持久层来获取（MyBatis -- UserMapper）
//        * 该类是UserService接口的实现子类
@Service//将UserServiceImpl交给Spring框架管理
public class UserServiceImpl implements UserService {

    //定义常量 正则表达式 电话号和用户名
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    // 修改后：允许大小写字母、数字、下划线
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    @Autowired //自动装配的注解，从spring框架中或UserMapper
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;//SpringBoot提供的crypt加密算法

    @Override
    public User getById(Long id) {
        //调用数据持久层的代码获取数据
        User user = userMapper.selectById(id);
        //安全：清除密码的操作
        if (user == null) {
            user.setPassword(null);
        }
        return user;//返回User用户信息，看不到密码
    }

    @Override
    public User getByUsername(String username) {
        //调用数据持久层，根据用户名获取用户信息
        User user = userMapper.selectByUsername(username);
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = userMapper.selectAll();
        for (User u:users) {
            u.setPassword(null);
        }
        return users;

    }

    @Override
    public void save(User user) {
        if(user==null || user.getUsername()==null || user.getPassword()==null){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"用户名或密码是必填项，不能为空");
        }
        //调用用户信息校验的方法
        validateUserField(user);
        //判断用户的状态和用户的角色
        if(user.getStatus()==null){
            user.setStatus(1);  //状态 1=启用  0=禁用  默认值是1启动状态
        }
        if(user.getRole()==null){
            user.setRole("user");  //user角色   admin管理员角色
        }
        //设置密码加密
        user.setPassword(encodeIfNecessary(user.getPassword()));
        //调用持久层的Mapper，让持久层的Mapper完成数据的插入操作
        userMapper.insert(user);
    }
    //用户信息校验
    private void validateUserField(User user) {
        String username = user.getUsername()==null ? "" : user.getUsername().trim();
        //校验用户名
        if(!USERNAME_PATTERN.matcher(username).matches()) {//用户名不匹配正则表达式
            //抛出异常
            throw new BusinessException(Result.BAD_REQUEST_CODE,"用户名必须是4-20位英文数字下划线构成");

        }
        // 校验手机号
        if(user.getPhone()!=null && !user.getPhone().isEmpty() && !PHONE_PATTERN.matcher(user.getPhone()).matches()){
            // 抛出异常
            throw new BusinessException(Result.BAD_REQUEST_CODE,"手机号格式不正确");
        }

    }

    @Override
    public void update(User user) {
        //user为空的情况  update user set xx=xx, xx=xx where id=??
        if(user == null || user.getId()==null){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"用户不能为空或用户id为必填项");
        }
        //根据用户的id从数据库将用户信息查询出来，然后在此基础上修改用户信息（Dao持久层）
        User existing = userMapper.selectById(user.getId());
        if(existing ==null){
            throw new BusinessException(Result.NOT_FOUND_CODE,"User does not exist");
        }
        //调用mergeMissingFields
        mergeMissingFields(user,existing);
        //校验用户名和手机号的合法性
        validateUserField(user);
        //判断用户信息中密码是否为空
        if(user.getPassword() == null || user.getPassword().isEmpty()){
            user.setPassword(existing.getPassword());
        }else{ //要修改的User信息有密码，进行加密
            user.setPassword(encodeIfNecessary(user.getPassword()));
        }
        userMapper.update(user);
    }

    private void mergeMissingFields(User user, User existing){
        if(user.getUsername()==null || user.getUsername().isEmpty()){
            //不修改用户名，要设置的用户名和数据库中的用户名保持一致
            user.setUsername(existing.getUsername());
        }
        if(user.getNickname()==null){
            user.setNickname(existing.getNickname());
        }
        if(user.getEmail()==null){
            user.setEmail(existing.getEmail());
        }
        if(user.getPhone()==null){
            user.setPhone(existing.getPhone());
        }
        if(user.getRole()==null){
            user.setRole(existing.getRole());
        }
        if(user.getStatus()==null){
            user.setStatus(existing.getStatus());
        }
    }
    @Override
    public void updatePassword(Long id, String encodePassword) {
       encodePassword =  encodeIfNecessary(encodePassword);

        userMapper.updatePassword(id, encodePassword);
    }

    @Override
    public void delete(Long id) {
        userMapper.delete(id);
    }

    @Override
    public String encodeIfNecessary(String rawPassword) {
        // 判断如果密码是否是空值，就不加密了
        if(rawPassword == null || rawPassword.isEmpty()){
            return rawPassword;
        }
        // 判断如果密码已经加密过了，不再重复加密  $2a$  $2b$  $2y$开头的密码，都是经过BCrypt加密算法的固定前缀
        if(rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$") || rawPassword.startsWith("$2y$")){
            return rawPassword;
        }

        return passwordEncoder.encode(rawPassword);
    }
    @Override
    public User updateNickname(Long userId, String nickname) {
        //原生mybatis 使用userMapper.selectById 查询，不是this.getById
        User dbUser = userMapper.selectById(userId);
        if(dbUser == null){
            //项目统一抛BusinessException，不是RuntimeException
            throw new BusinessException(Result.NOT_FOUND_CODE,"用户不存在");
        }
        //调用mapper自定义方法更新昵称
        userMapper.updateNickname(userId, nickname);
        //查询更新后的用户
        User updateUser = userMapper.selectById(userId);
        //清除密码，和其他接口逻辑保持一致
        if(updateUser != null){
            updateUser.setPassword(null);
        }
        return updateUser;
    }
    @Override
    public User updateEmail(Long userId, String email) {
        //1. 根据id查询当前登录用户
        User user = userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        //3. 执行更新，只修改email、update_time
        userMapper.updateEmail(userId, email);

        //4. 查询更新后的数据
        User updateUser = userMapper.selectById(userId);
        //密码置null，防止泄露
        updateUser.setPassword(null);
        return updateUser;
    }
    @Override
    public User updatePhone(Long userId, String phone) {
        //校验用户是否存在
        User dbUser = userMapper.selectById(userId);
        if(dbUser == null){
            throw new BusinessException(Result.NOT_FOUND_CODE,"用户不存在");
        }
        //校验手机号格式（复用类中已有的PHONE_PATTERN）
        if(!PHONE_PATTERN.matcher(phone).matches()){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"手机号格式不正确");
        }
        //校验手机号不能被其他用户占用
        User existUser = userMapper.selectByPhone(phone);
        if(existUser != null && !existUser.getId().equals(userId)){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"该手机号已被其他用户占用");
        }
        //执行更新
        userMapper.updatePhone(userId, phone);
        //查询更新后用户，清空密码
        User updateUser = userMapper.selectById(userId);
        if(updateUser != null){
            updateUser.setPassword(null);
        }
        return updateUser;
    }
}
