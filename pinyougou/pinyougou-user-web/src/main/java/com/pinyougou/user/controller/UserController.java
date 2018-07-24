package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.PhoneFormatCheckUtils;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

@RequestMapping("/user")
@RestController
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/findAll")
    public List<TbUser> findAll() {
        return userService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return userService.findPage(page, rows);
    }

    /**
     * 注册用户
     * @param user 用户信息
     * @param smsCode 验证码
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbUser user, String smsCode) {
        try {
            if (userService.checkSmsCode(user.getPhone(), smsCode)) {
                user.setPassword(DigestUtils.md5Hex(user.getPassword()));
                user.setCreated(new Date());
                user.setUpdated(user.getCreated());

                userService.add(user);
            } else {
                return Result.fail("验证码输入错误；注册失败！");
            }
            return Result.ok("注册成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("注册失败");
    }

    @GetMapping("/findOne")
    public TbUser findOne(Long id) {
        return userService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbUser user) {
        try {
            userService.update(user);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            userService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     * @param user 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody  TbUser user, @RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return userService.search(page, rows, user);
    }

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 发送结果
     */
    @GetMapping("/sendSmsCode")
    public Result sendSmsCode(String phone){
        Result result = Result.fail("发送验证码失败");
        try {
            //验证手机号是否合法
            if (PhoneFormatCheckUtils.isPhoneLegal(phone)) {
                //发送短信验证码
                userService.sendSmsCode(phone);
                result = Result.ok("发送验证码成功");
            } else {
                result = Result.fail("手机号非法");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取当前登录用户名
     * @return 登录用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername(){
        Map<String, Object> map = new HashMap<>();

        //返回map可以以后放置用户的其它信息如：头像地址，个人信息

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);

        return map;
    }

}
