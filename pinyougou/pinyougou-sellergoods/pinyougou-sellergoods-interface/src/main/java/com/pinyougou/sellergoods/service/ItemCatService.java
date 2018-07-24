package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface ItemCatService extends BaseService<TbItemCat> {

    PageResult search(Integer page, Integer rows, TbItemCat itemCat);

    /**
     * 缓存商品分类到redis中;将商品分类名称与其对应的分类模版id缓存到redis中
     */
    void updateItemCatToRedis();
}