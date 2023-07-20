package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserBattery;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserBatteryTypeMapper;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * (UserBatteryType)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-14 16:02:42
 */
@Service("userBatteryTypeService")
@Slf4j
public class UserBatteryTypeServiceImpl implements UserBatteryTypeService {
    @Resource
    private UserBatteryTypeMapper userBatteryTypeMapper;

    @Override
    public UserBatteryType queryByIdFromDB(Long id) {
        return this.userBatteryTypeMapper.queryById(id);
    }

    @Override
    public UserBatteryType queryByIdFromCache(Long id) {
        return null;
    }

    @Override
    public Integer insert(UserBatteryType userBatteryType) {
        return this.userBatteryTypeMapper.insert(userBatteryType);
    }

    @Override
    public Integer batchInsert(List<UserBatteryType> userBatteryType) {
        return this.userBatteryTypeMapper.batchInsert(userBatteryType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserBatteryType userBatteryType) {
        return this.userBatteryTypeMapper.update(userBatteryType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.userBatteryTypeMapper.deleteById(id) > 0;
    }

    @Override
    public Integer deleteByUid(Long uid) {
        return this.userBatteryTypeMapper.deleteByUid(uid);
    }

    @Override
    public List<String> selectByUid(Long uid) {
        return this.userBatteryTypeMapper.selectByUid(uid);
    }

    @Override
    public String selectUserMaxBatteryType(Long uid) {
        List<String> batteryTypes = this.selectByUid(uid);
        if(CollectionUtils.isEmpty(batteryTypes)){
            return null;
        }

        return batteryTypes.stream().sorted(Comparator.comparing(item -> item.substring(item.length() - 2))).reduce((first, second) -> second).orElse(null);
    }

    @Override
    public List<UserBatteryType> buildUserBatteryType(List<String> batteryTypeList, UserInfo userInfo) {
        List<UserBatteryType> list = new ArrayList<>(batteryTypeList.size());

        for (String batteryType : batteryTypeList) {
            UserBatteryType userBatteryType = new UserBatteryType();
            userBatteryType.setUid(userInfo.getUid());
            userBatteryType.setBatteryType(batteryType);
            userBatteryType.setTenantId(userInfo.getTenantId());
            userBatteryType.setDelFlag(UserBatteryType.DEL_NORMAL);
            userBatteryType.setCreateTime(System.currentTimeMillis());
            userBatteryType.setUpdateTime(System.currentTimeMillis());

            list.add(userBatteryType);
        }

        return list;
    }
}
