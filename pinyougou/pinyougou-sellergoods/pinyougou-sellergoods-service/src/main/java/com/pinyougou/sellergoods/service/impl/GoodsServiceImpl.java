package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Transactional
@Service(interfaceClass = GoodsService.class)
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbGoods goods) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //过滤已经删除的数据
        criteria.andNotEqualTo("isDelete", "1");//不等于1的数据才查询

        //根据商家查询
        if(!StringUtils.isEmpty(goods.getSellerId())){
            criteria.andEqualTo("sellerId", goods.getSellerId());
        }
        //根据状态查询
        if(!StringUtils.isEmpty(goods.getAuditStatus())){
            criteria.andEqualTo("auditStatus", goods.getAuditStatus());
        }
        //根据商品名称模糊查询
        if(!StringUtils.isEmpty(goods.getGoodsName())){
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    //@Transactional //事务控制
    @Override
    public void addGoods(Goods goods) {
        //1、保存基本信息
        add(goods.getGoods());

        //int i = 1/0;

        //2、保存描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        //3、保存sku商品列表
        saveItemList(goods);
    }

    @Override
    public Goods findGoodsByGoodsId(Long id) {
        Goods goods = new Goods();
        //1、获取基本信息
        goods.setGoods(findOne(id));

        //2、获取描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));

        //3、获取sku列表
        TbItem param = new TbItem();
        param.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(param);
        goods.setItemList(itemList);

        return goods;
    }

    @Override
    public void updateGoods(Goods goods) {
        //1、修改商品基本信息
        update(goods.getGoods());

        //2、修改描述信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());

        //3、删除sku列表
        TbItem param = new TbItem();
        param.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(param);

        //4、保存商品sku列表
        saveItemList(goods);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null && ids.length > 0) {
            TbGoods goods = new TbGoods();
            goods.setAuditStatus(status);

            Example example = new Example(TbGoods.class);
            example.createCriteria().andIn("id", Arrays.asList(ids));

            //选择性更新，参数1：更新的对象，参数2：更新条件
            goodsMapper.updateByExampleSelective(goods, example);

            //如果是审核通过的话；则需要将sku的状态修改为1， 启用 状态
            if("2".equals(status)) {
                TbItem item = new TbItem();
                item.setStatus("1");//已启用

                Example itemExample = new Example(TbItem.class);
                itemExample.createCriteria().andIn("goodsId", Arrays.asList(ids));

                itemMapper.updateByExampleSelective(item, itemExample);
                //update tb_item set status="1" where id in (1,3,4);
            }
        }
    }

    @Override
    public void deleteGoodsByIds(Long[] ids) {
        TbGoods goods = new TbGoods();
        goods.setIsDelete("1");//标记为已删除

        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));

        goodsMapper.updateByExampleSelective(goods, example);
    }

    @Override
    public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String status) {

        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", status)
                .andIn("goodsId", Arrays.asList(ids));

        return itemMapper.selectByExample(example);
    }

    @Override
    public Goods findGoodsByGoodsIdAndStatus(Long goodsId, String status) {
        Goods goods = new Goods();
        //1、获取基本信息
        goods.setGoods(findOne(goodsId));

        //2、获取描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(goodsId));

        //3、获取sku列表
        Example example = new Example(TbItem.class);
        example.createCriteria()
                .andEqualTo("status", status)
                .andEqualTo("goodsId", goodsId);

        example.orderBy("isDefault").desc();

        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;
    }

    /**
     * 保存sku商品列表
     */
    private void saveItemList(Goods goods) {
        //如果启用规格的话
        if("1".equals(goods.getGoods().getIsEnableSpec())) {
            List<TbItem> itemList = goods.getItemList();
            if (itemList != null && itemList.size() > 0) {
                for (TbItem item : itemList) {

                    //标题：spu标题+当前sku的所有规格值（spec）
                    String title = goods.getGoods().getGoodsName();
                    //将规格转换为一个map对象
                    Map specMap = JSON.parseObject(item.getSpec(), Map.class);
                    Set<Map.Entry> entrySet = specMap.entrySet();
                    for (Map.Entry entry : entrySet) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);

                    setItemValue(item, goods);

                    //保存sku
                    itemMapper.insertSelective(item);
                }
            }
        } else {
            //不启用规格:则使用spu的信息作为sku的信息保存一条sku到tb_item
            TbItem tbItem = new TbItem();

            //标题使用spu的标题
            tbItem.setTitle(goods.getGoods().getGoodsName());

            tbItem.setPrice(goods.getGoods().getPrice());
            tbItem.setNum(9999);
            tbItem.setStatus("0");
            tbItem.setIsDefault("1");
            tbItem.setSpec("{}");

            setItemValue(tbItem, goods);

            itemMapper.insertSelective(tbItem);
        }
    }

    /**
     * 设置sku信息
     * @param item 设置的sku
     * @param goods 商品spu
     */
    private void setItemValue(TbItem item, Goods goods) {
        //品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());

        //图片：从spu的图片列表中获取第1个
        List<Map> imageList = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList != null && imageList.size() > 0) {
            item.setImage(imageList.get(0).get("url").toString());
        }

        //categoryId：获取spu中的第3级的商品分类id
        item.setCategoryid(goods.getGoods().getCategory3Id());

        //category：获取spu中的第3级的商品分类id对应的商品分类中文名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName());

        //goods_id：spu对应的id
        item.setGoodsId(goods.getGoods().getId());

        //seller_id:获取spu中的seller_id
        item.setSellerId(goods.getGoods().getSellerId());

        //seller:获取spu中的seller_id获取商家的中文名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(item.getSellerId());
        item.setSeller(seller.getName());

        item.setCreateTime(new Date());
        item.setUpdateTime(item.getCreateTime());
    }

}
