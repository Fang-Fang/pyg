package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map<String, String> createNative(String outTradeNo, String totalFee) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            //1、组装参数
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);//公众账号ID
            param.put("mch_id", partner);//商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            //param.put("sign", "");//签名；在提交的时候由微信工具类统一签名
            param.put("body", "品优购");//商品描述
            param.put("out_trade_no", outTradeNo);//商户订单号
            param.put("total_fee", totalFee);//标价金额
            param.put("spbill_create_ip", "127.0.0.1");//终端IP
            param.put("notify_url", notifyurl);//通知地址
            param.put("trade_type", "NATIVE");//交易类型

            //2、转换参数为微信可接受的内容
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发送到微信 统一下单 接口的内容：" + signedXml);

            //3、创建http请求对象;发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //4、处理返回结果
            String content = httpClient.getContent();
            System.out.println("调用微信 统一下单 接口的返回内容：" + content);
            Map<String, String> returnMap = WXPayUtil.xmlToMap(content);
            resultMap.put("outTradeNo", outTradeNo);
            resultMap.put("totalFee", totalFee);
            resultMap.put("result_code", returnMap.get("result_code"));
            resultMap.put("code_url", returnMap.get("code_url"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try {
            //1、组装参数
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);//公众账号ID
            param.put("mch_id", partner);//商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            //param.put("sign", "");//签名；在提交的时候由微信工具类统一签名
            param.put("out_trade_no", outTradeNo);//商户订单号

            //2、转换参数为微信可接受的内容
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发送到微信 查询订单 接口的内容：" + signedXml);

            //3、创建http请求对象;发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //4、处理返回结果
            String content = httpClient.getContent();
            System.out.println("调用微信 查询订单 接口的返回内容：" + content);
            Map<String, String> returnMap = WXPayUtil.xmlToMap(content);

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("trade_state", returnMap.get("trade_state"));
            resultMap.put("transaction_id", returnMap.get("transaction_id"));

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, String> closeOrder(String outTradeNo) {
        try {
            //1、组装参数
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);//公众账号ID
            param.put("mch_id", partner);//商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            //param.put("sign", "");//签名；在提交的时候由微信工具类统一签名
            param.put("out_trade_no", outTradeNo);//商户订单号

            //2、转换参数为微信可接受的内容
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发送到微信 关闭订单 接口的内容：" + signedXml);

            //3、创建http请求对象;发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //4、处理返回结果
            String content = httpClient.getContent();
            System.out.println("调用微信 关闭订单 接口的返回内容：" + content);
            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
