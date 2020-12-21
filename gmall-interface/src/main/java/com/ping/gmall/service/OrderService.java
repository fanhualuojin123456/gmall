package com.ping.gmall.service;

import com.ping.gmall.bean.OrderInfo;
import com.ping.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 获取流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 对比流水号
     * @param userId
     * @param tradeNo
     * @return
     */
    boolean checkedTradeNo(String userId, String tradeNo);

    /**
     *  删除流水号
     * @param userId
     */
    void deleteTradeNo(String userId);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 查询订单
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     *新订单状态
     * @param orderId
     * @param paid
     */
    void updateOrderStatus(String orderId, ProcessStatus paid);

    /**
     * 发送消息给库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 查询过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 关闭订单
     * @param orderInfo
     */
    void execExpireOrder(OrderInfo orderInfo);

    /**
     * 拆分订单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    /**
     * 转换成map
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);
}
