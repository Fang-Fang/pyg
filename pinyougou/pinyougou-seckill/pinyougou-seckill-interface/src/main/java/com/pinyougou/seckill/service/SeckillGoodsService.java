package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface SeckillGoodsService extends BaseService<TbSeckillGoods> {

    PageResult search(Integer page, Integer rows, TbSeckillGoods seckillGoods);

    /**
     * 查询秒杀系统首页商品列表
     * 查询已经审核通过并且库存大于0，并且开始时间小于等于当前时间，结束时间大于当前时间
     * @return 秒杀商品列表
     */
    List<TbSeckillGoods> findList();

    /**
     * 根据秒杀商品id到redis中查询该秒杀商品
     * @param id 秒杀商品id
     * @return 秒杀商品
     */
    TbSeckillGoods findOneFromRedis(Long id);
}