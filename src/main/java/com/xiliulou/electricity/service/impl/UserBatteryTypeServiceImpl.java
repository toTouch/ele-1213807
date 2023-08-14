package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserBatteryTypeMapper;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @Autowired
    private MemberCardBatteryTypeService memberCardBatteryTypeService;

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
        if (CollectionUtils.isEmpty(batteryTypes)) {
            return null;
        }

        return batteryTypes.stream().sorted(Comparator.comparing(item -> item.substring(item.length() - 2))).reduce((first, second) -> second).orElse(null);
    }

    @Override
    public String selectUserSimpleBatteryType(Long uid) {
        List<String> batteryTypes = this.selectByUid(uid);
        if (CollectionUtils.isEmpty(batteryTypes)) {
            return null;
        }

        String batteryType = batteryTypes.get(0);
        if (StringUtils.isBlank(batteryType)) {
            return null;
        }

        return batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserBatteryType(ElectricityMemberCardOrder electricityMemberCardOrder, UserInfo userInfo) {
        List<String> totalBatteryTypes = new ArrayList<>();

        List<String> userBindBatteryTypes = this.selectByUid(electricityMemberCardOrder.getUid());

        List<String> membercardBatteryTypes = memberCardBatteryTypeService.selectBatteryTypeByMid(electricityMemberCardOrder.getMemberCardId());

        if (CollectionUtils.isEmpty(userBindBatteryTypes)) {
            totalBatteryTypes = membercardBatteryTypes;
        }

        if (CollectionUtils.isEmpty(membercardBatteryTypes)) {
            totalBatteryTypes = userBindBatteryTypes;
        }

        if (CollectionUtils.isEmpty(userBindBatteryTypes) && CollectionUtils.isEmpty(membercardBatteryTypes)) {
            totalBatteryTypes = (List<String>) CollectionUtils.union(userBindBatteryTypes, membercardBatteryTypes);
        }

        if (CollectionUtils.isEmpty(totalBatteryTypes)) {
            log.error("ELE ERROR! totalBatteryTypes is null,uid={}", userInfo.getUid());
            return;
        }

        this.deleteByUid(electricityMemberCardOrder.getUid());

        this.batchInsert(buildUserBatteryType(totalBatteryTypes, userInfo));
    }
}
