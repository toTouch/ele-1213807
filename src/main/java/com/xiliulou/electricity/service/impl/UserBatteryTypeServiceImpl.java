package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserBatteryTypeMapper;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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

    @Autowired
    private UserInfoService userInfoService;

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
    public String selectOneByUid(Long uid) {
        return this.userBatteryTypeMapper.selectOneByUid(uid);
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
    public Triple<Boolean, String, Object> selectUserBatteryTypeByUid(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(TenantContextHolder.getTenantId(), userInfo.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        return Triple.of(true, "", this.selectOneByUid(uid));
    }

    @Override
    public Triple<Boolean, String, Object> modifyUserBatteryType(UserBatteryType userBatteryType) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(userBatteryType.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(TenantContextHolder.getTenantId(), userInfo.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }

        if(StringUtils.isNotBlank(userBatteryType.getBatteryType())){
            String batteryType = this.selectOneByUid(userBatteryType.getUid());
            if (StringUtils.isNotBlank(batteryType)) {
                UserBatteryType userBatteryTypeUpdate = new UserBatteryType();
                userBatteryTypeUpdate.setUid(userBatteryType.getUid());
                userBatteryTypeUpdate.setBatteryType(userBatteryType.getBatteryType());
                userBatteryTypeUpdate.setUpdateTime(System.currentTimeMillis());
                this.userBatteryTypeMapper.updateByUid(userBatteryTypeUpdate);
            } else {
                UserBatteryType userBatteryTypeInsert = new UserBatteryType();
                userBatteryTypeInsert.setUid(userBatteryType.getUid());
                userBatteryTypeInsert.setBatteryType(userBatteryType.getBatteryType());
                userBatteryTypeInsert.setTenantId(TenantContextHolder.getTenantId());
                userBatteryTypeInsert.setDelFlag(UserBatteryType.DEL_NORMAL);
                userBatteryTypeInsert.setCreateTime(System.currentTimeMillis());
                userBatteryTypeInsert.setUpdateTime(System.currentTimeMillis());
                this.userBatteryTypeMapper.insert(userBatteryTypeInsert);
            }
        }else{
            this.userBatteryTypeMapper.deleteByUid(userBatteryType.getUid());
        }

        return Triple.of(true, null, null);
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
        Set<String> totalBatteryTypes = new HashSet<>();

        List<String> userBindBatteryTypes = this.selectByUid(electricityMemberCardOrder.getUid());

        List<String> membercardBatteryTypes = memberCardBatteryTypeService.selectBatteryTypeByMid(electricityMemberCardOrder.getMemberCardId());

        if (CollectionUtils.isNotEmpty(userBindBatteryTypes)) {
            totalBatteryTypes.addAll(userBindBatteryTypes);
        }

        if (CollectionUtils.isNotEmpty(membercardBatteryTypes)) {
            totalBatteryTypes.addAll(membercardBatteryTypes);
        }

        if (CollectionUtils.isEmpty(totalBatteryTypes)) {
            log.info("ELE INFO! totalBatteryTypes is null,uid={}", userInfo.getUid());
            return;
        }

        this.deleteByUid(electricityMemberCardOrder.getUid());

        this.batchInsert(buildUserBatteryType(new ArrayList<>(totalBatteryTypes), userInfo));
    }

    /**
     * 同步用户电池型号数据
     * @param uid 用户uid
     * @param batteryTypes 套餐包含的电池型号列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void synchronizedUserBatteryType(Long uid, Integer tenantId, List<String> batteryTypes) {
        Set<String> totalBatteryTypes = new HashSet<>();

        List<String> userBindBatteryTypes = this.selectByUid(uid);

        if (CollectionUtils.isNotEmpty(userBindBatteryTypes)) {
            totalBatteryTypes.addAll(userBindBatteryTypes);
        }

        if (CollectionUtils.isNotEmpty(batteryTypes)) {
            totalBatteryTypes.addAll(batteryTypes);
        }

        if (CollectionUtils.isEmpty(totalBatteryTypes)) {
            log.error("ELE ERROR! synchronized user batteryType error, totalBatteryTypes is null,uid={}", uid);
            return;
        }

        this.deleteByUid(uid);

        List<UserBatteryType> list = new ArrayList<>(totalBatteryTypes.size());
        for (String batteryType : totalBatteryTypes) {
            UserBatteryType userBatteryType = new UserBatteryType();
            userBatteryType.setUid(uid);
            userBatteryType.setBatteryType(batteryType);
            userBatteryType.setTenantId(tenantId);
            userBatteryType.setDelFlag(UserBatteryType.DEL_NORMAL);
            userBatteryType.setCreateTime(System.currentTimeMillis());
            userBatteryType.setUpdateTime(System.currentTimeMillis());
            list.add(userBatteryType);
        }

        this.batchInsert(list);
    }
}
