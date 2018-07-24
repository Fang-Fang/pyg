package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service(interfaceClass = ItemCatService.class)
public class ItemCatServiceImpl extends BaseServiceImpl<TbItemCat> implements ItemCatService {

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, TbItemCat itemCat) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(itemCat.get***())){
            criteria.andLike("***", "%" + itemCat.get***() + "%");
        }*/

        List<TbItemCat> list = itemCatMapper.selectByExample(example);
        PageInfo<TbItemCat> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void updateItemCatToRedis() {
        //1、获取所有商品分类
        List<TbItemCat> itemCatList = findAll();
        //2、遍历每个商品分类，及对应的分类模版id
        for (TbItemCat itemCat : itemCatList) {
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
        }
        System.out.println("缓存商品分类数据完成。");
    }
}
