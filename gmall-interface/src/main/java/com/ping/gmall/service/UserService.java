package com.ping.gmall.service;

import com.ping.gmall.bean.UserAddress;
import com.ping.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    /**
     * 查询所有用户
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户id查询地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);

    /**
     * 登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     *
     * 验证
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
