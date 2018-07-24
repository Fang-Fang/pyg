package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface OrderService extends BaseService<TbOrder> {

    PageResult search(Integer page, Integer rows, TbOrder order);

    /**
     * 保存订单基本、明细、支付日志
     * @param order 基本信息
     * @return 支付日志id
     */
    String addOrder(TbOrder order);

    /**
     * 根据outTradeNo支付日志id从数据库中获取支付信息
     * @param outTradeNo 支付日志id
     * @return 支付日志
     */
    TbPayLog findPayLogByOutTradeNo(String outTradeNo);

    /**
     * 修改支付日志的支付状态为已支付，修改支付日志中对应的所有订单的支付状态为已支付
     * @param outTradeNo 支付日志id
     * @param transaction_id 微信订单号
     */
    void updateOrderByOutTradeNo(String outTradeNo, String transaction_id);
}