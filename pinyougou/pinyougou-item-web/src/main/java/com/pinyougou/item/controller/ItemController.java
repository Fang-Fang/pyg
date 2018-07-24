package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ItemController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    /**
     * 跳转到商品详情页面
     * @param goodsId 商品spu id
     * @return 页面名称和数据
     */
    @GetMapping("/{goodsId}")
    public ModelAndView toItemPage(@PathVariable Long goodsId){
        ModelAndView mv = new ModelAndView("item");

        Goods goods = goodsService.findGoodsByGoodsIdAndStatus(goodsId, "1");

        //基本
        mv.addObject("goods", goods.getGoods());
        //描述
        mv.addObject("goodsDesc", goods.getGoodsDesc());

        //1级分类
        TbItemCat itemCat = itemCatService.findOne(goods.getGoods().getCategory1Id());
        mv.addObject("itemCat1", itemCat.getName());
        //2级分类
        itemCat = itemCatService.findOne(goods.getGoods().getCategory2Id());
        mv.addObject("itemCat2", itemCat.getName());
        //3级分类
        itemCat = itemCatService.findOne(goods.getGoods().getCategory3Id());
        mv.addObject("itemCat3", itemCat.getName());

        //sku商品列表
        mv.addObject("itemList", goods.getItemList());

        return mv;
    }
}
