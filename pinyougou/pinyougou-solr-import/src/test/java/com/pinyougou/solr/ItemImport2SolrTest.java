package com.pinyougou.solr;

import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class ItemImport2SolrTest {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void importItem(){
        //1、获取商品列表
        TbItem param = new TbItem();
        param.setStatus("1");
        List<TbItem> itemList = itemMapper.select(param);

        //2、转换每个商品中的specMap
        for (TbItem tbItem : itemList) {
            Map specMap = JSONObject.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(specMap);
        }

        //3、导入
        solrTemplate.saveBeans(itemList);

        solrTemplate.commit();
    }
}
