package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardPackageMapper;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserBatteryMemberCardPackage)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-12 14:44:01
 */
@Service("userBatteryMemberCardPackageService")
@Slf4j
public class UserBatteryMemberCardPackageServiceImpl implements UserBatteryMemberCardPackageService {
    @Resource
    private UserBatteryMemberCardPackageMapper userBatteryMemberCardPackageMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCardPackage queryByIdFromDB(Long id) {
        return this.userBatteryMemberCardPackageMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCardPackage queryByIdFromCache(Long id) {
        return null;
    }

    @Override
    public Integer insert(UserBatteryMemberCardPackage userBatteryMemberCardPackage) {
        return userBatteryMemberCardPackageMapper.insert(userBatteryMemberCardPackage);
    }

    /**
     * 修改数据
     *
     * @param userBatteryMemberCardPackage 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserBatteryMemberCardPackage userBatteryMemberCardPackage) {
        return this.userBatteryMemberCardPackageMapper.update(userBatteryMemberCardPackage);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.userBatteryMemberCardPackageMapper.deleteById(id) > 0;
    }
}
