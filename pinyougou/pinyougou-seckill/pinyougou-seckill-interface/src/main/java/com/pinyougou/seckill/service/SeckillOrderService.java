package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface SeckillOrderService extends BaseService<TbSeckillOrder> {

    PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder);

    /**
     * 生成秒杀订单到redis中
     * @param seckillId 秒杀商品id
     * @param userId 当前登录用户id
     * @return 秒杀订单id
     */
    String submitOrder(Long seckillId, String userId) throws InterruptedException, Exception;

    /**
     * 根据秒杀订单id从redis中获取秒杀订单
     * @param outTradeNo 秒杀订单id
     * @return 秒杀订单
     */
    TbSeckillOrder findSeckillOrderByOutTradeNo(String outTradeNo);

    /**
     * 将redis中的订单的支付状态修改为已支付并保存到数据库中
     * @param outTradeNo 订单id
     * @param transaction_id 微信交易号
     */
    void saveOrderInRedisToDB(String outTradeNo, String transaction_id);

    /**
     * 删除订单并加回库存
     * @param outTradeNo 订单id
     */
    void deleteOrderInRedis(String outTradeNo) throws InterruptedException;
}