package com.ping.gmall.service;

import com.ping.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    /**
     * 添加购物车
     * @param skuId
     * @param userId
     * @param i
     */
    void addToCart(String skuId, String userId, Integer skuNum);

    /**
     * 获取购物车列表
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * cookie合并redis
     * @param cartListFormCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFormCookie, String userId);

    /**
     * 购物车选中
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 得到选中购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * 保存数据
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);
}
