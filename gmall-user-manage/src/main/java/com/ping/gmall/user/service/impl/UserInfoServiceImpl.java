package com.ping.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ping.gmall.bean.UserAddress;
import com.ping.gmall.bean.UserInfo;
import com.ping.gmall.config.RedisUtil;
import com.ping.gmall.service.UserService;
import com.ping.gmall.user.mapper.UserAddressMapper;
import com.ping.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserService{

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }


    public static final String USERKEY_PREFIX = "user:";
    public static final String USERINFOKEY_SUFFIX = ":info";
    public static final int TIMEOUT = 60*60*24;

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        //select * from useraddress where userId =?
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if (info != null){
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(USERKEY_PREFIX+ info.getId()+ USERINFOKEY_SUFFIX,TIMEOUT, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    public UserInfo verify(String userId){
        Jedis jedis = redisUtil.getJedis();
        String key = USERKEY_PREFIX + userId + USERINFOKEY_SUFFIX;
        String userJson = jedis.get(key);
        // 延长时效
        jedis.expire(key,TIMEOUT);
        if (userJson != null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }
}
