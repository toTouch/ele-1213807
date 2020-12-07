package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoAddAndUpdate;
import com.xiliulou.electricity.service.UserInfoService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * 用户列表(TUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserInfo queryByIdFromDB(Long id) {
        return this.userInfoMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserInfo queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo insert(UserInfo userInfo) {
        this.userInfoMapper.insert(userInfo);
        return userInfo;
    }

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserInfo userInfo) {
       return this.userInfoMapper.update(userInfo);
         
    }

    @Override
    public R bindBattery(UserInfoAddAndUpdate userInfoAddAndUpdate) {
        UserInfo oldUserInfo=queryByIdFromDB(userInfoAddAndUpdate.getId());
        if(Objects.isNull(oldUserInfo)){
            return R.fail("ELECTRICITY.0019","未找到用户");
        }
        UserInfo userInfo=new UserInfo();
        BeanUtil.copyProperties(userInfoAddAndUpdate,userInfo);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setDelFlag(UserInfo.DEL_NORMAL);
        userInfo.setServiceStatus(UserInfo.IS_SERVICE_STATUS);
        userInfoMapper.update(userInfo);
        // TODO 电池绑定用户 YG
        return R.ok();
    }
}