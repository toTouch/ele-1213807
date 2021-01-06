package com.xiliulou.electricity.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.electricity.entity.LoginInfo;


/**
 * 用户列表(LoginInfo)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface LoginInfoService extends IService<LoginInfo> {

    void insert(LoginInfo loginInfo);
}