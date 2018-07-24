package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 如果是审核通过的话，则需要将商品spu id集合发送到mq，详情系统接收并根据每个spu id生成具体的html文件到指定路径
 */
public class ItemTopicMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;


    @Override
    public void onMessage(Message message, Session session) throws JMSException {

        //1、接收消息
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] goodsIds = (Long[]) objectMessage.getObject();
        //2、生成具体的html
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                genHtml(goodsId);
            }
        }
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
