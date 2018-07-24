package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service(interfaceClass = CartService.class)
public class CartServiceImpl implements CartService {

    //系统购物车在redis中对应的key的名称
    private static final String REDIS_CART_KEY = "CART_LIST";

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 1、判断是否存在和使用启用状态
     * 2、判断商家（Cart）是否存在
     * 2.1、当前商品对应的商家不存在：判断购买数量是否大于0，重新创建一个cart并且往其里面的orderItemList中添加一个orderItem；再将cart加入到cartList
     * 2.2、当前商品对应的商家存在
     * 2.2.1、商品存在在当前的商家的商品列表中：购买数量叠加（减）；如果处理完商品的购买数量为0的话，则需要将该商品从商品列表中删除；如果该商家的商品列表中商品数为0则需要将该商家从购物车列表中删除
     * 2.2.2、商品不存在：往当前这个商家的商品列表中添加一个新的商品（orderItem）
     */
    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态不合法");
        }

        Cart cart = findCartByItemId(cartList, item.getSellerId());
        if (cart == null) {
            //商家不存在
            if(num > 0){
                cart = new Cart();
                cart.setSellerId(item.getSellerId());
                cart.setSellerName(item.getSeller());
                List<TbOrderItem> orderItemList = new ArrayList<>();

                TbOrderItem orderItem = createOrderItem(item, num);
                orderItemList.add(orderItem);
                cart.setOrderItemList(orderItemList);

                cartList.add(cart);
            } else {
                throw new RuntimeException("商品购买数量非法");
            }
        } else {
            //商家存在
            TbOrderItem orderItem = findOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem != null){
                //商品存在；则购买数量叠加
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));

                if(orderItem.getNum() <= 0){
                    //说明商品数量为0，应该从商品列表中删除
                    cart.getOrderItemList().remove(orderItem);
                }
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            } else {
                //商品不存在
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }
        }

        return cartList;
    }

    @Override
    public List<Cart> findCartListByUsername(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_KEY).get(username);
        if (cartList != null) {
            return cartList;
        }
        return new ArrayList<>();
    }

    @Override
    public void saveCartListByUsername(List<Cart> cartList, String username) {
        redisTemplate.boundHashOps(REDIS_CART_KEY).put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cookie_cartList, List<Cart> redis_cartList) {
        for (Cart cart : cookie_cartList) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                redis_cartList = addItemToCartList(redis_cartList, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return redis_cartList;
    }

    /**
     * 根据商品id在商品列表中查询商品
     * @param orderItemList 商品列表
     * @param itemId 商品id
     * @return 商品orderItem
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        if (orderItemList != null && orderItemList.size() > 0) {
            for (TbOrderItem orderItem : orderItemList) {
                if (itemId.equals(orderItem.getItemId())) {
                    return orderItem;
                }
            }
        }
        return null;
    }

    /**
     * 根据商品信息和购买数量创建一个orderItem
     * @param item 商品信息
     * @param num 购买数量
     * @return orderItem
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setItemId(item.getId());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        //总费用
        orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
        return orderItem;
    }

    /**
     * 在购物车列表中根据商品id查询购物车
     * @param cartList 购物车列表
     * @param sellerId 商家id
     * @return 购物车
     */
    private Cart findCartByItemId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }
}
