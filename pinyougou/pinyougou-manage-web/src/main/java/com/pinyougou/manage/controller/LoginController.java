package com.pinyougou.manage.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/login")
@RestController
public class LoginController {

    /**
     * 获得当前登录用户名
     * @return 登录用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, String> getUsername(){
        Map<String, String> map = new HashMap<String, String>();

        //获取security中保存的当前登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //如果要获取用户的角色权限
        //SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        map.put("username", username);

        return map;
    }
}
