package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 同时保存基本、描述、商品sku列表
     * @param goods 商品信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            //当前登录的商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            //商家id
            goods.getGoods().setSellerId(sellerId);
            //未审核
            goods.getGoods().setAuditStatus("0");//未审核
            goodsService.addGoods(goods);
            return Result.ok("增加商品成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("增加商品失败");
    }

    /**
     * 根据商品id查询商品基本、描述、sku列表
     * @param id 商品spu id
     * @return 商品基本、描述、sku列表
     */
    @GetMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findGoodsByGoodsId(id);
    }

    /**
     * 根据商品id保存商品基本、描述、sku列表
     * @param goods 商品
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());

            //判断当前的操作用户是否为该商品的商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if(sellerId.equals(goods.getGoods().getSellerId()) && sellerId.equals(oldGoods.getSellerId())) {
                goodsService.updateGoods(goods);
            } else {
                return Result.fail("商家非法");
            }
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     * @param goods 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody  TbGoods goods, @RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        //需要根据商家查询
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsService.search(page, rows, goods);
    }

    /**
     * 批量更新商品的状态
     * @param ids 商品id集合
     * @param status 状态 0未审核 1审核中 2审核通过 3审核不通过 4关闭
     * @return 操作结果
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {
            goodsService.updateStatus(ids, status);
            return Result.ok("更新状态成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新状态失败");
    }

    //TODO 上下架

}
