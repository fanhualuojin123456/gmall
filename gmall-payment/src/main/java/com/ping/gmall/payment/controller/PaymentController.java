package com.ping.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.ping.gmall.bean.OrderInfo;
import com.ping.gmall.bean.PaymentInfo;
import com.ping.gmall.bean.enums.PaymentStatus;
import com.ping.gmall.payment.config.AlipayConfig;
import com.ping.gmall.service.OrderService;
import com.ping.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;
    @Reference
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;


    @RequestMapping("index")
    public String index(String orderId,HttpServletRequest request){
        // 选中支付渠道！
        // 获取总金额 通过orderId 获取订单的总金额
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        // 保存订单Id
        request.setAttribute("orderId",orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        return "index";
    }

    //http://payment.gmall.com/alipay/submit
    @RequestMapping("alipay/submit")
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        // 获取订单Id
        String orderId = request.getParameter("orderId");
        // 取得订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("-----");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        // 保存信息
        paymentService.savePaymentInfo(paymentInfo);

        // 生成二维码！
        // 参数做成配置文件，进行软编码！
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        // alipay.trade.page.pay
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        // 设置同步回调
//        alipayRequest.setReturnUrl("http://luojin.cn.utools.club/return_url.jsp");
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 设置异步回调
//        alipayRequest.setNotifyUrl("http://luojin.cn.utools.club/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        // 参数
        // 声明一个map 集合来存储参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());
        // 将封装好的参数传递给支付宝！
        alipayRequest.setBizContent(JSON.toJSONString(map));

//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
//        response.getWriter().write(form);//直接将完整的表单html输出到页面
//        response.getWriter().flush();
//        response.getWriter().close();
        // 调用延迟队列
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }
    @RequestMapping("alipay/callback/return")
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type);
        if (!flag){
            return "fail";
        }
        // 判断结束
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            // 查单据是否处理
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(paymentInfo);
            if (paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置创建时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 设置内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                return "success";
            }
        }
        return  "fail";
    }
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        boolean flag = paymentService.refund(orderId);
        System.out.println("flag:"+flag);
        return flag+"";
    }

    // 根据orderId 支付
    @RequestMapping("wx/submit")
    @ResponseBody
    public Map wxSubmit(String orderId){
        // 调用服务层生成数据

        // 调用service服务层判断一下是否可以继续支付
        /*
            boolean flag = paymentSerivce.checkPay(orderId); flag ==true :验证成功，可以支付，false 表示验证失败！不能支付
            if(flag){
                   orderId = UUID.randomUUID().toString().replace("-","");
                   Map map = paymentSerivce.createNative(orderId,"1");
                   System.out.println(map.get("code_url"));
                    // map中必须有code_url
                   return map;
            }else{
                    return new HashMap();
            }
         */
        // IdWorker 自动生成一个Id

        // orderId 订单编号，1 表示金额 分
        orderId = UUID.randomUUID().toString().replace("-","");
        Map map = paymentService.createNative(orderId,"1");
        System.out.println(map.get("code_url"));
        // map中必须有code_url
        return map;
    }

    // http://payment.gmall.com/sendPaymentResult?orderId=102&result=success
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "OK";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult (String orderId){
        // 必须通过orderId 查询paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        // 有out_trade_no
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
        // 该对象中必须有out_trade_no
        boolean result = paymentService.checkPayment(paymentInfoQuery);
        return "" + result;
    }

}
