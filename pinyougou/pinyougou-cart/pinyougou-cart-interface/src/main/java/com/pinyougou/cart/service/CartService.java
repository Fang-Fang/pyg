package com.pinyougou.cart.service;

import com.pinyougou.vo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加一个商品和购买数量到购物车列表
     * @param cartList 购物车列表
     * @param itemId 商品id
     * @param num 购买数量
     * @return 购物车列表
     */
    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 根据用户id查询其对应的购物车列表
     * @param username 用户id
     * @return
     */
    List<Cart> findCartListByUsername(String username);

    /**
     * 保存最新的购物车列表到redis中
     * @param cartList 购物车列表
     * @param username 用户id
     */
    void saveCartListByUsername(List<Cart> cartList, String username);

    /**
     * 合并两个购物车列表数据
     * @param cookie_cartList
     * @param redis_cartList
     * @return 合并之后的购物车列表
     */
    List<Cart> mergeCartList(List<Cart> cookie_cartList, List<Cart> redis_cartList);
}
