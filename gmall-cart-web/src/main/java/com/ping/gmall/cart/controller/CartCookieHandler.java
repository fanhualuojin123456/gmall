package com.ping.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ping.gmall.bean.CartInfo;
import com.ping.gmall.bean.SkuInfo;
import com.ping.gmall.config.CookieUtil;
import com.ping.gmall.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String cookieName = "CART";
    // 设置cookie 过期时间
    private int cookie_cart_maxAge = 7*24*3600;

    @Reference
    private ManageService manageService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId,String userId, Integer skuNum) {
        //判断cookie中是否有购物车 有可能有中文，所有要进行序列化
        String cookieValue = CookieUtil.getCookieValue(request, cookieName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist = false;
        if (cookieValue != null){
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist = true;
                    break;
                }
                
            }

        }
        // //购物车里没有对应的商品 或者 没有购物车
        if (!ifExist){
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());

            cartInfoList.add(cartInfo);

        }
        // 把购物车写入cookie
        String newCartStr = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,cookieName,newCartStr,cookie_cart_maxAge,true);


    }

    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cookieValue = CookieUtil.getCookieValue(request, cookieName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
        return cartInfoList;


    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request,response,cookieName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        List<CartInfo> cartList = getCartList(request);
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 保存到cookie
        CookieUtil.setCookie(request,response,cookieName, JSON.toJSONString(cartList),cookie_cart_maxAge,true);




    }
}
