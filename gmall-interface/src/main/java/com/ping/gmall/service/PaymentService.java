package com.ping.gmall.service;

import com.ping.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    /**
     * 保存支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);
    /**
     * 根据out_trade_no 查询
     * @param paymentInfoQuery
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);
    /**
     *
     * @param out_trade_no
     * @param paymentInfo
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    Map createNative(String orderId, String s);
    /**
     * 发送消息给订单
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    /**
     * 检查outtradeno
     * @param paymentInfoQuery
     * @return
     */
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    /**
     * 延迟队列
     * @param outTradeNo
     * @param i
     * @param i1
     */
    void sendDelayPaymentResult(String outTradeNo, int i, int i1);

    /**
     * 关闭支付订单
     *
     * @param id
     */
    void closePayment(String id);
}
