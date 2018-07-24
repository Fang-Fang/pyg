package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSONArray;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 如果是审核通过的话，则需要将那些商品sku列表发送消息到MQ，在搜索系统接收消息并将这些sku商品列表导入solr
 */
public class ItemSolrQueueMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) {

        try {
            //1、接收消息
            TextMessage textMessage = (TextMessage) message;
            List<TbItem> itemList = JSONArray.parseArray(textMessage.getText(), TbItem.class);

            //2、导入数据到solr中
            itemSearchService.importItemList(itemList);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
