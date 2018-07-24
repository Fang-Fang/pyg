package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    //系统购物车在redis中对应的key的名称
    private static final String REDIS_CART_KEY = "CART_LIST";

    @Autowired
    private IdWorker idWorker;

    @Override
    public PageResult search(Integer page, Integer rows, TbOrder order) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(order.get***())){
            criteria.andLike("***", "%" + order.get***() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        PageInfo<TbOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public String addOrder(TbOrder order) {
        String payLogId = "";
        //查询当前登录用户的购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_KEY).get(order.getUserId());

        if(cartList != null && cartList.size() > 0) {
            //遍历购物车列表根据不同的商家生成不同的订单
            double orderPayment = 0.0;
            double totalFee = 0.0;
            String orderIds = "";
            for (Cart cart : cartList) {
                TbOrder tbOrder = new TbOrder();
                tbOrder.setOrderId(idWorker.nextId());
                tbOrder.setStatus("1");//未付款
                tbOrder.setSellerId(cart.getSellerId());//卖家
                tbOrder.setReceiverAreaName(order.getReceiverAreaName());
                tbOrder.setReceiver(order.getReceiver());
                tbOrder.setReceiverMobile(order.getReceiverMobile());
                tbOrder.setCreateTime(new Date());
                tbOrder.setUpdateTime(tbOrder.getCreateTime());

                //在订单里面根据商品列表生成一个个的订单明细记录
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    orderItem.setId(idWorker.nextId());
                    orderItem.setOrderId(tbOrder.getOrderId());
                    orderItemMapper.insertSelective(orderItem);

                    //累计本订单的总金额
                    orderPayment += orderItem.getTotalFee().doubleValue();
                }

                //本订单的总金额
                tbOrder.setPayment(new BigDecimal(orderPayment));

                //累计所有订单的总金额
                totalFee += orderPayment;

                orderMapper.insertSelective(tbOrder);

                //记录每个订单的订单号
                if (orderIds.length() > 0) {
                    orderIds += "," + tbOrder.getOrderId();
                } else {
                    orderIds = tbOrder.getOrderId().toString();
                }
            }

            //如果是微信支付的话则需要生成一条本次总支付的支付日志并返回支付日志id；如果货到付款的话则返回空
            if ("1".equals(order.getPaymentType())) {
                TbPayLog payLog = new TbPayLog();
                payLogId = idWorker.nextId() + "";
                payLog.setOutTradeNo(payLogId);
                payLog.setTradeState("0");//未支付
                payLog.setPayType("1");//微信支付
                payLog.setCreateTime(new Date());
                payLog.setUserId(order.getUserId());
                payLog.setTotalFee((long)(totalFee*100));//所有订单的累计总金额；因为提交到微信的金额要精确到分
                payLog.setOrderList(orderIds);//本次支付包含的所有订单id

                payLogMapper.insertSelective(payLog);
            }

            //删除购物车中数据
            redisTemplate.boundHashOps(REDIS_CART_KEY).delete(order.getUserId());
        }

        //返回支付日志id
        return payLogId;
    }

    @Override
    public TbPayLog findPayLogByOutTradeNo(String outTradeNo) {
        return payLogMapper.selectByPrimaryKey(outTradeNo);
    }

    @Override
    public void updateOrderByOutTradeNo(String outTradeNo, String transaction_id) {
        //1、更新支付日志的支付状态
        TbPayLog payLog = findPayLogByOutTradeNo(outTradeNo);
        payLog.setTradeState("1");//已支付
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);

        payLogMapper.updateByPrimaryKeySelective(payLog);

        //2、修改支付日志中对应的所有订单的支付状态为已支付
        String[] orderIds = payLog.getOrderList().split(",");

        //更新的对象内容
        TbOrder order = new TbOrder();
        order.setPaymentTime(new Date());
        order.setStatus("2");//已付款

        //创建更新条件
        Example example = new Example(TbOrder.class);
        example.createCriteria().andIn("orderId", Arrays.asList(orderIds));

        orderMapper.updateByExampleSelective(order, example);
    }
}
