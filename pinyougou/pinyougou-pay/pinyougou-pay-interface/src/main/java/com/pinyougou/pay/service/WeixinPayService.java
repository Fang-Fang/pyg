package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 调用支付系统的支付业务对象实现统一下单的接口并返回信息（操作结果，二维码地址）
     * @param outTradeNo 提供给微信的支付业务id（支付日志id）
     * @param totalFee 本次要支付的总金额
     * @return 微信那边的操作结果
     */
    Map<String,String> createNative(String outTradeNo, String totalFee);

    /**
     * 到微信支付系统根据交易编号查询该交易支付情况
     * @param outTradeNo 交易编号
     * @return 微信那边的操作结果
     */
    Map<String,String> queryPayStatus(String outTradeNo);

    /**
     * 关闭在微信中的订单
     * @param outTradeNo 交易编号
     * @return 微信那边的操作结果
     */
    Map<String,String> closeOrder(String outTradeNo);
}
