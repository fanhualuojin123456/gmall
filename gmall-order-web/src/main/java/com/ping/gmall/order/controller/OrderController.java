package com.ping.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ping.gmall.bean.*;
import com.ping.gmall.bean.enums.OrderStatus;
import com.ping.gmall.bean.enums.ProcessStatus;
import com.ping.gmall.config.LoginRequire;
import com.ping.gmall.service.CartService;
import com.ping.gmall.service.ManageService;
import com.ping.gmall.service.OrderService;
import com.ping.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

//    @RequestMapping("trade")
//    public String trade(){
//        //返回一个视图名称index.html
//        return "index";
//    }
//    @Autowired
    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;
    @Reference
    private ManageService manageService;
    @Reference
    private OrderService orderService;

    //    @ResponseBody
    @RequestMapping("trade" )
    @LoginRequire
    public String trade(HttpServletRequest request){
//        return userService.getUserAddressList(userId);
        String userId = (String) request.getAttribute("userId");
        // 得到选中的购物车列表
        List<CartInfo> cartCheckedList  = cartService.getCartCheckedList(userId);
        // 收货人地址
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);
        // 订单信息集合
        List<OrderDetail> orderDetailList = new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        // 调用计算总金额的方法  {totalAmount}
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";

    }



    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        

        // 判断是否是重复提交
        // 先获取页面的流水号
        String tradeNo = request.getParameter("tradeNo");
        // 调用比较方法
        boolean result = orderService.checkedTradeNo(userId,tradeNo);
        if (!result) {
            request.setAttribute("errMsg","订单已提交，不能重复提交！");
            return "tradeFail";
        }
        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        // 校验，验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 从订单中去购物skuId，数量
            boolean flag = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            if (!flag){
                request.setAttribute("errMsg","商品库存不足，请重新下单！");
                return "tradeFail";
            }
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            int res = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());
            if (res != 0){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"价格不匹配");
                cartService.loadCartCache(userId);
                return "tradeFail";
            }

        }


        // 保存
        String orderId = orderService.saveOrder(orderInfo);
        orderService.deleteTradeNo(userId);

        // 重定向
        return "redirect://payment.gmall.com/index?orderId=" + orderId;


    }

    // http://order.gmall.com/orderSplit?orderId=xxx&wareSkuMap=xxx
    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        //返回的是子订单集合
        List<OrderInfo> orderInfoList = orderService.orderSplit(orderId,wareSkuMap);
        // 创建一个集合 来存储map
        ArrayList<Map> mapArrayList = new ArrayList<>();
        // 循环遍历
        for (OrderInfo orderInfo : orderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            mapArrayList.add(map);

        }
        return JSON.toJSONString(mapArrayList);
    }


}

