package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/test")
@RestController
public class PageTestController {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    /**
     * 审核通过时生成商品的静态页面到指定路径
     * @param goodsIds 商品spu id集合
     * @return 操作标识符
     */
    @GetMapping("/audit")
    public String auditGoods(Long[] goodsIds){
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                genHtml(goodsId);
            }
        }
        return "success";
    }

    /**
     * 批量删除商品后到指定路径删除静态页面
     * @param ids 商品spu id集合
     * @return 操作标识符
     */
    @GetMapping("/delete")
    public String deleteGoods(Long[] ids){
        if (ids != null && ids.length > 0) {
            for (Long goodsId : ids) {
                File file = new File(ITEM_HTML_PATH + goodsId + ".html");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        return "success";
    }

    /**
     * 根据商品spu id生成html页面到指定路径下
     * @param goodsId 商品spu id
     */
    private void genHtml(Long goodsId) {
        try {
            //获取配置对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();

            //获取模版
            Template template = configuration.getTemplate("item.ftl");

            //数据
            Map<String, Object> rootMap = new HashMap<>();

            Goods goods = goodsService.findGoodsByGoodsIdAndStatus(goodsId, "1");

            //基本
            rootMap.put("goods", goods.getGoods());
            //描述
            rootMap.put("goodsDesc", goods.getGoodsDesc());

            //1级分类
            TbItemCat itemCat = itemCatService.findOne(goods.getGoods().getCategory1Id());
            rootMap.put("itemCat1", itemCat.getName());
            //2级分类
            itemCat = itemCatService.findOne(goods.getGoods().getCategory2Id());
            rootMap.put("itemCat2", itemCat.getName());
            //3级分类
            itemCat = itemCatService.findOne(goods.getGoods().getCategory3Id());
            rootMap.put("itemCat3", itemCat.getName());

            //sku商品列表
            rootMap.put("itemList", goods.getItemList());

            //输出
            FileWriter fileWriter = new FileWriter(ITEM_HTML_PATH + goodsId + ".html");
            template.process(rootMap, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
