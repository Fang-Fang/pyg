package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 根据搜索关键字搜索solr中的商品
     * @param searchMap 搜索条件
     * @return 搜索结果
     */
    Map<String,Object> search(Map<String, Object> searchMap);

    /**
     * 批量导入商品sku到solr中
     * @param itemList 商品sku 列表
     */
    void importItemList(List<TbItem> itemList);

    /**
     * 根据商品spu id集合删除solr中对应的sku商品
     * @param ids 商品spu id集合
     */
    void deleteItemByGoodsIds(Long[] ids);
}
