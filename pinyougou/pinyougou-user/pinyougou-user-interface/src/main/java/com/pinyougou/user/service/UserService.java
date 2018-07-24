package com.pinyougou.user.service;

import com.pinyougou.pojo.TbUser;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface UserService extends BaseService<TbUser> {

    PageResult search(Integer page, Integer rows, TbUser user);

    /**
     * 发送短信验证码
     * @param mobile 手机号
     */
    void sendSmsCode(String mobile);

    /**
     * 将前台传递的验证码与在redis存放的验证码进行对比；如果一致则说明验证码正确（删除redis中的验证码）可以注册，否则不可注册提示用户验证码输入错误
     * @param phone 手机号
     * @param smsCode 用户输入的验证码
     * @return 验证结果
     */
    boolean checkSmsCode(String phone, String smsCode);
}