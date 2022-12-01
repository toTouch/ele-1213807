package com.xiliulou.electricity.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.FranchiseeUserInfoMapper;
import com.xiliulou.electricity.query.BatteryMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.MemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户绑定列表(FranchiseeUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
@Service("franchiseeUserInfoService")
@Slf4j
public class FranchiseeUserInfoServiceImpl implements FranchiseeUserInfoService {
    @Resource
    private FranchiseeUserInfoMapper franchiseeUserInfoMapper;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    /**
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FranchiseeUserInfo franchiseeUserInfo) {
        return this.franchiseeUserInfoMapper.updateById(franchiseeUserInfo);

    }

    @Override
    public FranchiseeUserInfo queryByUserInfoId(Long id) {
        return franchiseeUserInfoMapper.selectOne(new LambdaQueryWrapper<FranchiseeUserInfo>()
                .eq(FranchiseeUserInfo::getUserInfoId, id).eq(FranchiseeUserInfo::getDelFlag, FranchiseeUserInfo.DEL_NORMAL));
    }

    @Override
    public FranchiseeUserInfo queryByUid(Long uid) {
        return franchiseeUserInfoMapper.queryFranchiseeUserInfoByUid(uid);
    }

   /* @Override
    public Integer queryCountByBatterySn(String electricityBatterySn) {
        return franchiseeUserInfoMapper.selectCount(new LambdaQueryWrapper<FranchiseeUserInfo>()
                .eq(FranchiseeUserInfo::getNowElectricityBatterySn,electricityBatterySn).eq(FranchiseeUserInfo::getDelFlag,FranchiseeUserInfo.DEL_NORMAL));
    }*/

    @Override
    @Deprecated
    public Integer unBind(FranchiseeUserInfo franchiseeUserInfo) {
        return franchiseeUserInfoMapper.unBind(franchiseeUserInfo);
    }

    @Override
    public Integer minCount(Long id) {
        return franchiseeUserInfoMapper.minCount(id);
    }

    @Override
    public Integer minCountForOffLineEle(Long id) {
        return franchiseeUserInfoMapper.minMemberCountForOffLineEle(id);
    }

    @Override
    public Integer plusCount(Long id) {
        return franchiseeUserInfoMapper.plusCount(id);
    }


    @Override
    public void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateByUserInfoId(franchiseeUserInfo);
    }

    @Override
    public Triple<Boolean, String, Object> updateServiceStatus(Long uid, Integer serviceStatus) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(serviceStatus, FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(userInfo.getUid());
            if (!Objects.isNull(battery)) {
                return Triple.of(false, "ELECTRICITY.0045", String.format("用户已绑定电池【%s】, 请先解绑！", battery.getSn()));
            }
        } else if (Objects.equals(serviceStatus, FranchiseeUserInfo.STATUS_IS_BATTERY)) {

        } else {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }

        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setServiceStatus(serviceStatus);
        franchiseeUserInfo.setUserInfoId(userInfo.getId());
        franchiseeUserInfo.setTenantId(TenantContextHolder.getTenantId());
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());

        this.updateByUserInfoId(franchiseeUserInfo);
        return Triple.of(true, "", null);
    }

    @Override
    public void updateRefund(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateRefund(franchiseeUserInfo);
    }

    @Override
    public FranchiseeUserInfo insert(FranchiseeUserInfo insertFranchiseeUserInfo) {
        franchiseeUserInfoMapper.insert(insertFranchiseeUserInfo);
        return insertFranchiseeUserInfo;
    }

    @Override
    public Integer queryCountByFranchiseeId(Long id) {
        return franchiseeUserInfoMapper.selectCount(new LambdaQueryWrapper<FranchiseeUserInfo>().eq(FranchiseeUserInfo::getFranchiseeId, id).last("limit 0,1"));
    }

    @Override
    public void updateByOrder(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateByOrder(franchiseeUserInfo);
    }

    @Override
    public List<FranchiseeUserInfo> selectByMemberCardId(Integer memberCardId, Integer tenantId) {
        return franchiseeUserInfoMapper.selectList(new LambdaQueryWrapper<FranchiseeUserInfo>().eq(FranchiseeUserInfo::getCardId, memberCardId).eq(FranchiseeUserInfo::getTenantId, tenantId)
                .eq(FranchiseeUserInfo::getDelFlag, FranchiseeUserInfo.DEL_NORMAL));
    }

    @Override
    public List<FranchiseeUserInfo> selectByFranchiseeId(Long id) {
        return franchiseeUserInfoMapper.selectList(new LambdaQueryWrapper<FranchiseeUserInfo>().eq(FranchiseeUserInfo::getFranchiseeId, id)
                .eq(FranchiseeUserInfo::getDelFlag, FranchiseeUserInfo.DEL_NORMAL));
    }

    @Override
    public List<MemberCardExpiringSoonQuery> queryMemberCardExpiringSoon(int offset, int size, long startExpireTime, long endExpireTime) {
        return franchiseeUserInfoMapper.queryMemberCardExpiringSoon(offset, size, startExpireTime, endExpireTime);
    }

    @Override
    public void updateMemberCardExpire(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateMemberCardExpire(franchiseeUserInfo);
    }

    @Override
    @Deprecated
    public FranchiseeUserInfo selectByNowBattery(String batteryName) {
        return franchiseeUserInfoMapper.selectByNowBattery(batteryName);
    }

    @Override
    public Integer unBindNowBatterySn(FranchiseeUserInfo franchiseeUserInfo) {
        return franchiseeUserInfoMapper.unBindNowBatterySn(franchiseeUserInfo);
    }

    @Override
    public List<BatteryMemberCardExpiringSoonQuery> batteryMemberCardExpire(Integer offset, Integer size,
                                                                            Long firstTime, Long lastTime) {
        return franchiseeUserInfoMapper.batteryMemberCardExpire(offset, size, firstTime, lastTime);
    }

    @Override
    public List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(Integer offset, Integer size, Long firstTime,
                                                                    Long lastTime) {
        return franchiseeUserInfoMapper.carMemberCardExpire(offset, size, firstTime, lastTime);
    }

    @Override
    public R queryBattery() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryBattery  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }


        //
        FranchiseeUserInfo franchiseeUserInfo = queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("queryBattery  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }
        return R.ok(franchiseeUserInfo);
    }

    @Override
    public R updateBattery(String batteryType) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryBattery  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }


        //
        FranchiseeUserInfo franchiseeUserInfo = queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("queryBattery  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
        franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
        franchiseeUserInfoUpdate.setBatteryType(batteryType);
        update(franchiseeUserInfoUpdate);
        return R.ok();
    }

    @Override
    public void updateOrderByUserInfoId(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateOrderByUserInfoId(franchiseeUserInfo);
    }

    @Override
    public Integer deleteByUserInfoId(Long userInfoId) {
        return franchiseeUserInfoMapper.delete(new LambdaQueryWrapper<FranchiseeUserInfo>().eq(FranchiseeUserInfo::getUserInfoId, userInfoId));
    }

    @Override
    public EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid) {
        //获取新用户所绑定的加盟商的电池服务费
        Franchisee franchisee = franchiseeService.queryByUserId(uid);
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = new EleBatteryServiceFeeVO();
        //计算用户所产生的电池服务费
        if (Objects.isNull(franchisee)) {
            return eleBatteryServiceFeeVO;
        }

        Integer modelType = franchisee.getModelType();
        if (Objects.equals(modelType, Franchisee.OLD_MODEL_TYPE) && Objects.equals(franchisee.getBatteryServiceFee(), BigDecimal.valueOf(0))) {
            return eleBatteryServiceFeeVO;
        }

        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoMapper.queryFranchiseeUserInfoByUid(uid);
        if (Objects.isNull(franchiseeUserInfo)) {
            return eleBatteryServiceFeeVO;
        }
        eleBatteryServiceFeeVO.setBatteryServiceFee(franchisee.getBatteryServiceFee());
        eleBatteryServiceFeeVO.setMemberCardStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);

        if (Objects.isNull(franchiseeUserInfo.getBatteryServiceFeeGenerateTime())) {
            return eleBatteryServiceFeeVO;
        }

        eleBatteryServiceFeeVO.setModelType(franchisee.getModelType());

        BigDecimal userChangeServiceFee=BigDecimal.valueOf(0);
        Long now = System.currentTimeMillis();
        long cardDays = 0;
        //用户产生的套餐过期电池服务费
        if (Objects.nonNull(franchiseeUserInfo.getBatteryServiceFeeGenerateTime())) {
            cardDays = (now - franchiseeUserInfo.getBatteryServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            BigDecimal serviceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(franchiseeUserInfo, franchisee, cardDays);
            userChangeServiceFee = serviceFee;
        }

        //用户产生的停卡电池服务费
        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE) && Objects.equals(franchiseeUserInfo.getBatteryServiceFeeStatus(), FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE)) {
            eleBatteryServiceFeeVO.setMemberCardStatus(FranchiseeUserInfo.MEMBER_CARD_DISABLE);
            cardDays = (now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
            //不足一天按一天计算
            double time = Math.ceil((now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }

            BigDecimal batteryServiceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(franchiseeUserInfo, uid, cardDays, null);
            userChangeServiceFee = batteryServiceFee;
        }
//        if (Objects.nonNull(franchiseeUserInfo.getServiceStatus()) && Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY) && cardDays >= 1) {
//            //查询用户是否存在电池服务费
//            if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
//                Integer model = BatteryConstant.acquireBattery(franchiseeUserInfo.getBatteryType());
//                eleBatteryServiceFeeVO.setModel(model);
//                List<ModelBatteryDeposit> list = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
//
//                eleBatteryServiceFeeVO.setModelBatteryServiceFeeList(list);
//
//                for (ModelBatteryDeposit modelBatteryDeposit : list) {
//                    if (Objects.equals(model, modelBatteryDeposit.getModel())) {
//                        //计算服务费
//                        BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
//                        eleBatteryServiceFeeVO.setUserBatteryServiceFee(batteryServiceFee);
//                        return eleBatteryServiceFeeVO;
//                    }
//                }
//            } else {
//                //计算服务费
//                BigDecimal batteryServiceFee = queryServiceFeeChargeRateByMemberCardStatus(franchiseeUserInfo, franchisee, uid).multiply(new BigDecimal(cardDays));
//                eleBatteryServiceFeeVO.setUserBatteryServiceFee(batteryServiceFee);
//                return eleBatteryServiceFeeVO;
//            }
//        }
        eleBatteryServiceFeeVO.setUserBatteryServiceFee(userChangeServiceFee);

        return eleBatteryServiceFeeVO;
    }

    @Override
    public void updateRentCar(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateRentCar(franchiseeUserInfo);
    }

    @Override
    public void modifyRentCarStatus(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.modifyRentCarStatus(franchiseeUserInfo);
    }

    @Override
    public void modifyRentCarStatusByUserInfoId(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.modifyRentCarStatusByUserInfoId(franchiseeUserInfo);
    }

    private BigDecimal queryServiceFeeChargeRateByMemberCardStatus(FranchiseeUserInfo franchiseeUserInfo, Franchisee franchisee, Long uid) {
        BigDecimal batteryServiceFee = franchisee.getBatteryServiceFee();

        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(uid, franchiseeUserInfo.getTenantId());
            batteryServiceFee = eleDisableMemberCardRecord.getChargeRate();
        }
        return batteryServiceFee;
    }
}
