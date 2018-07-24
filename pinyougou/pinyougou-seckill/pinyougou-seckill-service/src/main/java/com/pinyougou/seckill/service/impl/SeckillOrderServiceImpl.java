package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.common.util.RedisLock;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service(interfaceClass = SeckillOrderService.class)
public class SeckillOrderServiceImpl extends BaseServiceImpl<TbSeckillOrder> implements SeckillOrderService {

    //秒杀订单在redis中对应的key的名称
    private static final String SECKILL_ORDERS = "SECKILL_ORDERS";
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Override
    public PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(seckillOrder.get***())){
            criteria.andLike("***", "%" + seckillOrder.get***() + "%");
        }*/

        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public String submitOrder(Long seckillId, String userId) throws Exception {
        String orderId = "";
        //需要添加分布式锁：
        RedisLock redisLock = new RedisLock(redisTemplate);
        if(redisLock.lock(seckillId.toString())) {
            //1、到redis中根据秒杀商品id获取该秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillId);
            //2、判断商品是否存在并库存大于0
            if (seckillGoods == null) {
                throw new RuntimeException("秒杀商品不存在");
            }
            if (seckillGoods.getStockCount() == 0) {
                throw new RuntimeException("商品已秒完");
            }
            //3、递减库存之后判断库存是否大于0
            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

            if(seckillGoods.getStockCount() > 0) {
                //3.1、如果库存大于0；将最新的秒杀商品更新回redis
                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillId, seckillGoods);
            } else {
                //3.2、如果库存等于0；将该秒杀商品从redis中同步回mysql数据库中；并且删除redis中该秒杀商品
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);

                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).delete(seckillId);
            }
            //释放分布式锁：
            redisLock.unlock(seckillId.toString());

            //4、生成秒杀订单并保存到redis
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            orderId = seckillOrder.getId().toString();
            seckillOrder.setStatus("0");
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setUserId(userId);
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            seckillOrder.setSeckillId(seckillId);

            redisTemplate.boundHashOps(SECKILL_ORDERS).put(orderId, seckillOrder);
        }
        //5、返回秒杀订单id
        return orderId;
    }

    @Override
    public TbSeckillOrder findSeckillOrderByOutTradeNo(String outTradeNo) {
        return (TbSeckillOrder) redisTemplate.boundHashOps(SECKILL_ORDERS).get(outTradeNo);
    }

    @Override
    public void saveOrderInRedisToDB(String outTradeNo, String transaction_id) {
        //1、获取到秒杀订单
        TbSeckillOrder seckillOrder = findSeckillOrderByOutTradeNo(outTradeNo);
        //2、修改订单信息
        seckillOrder.setStatus("1");
        seckillOrder.setPayTime(new Date());
        seckillOrder.setTransactionId(transaction_id);

        //3、保存订单到数据库中
        seckillOrderMapper.insertSelective(seckillOrder);

        //4、删除redis中的订单
        redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
    }

    @Override
    public void deleteOrderInRedis(String outTradeNo) throws InterruptedException {
        //1、删除秒杀订单
        TbSeckillOrder seckillOrder = findSeckillOrderByOutTradeNo(outTradeNo);
        redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);

        RedisLock redisLock = new RedisLock(redisTemplate);
        if(redisLock.lock(seckillOrder.getSeckillId().toString())) {
            //2、更新秒杀商品库存
            //2.1、查询秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillOrder.getSeckillId());
            //2.2、如果秒杀商品不存在则需要到mysql中查询
            if (seckillGoods == null) {
                 seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
            }
            //2.3、对秒杀商品叠加1个库存
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);

            //2.4、将秒杀商品更新到redis中
            redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillGoods.getId(), seckillGoods);

            //释放分布式锁
            redisLock.unlock(seckillOrder.getSeckillId().toString());
        }

    }
}
