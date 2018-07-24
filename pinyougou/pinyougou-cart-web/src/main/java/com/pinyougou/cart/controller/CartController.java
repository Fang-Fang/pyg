package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    //在用户的浏览器中记录的购物车对应在cookie中的名称
    private static final String COOKIE_CART_LIST = "CART_LIST";
    //在用户的浏览器中记录的购物车对应在cookie中最大的时长；1天
    private static final int COOKIE_CART_MAX_AGE = 3600*24;

    @Reference
    private CartService cartService;
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private HttpServletResponse response;

    /**
     * 登录或者未登录情况下将商品加入购物车
     * CrossOrigin 设置跨域可访问的服务器信息：origins 设置那些可以访问的域名服务器（数组），allowCredentials 允许接收cookie信息
     * @param itemId 商品sku id
     * @param num 购买数量
     * @return 操作结果
     */
    @GetMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com", allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num){

        try {
            //允许接收如下域名发送过来的请求
            //response.setHeader("Access-Control-Allow-Origin","http://item.pinyougou.com");
            //允许获取传递过来的cookie
            //response.setHeader("Access-Control-Allow-Credentials", "true");

            //1、获取购物车列表
            List<Cart> cartList = findCartList();
            //2、将新的商品sku id和购买数量加入到购物车列表中
            cartList = cartService.addItemToCartList(cartList, itemId, num);
            //如果是没有登录的情况下获取到的名字为anonymousUser
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                //未登录；将商品加入到cookie
                //3、将最新的购物车列表写回cookie
                CookieUtils.setCookie(request, response, COOKIE_CART_LIST,
                        JSONArray.toJSONString(cartList), COOKIE_CART_MAX_AGE, true);
            } else {
                //已登录；将商品加入到redis
                //3、将最新的购物车列表写回redis
                cartService.saveCartListByUsername(cartList, username);
            }

            return Result.ok("加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("加入购物车失败");
    }

    /**
     * 获取登录或者未登录的购物车数据列表
     * @return 购物车列表
     */
    @GetMapping("/findCartList")
    public List<Cart> findCartList(){
        //如果是没有登录的情况下获取到的名字为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListJsonStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);
        List<Cart> cookie_cartList = new ArrayList<>();
        if (!StringUtils.isEmpty(cartListJsonStr)) {
            //将json格式字符串转换为集合
            cookie_cartList = JSONArray.parseArray(cartListJsonStr, Cart.class);
        }
        if ("anonymousUser".equals(username)) {
            //未登录；从cookie中获取购物车数据

            return cookie_cartList;
        } else {
            //已登录；从redis中获取购物车数据
            List<Cart> redis_cartList = cartService.findCartListByUsername(username);

            //合并cookie中的购物车数据
            if (cookie_cartList.size() > 0) {
                redis_cartList = cartService.mergeCartList(cookie_cartList, redis_cartList);

                //写回到redis
                cartService.saveCartListByUsername(redis_cartList, username);

                //删除cookie中数据
                CookieUtils.deleteCookie(request, response, COOKIE_CART_LIST);
            }

            return redis_cartList;
        }

    }

    /**
     * 获取当前登录用户名
     *
     * @return 登录用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername() {
        Map<String, Object> map = new HashMap<>();

        //返回map可以以后放置用户的其它信息如：头像地址，个人信息
        //如果是没有登录的情况下获取到的名字为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);

        return map;
    }

}
