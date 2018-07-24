package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {//处理搜索关键字中的空格
            searchMap.put("keywords", searchMap.get("keywords").toString().replaceAll(" ", ""));
        }

        //查询
        resultMap.putAll(searchItemList(searchMap));

        //获取商品分类中文名称列表
        List<String> categoryList = getCategoryList(searchMap);
        resultMap.put("categoryList", categoryList);

        //如果前端传递了商品分类的话，则按照传递的商品分类进行查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if(StringUtils.isEmpty(categoryName) && categoryList.size() > 0) {
            // 默认获取第一个商品分类对应的品牌和规格列表
            resultMap.putAll(findBrandListAndSpecList(categoryList.get(0)));
        } else {
            resultMap.putAll(findBrandListAndSpecList(categoryName));
        }
        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> itemList) {
        for (TbItem tbItem : itemList) {
            Map specMap = JSONObject.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteItemByGoodsIds(Long[] ids) {

        SimpleQuery query = new SimpleQuery();

        Criteria criteria = new Criteria("item_goodsid").in(Arrays.asList(ids));
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 根据分类名称获取redis中对应的品牌和规格选项列表
     * @param categoryName 商品分类中文名称
     * @return 品牌和规格选项列表
     */
    private Map<String, Object> findBrandListAndSpecList(String categoryName) {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            //1、根据商品分类获取分类模板id
            Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);

            //2、根据分类模板id获取品牌列表
            List<Map<String, Object>> brandList = (List<Map<String, Object>>) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);

            //3、根据分类模板id获取规格选项列表
            List<Map<String, Object>> specList = (List<Map<String, Object>>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;

    }

    /**
     * 获取商品分类中文名称列表:对搜索关键字查询的记录按照商品分类进行分组，再获取分组的名称
     * @param searchMap 搜索条件
     * @return 商品分类中文名称列表
     */
    private List<String> getCategoryList(Map<String,Object> searchMap) {
        List<String> categoryList = new ArrayList<>();

        SimpleQuery query = new SimpleQuery();

        //is方法会对查询关键字进行分词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组信息
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");//分组的域
        query.setGroupOptions(groupOptions);

        //分组查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);

        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

        for (GroupEntry<TbItem> groupEntry : groupResult.getGroupEntries()) {
            categoryList.add(groupEntry.getGroupValue());
        }

        return categoryList;
    }

    /**
     * 根据关键字和过滤条件查询并过滤数据
     * @param searchMap 键字和过滤条件
     * @return 查询结果
     */
    private Map<String, Object> searchItemList(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //创建高亮查询对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //is方法会对查询关键字进行分词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置高亮信息
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮起始标签
        highlightOptions.setSimplePostfix("</em>");//高亮结束标签
        query.setHighlightOptions(highlightOptions);

        //设置过滤条件查询
        if (!StringUtils.isEmpty(searchMap.get("category"))) {//在查询的结果中根据商品分类条件过滤
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(categoryCriteria);
            query.addFilterQuery(filterQuery);
        }

        if (!StringUtils.isEmpty(searchMap.get("brand"))) {//在查询的结果中根据品牌条件过滤
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(brandCriteria);
            query.addFilterQuery(filterQuery);
        }

        if (searchMap.get("spec") != null) {//在查询的结果中根据规格条件过滤
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                Criteria specCriteria = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                filterQuery.addCriteria(specCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //价格
        if(!StringUtils.isEmpty(searchMap.get("price"))){
            //获取价格区间
            String[] prices = searchMap.get("price").toString().split("-");

            //价格大于等于起始价格
            Criteria startCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(startCriteria);
            query.addFilterQuery(filterQuery);

            if(!"*".equals(prices[1])){
                //价格小于等于结束价格
                Criteria endCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                SimpleFilterQuery endFilterQuery = new SimpleFilterQuery(endCriteria);
                query.addFilterQuery(endFilterQuery);
            }
        }

        //设置分页
        int pageNo = 1;
        if (searchMap.get("pageNo") != null) {
            pageNo = Integer.parseInt(searchMap.get("pageNo").toString());
        }
        int pageSize = 40;
        if (searchMap.get("pageSize") != null) {
            pageSize = Integer.parseInt(searchMap.get("pageSize").toString());
        }
        query.setOffset((pageNo-1)*pageSize);//分页起始索引号 limit 起始索引号，页号
        query.setRows(pageSize);

        //设置排序
        if (!StringUtils.isEmpty(searchMap.get("sortField")) && !StringUtils.isEmpty(searchMap.get("sort"))) {

            //排序序列：DESC,ASC
            String order = searchMap.get("sort").toString();
            //参数1：排序序列，参数2：排序域名
            Sort sort = new Sort(new Sort.Order(order.equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, "item_" + searchMap.get("sortField")));
            query.addSort(sort);
        }


        //查询
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //处理高亮标题
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<TbItem> entry : highlighted) {
                List<HighlightEntry.Highlight> highlights = entry.getHighlights();
                if (highlights != null && highlights.size() > 0) {
                    entry.getEntity().setTitle(highlights.get(0).getSnipplets().get(0));
                }
            }
        }

        resultMap.put("rows", highlightPage.getContent());
        resultMap.put("total", highlightPage.getTotalElements());//总记录数
        resultMap.put("totalPages", highlightPage.getTotalPages());//总页数

        return resultMap;
    }
}
