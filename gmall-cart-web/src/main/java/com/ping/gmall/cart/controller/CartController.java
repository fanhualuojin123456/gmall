package com.ping.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ping.gmall.bean.CartInfo;
import com.ping.gmall.bean.SkuInfo;
import com.ping.gmall.config.CookieUtil;
import com.ping.gmall.config.LoginRequire;
import com.ping.gmall.service.CartService;
import com.ping.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@CrossOrigin
public class CartController {
    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String  addToCart(HttpServletRequest request, HttpServletResponse response){
        // 获取userId，skuId，skuNum
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        String userId = (String)request.getAttribute("userId");
        // 判断用户是否登录
        if (userId != null){
            // 说明用户登录
            cartService.addToCart(skuId,userId, Integer.parseInt(skuNum));
        }else{
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        // 取得sku信息对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        String userId = (String)request.getAttribute("userId");
        if (userId != null){
            // 从cookie中查找购物车
            List<CartInfo> cartListFormCookie = cartCookieHandler.getCartList(request);
            List<CartInfo> cartList = null;
            if (cartListFormCookie != null && cartListFormCookie.size() > 0){
                cartList = cartService.mergeToCartList(cartListFormCookie,userId);
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                // 从redis中取得，或者从数据库中
                cartList = cartService.getCartList(userId);
            }
            request.setAttribute("cartList",cartList);
        }else{
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }

        return "cartList";

    }
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    @RequestMapping("checkCart")
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId=(String) request.getAttribute("userId");
        if (userId!=null){
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }


    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = cartCookieHandler.getCartList(request);
        if (cartList != null && cartList.size() > 0){
            cartService.mergeToCartList(cartList,userId);
            cartCookieHandler.deleteCartCookie(request,response);
        }


        return "redirect://order.gmall.com/trade";



    }



}
