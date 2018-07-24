package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {

    PageResult search(Integer page, Integer rows, TbGoods goods);

    /**
     * 同时保存基本、描述、商品sku列表
     * @param goods 商品信息
     */
    void addGoods(Goods goods);

    /**
     * 根据商品id查询商品基本、描述、sku列表
     * @param id 商品spu id
     * @return 商品基本、描述、sku列表
     */
    Goods findGoodsByGoodsId(Long id);

    /**
     * 根据商品id保存商品基本、描述、sku列表
     * @param goods 商品
     */
    void updateGoods(Goods goods);

    /**
     * 批量更新商品的状态
     * @param ids 商品id集合
     * @param status 状态 0未审核 1审核中 2审核通过 3审核不通过 4关闭
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 批量逻辑删除商品
     * @param ids 商品id集合
     */
    void deleteGoodsByIds(Long[] ids);

    /**
     * 询审核通过的这些商品对应的sku列表
     * @param ids 商品spu id集合
     * @param status 商品sku的状态
     * @return sku列表
     */
    List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String status);

    /**
     * 根据商品id查询商品基本、描述、sku列表根据是默认降序排序
     * @param goodsId 商品spu id
     * @param status 商品sku 的状态
     * @return 商品基本、描述、sku列表
     */
    Goods findGoodsByGoodsIdAndStatus(Long goodsId, String status);
}