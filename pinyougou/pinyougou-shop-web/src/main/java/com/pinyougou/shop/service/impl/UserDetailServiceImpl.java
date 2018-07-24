package com.pinyougou.shop.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    /**
     * 可以根据用户名查询用户的信息（密码，权限）；
     *
     * @param username 是登录的时候输入的username
     * @return 用户对象（用户和角色权限和密码（需要与输入的密码进行对比的））
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //获取角色权限集合（如果是真式项目应该从数据库中查询）
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //根据输入的用户名查询用户信息
        TbSeller seller = sellerService.findOne(username);

        if (seller != null && "1".equals(seller.getStatus())) {//说明用户名正确并是审核通过的用户

            //只要密码为123456的任何用户都可以登录
            User user =  new User(username, seller.getPassword(), authorities);

            return user;
        }
        return null;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
}
