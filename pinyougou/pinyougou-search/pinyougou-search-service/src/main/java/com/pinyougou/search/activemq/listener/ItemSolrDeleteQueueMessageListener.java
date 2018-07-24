package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSONArray;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.*;
import java.io.Serializable;
import java.util.List;

/**
 * 如果是删除则发送商品spu id集合到mq，搜索系统接收消息并根据spu id删除solr中对应的商品
 */
public class ItemSolrDeleteQueueMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) {

        try {
            //1、接收消息
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();

            //2、导入数据到solr中
            itemSearchService.deleteItemByGoodsIds(ids);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
