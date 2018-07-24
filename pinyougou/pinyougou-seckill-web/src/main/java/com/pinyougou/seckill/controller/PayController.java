package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private SeckillOrderService seckillOrderService;

    @Reference
    private WeixinPayService weixinPayService;

    /**
     * 调用支付系统的接口方法统一下单，返回支付二维码地址
     * @param outTradeNo 订单id
     * @return 操作结果（支付二维码地址）
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo) {
        try {
            //1、根据outTradeNo订单id从redis中获取订单信息（订单id，支付总金额...）
            TbSeckillOrder seckillOrder = seckillOrderService.findSeckillOrderByOutTradeNo(outTradeNo);

            if(seckillOrder != null) {
                //2、调用支付系统的支付业务对象实现统一下单的接口并返回信息（操作结果，二维码地址）
                long totalFee = (long)(seckillOrder.getMoney().doubleValue()*100);
                return weixinPayService.createNative(outTradeNo, totalFee+"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 根据交易编号查询该交易的状态
     * @param outTradeNo 交易编号（订单id）
     * @return 操作结果
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = Result.fail("支付失败");

        try {
            int count = 0;
            while(true) {
                //1、到支付系统查询支付状态
                Map<String, String> resultMap = weixinPayService.queryPayStatus(outTradeNo);

                if (resultMap == null) {
                    break;
                }

                //1.1、如果支付成功：修改订单的支付状态为已支付，修改订单中对应的所有订单的支付状态为已支付
                if("SUCCESS".equals(resultMap.get("trade_state"))){
                    seckillOrderService.saveOrderInRedisToDB(outTradeNo, resultMap.get("transaction_id"));
                    result = Result.ok("支付成功");
                    break;
                }
                count++;
                //1.2、如在1分钟里面，每隔3秒查询，如果超过1分钟则提示支付超时；
                if(count > 5){
                    result = Result.fail("支付超时");

                    //关闭在微信中的订单
                    resultMap = weixinPayService.closeOrder(outTradeNo);
                    if("ORDERPAID".equals(resultMap.get("err_code"))){
                        //关闭订单的过程中被人支付了的话；那么也算支付成功
                        seckillOrderService.saveOrderInRedisToDB(outTradeNo, resultMap.get("transaction_id"));
                        result = Result.ok("支付成功");
                        break;
                    }

                    //更新秒杀商品（加回库存并删除订单）
                    seckillOrderService.deleteOrderInRedis(outTradeNo);

                    break;
                }
                //每隔3秒
                Thread.sleep(3000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
