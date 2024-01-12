package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.LoginInfo;
import com.xiliulou.electricity.mapper.LoginInfoMapper;
import com.xiliulou.electricity.service.LoginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;

/**
 * 用户列表(LoginInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("loginInfoService")
@Slf4j
public class LoginInfoServiceImpl extends ServiceImpl<LoginInfoMapper, LoginInfo> implements LoginInfoService {

    @Resource
    LoginInfoMapper loginInfoMapper;

    @Override
    public void insert(LoginInfo loginInfo) {
        loginInfoMapper.insert(loginInfo);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return loginInfoMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
}