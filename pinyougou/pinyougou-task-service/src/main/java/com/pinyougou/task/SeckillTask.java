package com.pinyougou.task;

import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 任务执行表达式cron =秒 分 时 日 月 周 年（可选）
     * 需要将数据库中状态已审核，库存大于0，开始时间小于等于当前时间，
     * 结束时间大于当前时间，并且这些商品是不在redis中的那些秒杀商品更新到redis中
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void refreshSeckillGoods(){
        //1、查询符合条件的秒杀商品

        //获取redis中的所有秒杀商品的id集合
        List ids = new ArrayList(redisTemplate.boundHashOps("SECKILL_GOODS").keys());

        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");
        criteria.andGreaterThan("stockCount", 0);
        criteria.andLessThanOrEqualTo("startTime", new Date());
        criteria.andGreaterThan("endTime", new Date());

        if (ids != null && ids.size() > 0) {
            criteria.andNotIn("id", ids);
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //2、更新秒杀商品到redis
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("SECKILL_GOODS").put(seckillGoods.getId(), seckillGoods);
            }
            System.out.println("更新了 " + seckillGoodsList.size() + " 条秒杀商品到redis中...");
        }
    }

    /**
     * 需要将在redis中结束时间小于当前时间的那些秒杀商品移除
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods(){
        //1、查询在redis中的所有秒杀商品
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("SECKILL_GOODS").values();
        //2、遍历每一个商品是否已经过期；如果过期则从redis中移除并更新到mysql
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                if (seckillGoods.getEndTime().getTime() < new Date().getTime()) {
                    //说明秒杀商品已经过时，需要移除并更新到mysql
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);

                    redisTemplate.boundHashOps("SECKILL_GOODS").delete(seckillGoods.getId());

                    System.out.println("移除了id为：" + seckillGoods.getId() + " 的秒杀商品.");
                }
            }
        }
    }
}
