package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private OrderService orderService;

    @Reference
    private WeixinPayService weixinPayService;

    /**
     * 调用支付系统的接口方法统一下单，返回支付二维码地址
     * @param outTradeNo 支付日志id
     * @return 操作结果（支付二维码地址）
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo) {
        try {
            //1、根据outTradeNo支付日志id从数据库中获取支付信息（支付日志id，支付总金额...）
            TbPayLog payLog = orderService.findPayLogByOutTradeNo(outTradeNo);

            if(payLog != null) {
                //2、调用支付系统的支付业务对象实现统一下单的接口并返回信息（操作结果，二维码地址）
                return weixinPayService.createNative(outTradeNo, payLog.getTotalFee().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 根据交易编号查询该交易的状态
     * @param outTradeNo 交易编号（支付日志id）
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

                //1.1、如果支付成功：修改支付日志的支付状态为已支付，修改支付日志中对应的所有订单的支付状态为已支付
                if("SUCCESS".equals(resultMap.get("trade_state"))){
                    orderService.updateOrderByOutTradeNo(outTradeNo, resultMap.get("transaction_id"));
                    result = Result.ok("支付成功");
                    break;
                }
                count++;
                //1.2、如在3分钟里面，每隔3秒查询，如果超过3分钟则提示支付二维码超时；
                if(count > 60){
                    result = Result.fail("二维码超时");
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
