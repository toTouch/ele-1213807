package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.mapper.EleDisableMemberCardRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleDisableMemberCardRecordVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.HRP
 * @create: 2022-05-21 10:54
 **/
@Service
@Slf4j
public class EleDisableMemberCardRecordServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements EleDisableMemberCardRecordService {
    
    @Resource
    EleDisableMemberCardRecordMapper eleDisableMemberCardRecordMapper;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    UserService userService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    @Override
    public int save(EleDisableMemberCardRecord eleDisableMemberCardRecord) {
        return eleDisableMemberCardRecordMapper.insert(eleDisableMemberCardRecord);
    }
    
    @Slave
    @Override
    public R list(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        List<EleDisableMemberCardRecordVO> eleDisableMemberCardRecordVOS = eleDisableMemberCardRecordMapper.queryList(electricityMemberCardRecordQuery);
        if (CollectionUtils.isEmpty(eleDisableMemberCardRecordVOS)) {
            return R.ok(Collections.emptyList());
        }
        
        eleDisableMemberCardRecordVOS.forEach(item -> {
            //            if(Objects.isNull(item.getDisableTime())){
            //                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(item.getUid());
            //                item.setDisableTime(Objects.isNull(userBatteryMemberCard)?null:userBatteryMemberCard.getDisableMemberCardTime());
            //            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getBatteryMemberCardId());
            item.setRentUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentUnit());
            item.setBusinessType(Objects.isNull(batteryMemberCard) ? BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode() : batteryMemberCard.getBusinessType());
            
            // 设置审核员名称
            if (!Objects.isNull(item.getAuditorId())) {
                User user = userService.queryByUidFromCache(item.getAuditorId());
                if (!Objects.isNull(user)) {
                    item.setAuditorName(user.getName());
                }
            }
            
            // 设置套餐剩余次数
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(item.getUid());
            if (Objects.nonNull(userBatteryMemberCard)) {
                item.setOrderRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
            }
        });
        
        return R.ok(eleDisableMemberCardRecordVOS);
    }
    
    @Override
    public EleDisableMemberCardRecord queryCreateTimeMaxEleDisableMemberCardRecord(Long uid, Integer tenantId) {
        return eleDisableMemberCardRecordMapper.queryCreateTimeMaxEleDisableMemberCardRecord(uid, tenantId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R reviewDisableMemberCard(String disableMemberCardNo, String errMsg, Integer status) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordMapper.selectOne(
                new LambdaQueryWrapper<EleDisableMemberCardRecord>().eq(EleDisableMemberCardRecord::getDisableMemberCardNo, disableMemberCardNo)
                        .eq(EleDisableMemberCardRecord::getTenantId, tenantId));
        if (Objects.isNull(eleDisableMemberCardRecord)) {
            log.error("REVIEW_DISABLE_MEMBER_CARD ERROR ,NOT FOUND DISABLE_MEMBER_CARD ORDER_NO={}", disableMemberCardNo);
            return R.fail("未找到停卡订单!");
        }
    
        if (!Objects.equals(eleDisableMemberCardRecord.getStatus(), EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW)) {
            return R.fail("订单状态异常!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDisableMemberCardRecord.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid={} ", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY  ERROR! not found franchisee,uid={}", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        //判断用户是否购买套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L) || Objects.equals(
                userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("REVIEW_DISABLE_MEMBER_CARD ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        if (Objects.equals(status, UserBatteryMemberCard.MEMBER_CARD_DISABLE) && (Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis())) {
            EleDisableMemberCardRecord updateEleDisableMemberCardRecord = new EleDisableMemberCardRecord();
            updateEleDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
            updateEleDisableMemberCardRecord.setStatus(EleDisableMemberCardRecord.MEMBER_CARD_EXPIRE);
            updateEleDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
            eleDisableMemberCardRecordMapper.updateById(updateEleDisableMemberCardRecord);
            
            //更新用户套餐状态为正常
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            return R.fail("100246", "套餐已过期，无法进行停卡审核");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY SERVICE FEE WARN! not found batteryMemberCard,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        EleDisableMemberCardRecord updateEleDisableMemberCardRecord = new EleDisableMemberCardRecord();
        updateEleDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
        updateEleDisableMemberCardRecord.setDisableMemberCardNo(disableMemberCardNo);
        updateEleDisableMemberCardRecord.setStatus(status);
        updateEleDisableMemberCardRecord.setErrMsg(errMsg);
        updateEleDisableMemberCardRecord.setDisableMemberCardTime(System.currentTimeMillis());
        updateEleDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
        updateEleDisableMemberCardRecord.setAuditorId(user.getUid());
        if (Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(), EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME) && Objects.equals(status,
                EleDisableMemberCardRecord.MEMBER_CARD_DISABLE)) {
            updateEleDisableMemberCardRecord.setDisableDeadline(System.currentTimeMillis() + eleDisableMemberCardRecord.getChooseDays() * (24 * 60 * 60 * 1000L));
        }
        
        eleDisableMemberCardRecordMapper.updateById(updateEleDisableMemberCardRecord);
        
        if (Objects.equals(status, UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE)) {
            //拒绝停卡
            UserBatteryMemberCard updateUserBatteryMemberCard = new UserBatteryMemberCard();
            updateUserBatteryMemberCard.setUid(userBatteryMemberCard.getUid());
            updateUserBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE);
            updateUserBatteryMemberCard.setDisableMemberCardTime(null);
            updateUserBatteryMemberCard.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUidForDisableCard(updateUserBatteryMemberCard);
            
            //停卡拒绝，解绑停卡信息
            ServiceFeeUserInfo updateServiceFeeUserInfo = new ServiceFeeUserInfo();
            updateServiceFeeUserInfo.setUid(userInfo.getUid());
            updateServiceFeeUserInfo.setUpdateTime(System.currentTimeMillis());
            updateServiceFeeUserInfo.setDisableMemberCardNo("");
            updateServiceFeeUserInfo.setPauseOrderNo("");
            serviceFeeUserInfoService.updateByUid(updateServiceFeeUserInfo);
        } else {
            //同意停卡
            UserBatteryMemberCard updateUserBatteryMemberCard = new UserBatteryMemberCard();
            updateUserBatteryMemberCard.setUid(userBatteryMemberCard.getUid());
            updateUserBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE);
            updateUserBatteryMemberCard.setUpdateTime(System.currentTimeMillis());
            updateUserBatteryMemberCard.setDisableMemberCardTime(System.currentTimeMillis());
            
            // 套餐过期时间需要加上冻结的时间
            Long frozenTime = eleDisableMemberCardRecord.getChooseDays() * TimeConstant.DAY_MILLISECOND;
            updateUserBatteryMemberCard.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() + frozenTime);
            updateUserBatteryMemberCard.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + frozenTime);
            userBatteryMemberCardService.updateByUid(updateUserBatteryMemberCard);
            
            //用户是否绑定电池
            if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                //记录企业用户冻结套餐记录
                enterpriseUserCostRecordService.asyncSaveUserCostRecordForBattery(userInfo.getUid(),
                        updateEleDisableMemberCardRecord.getId() + "_" + updateEleDisableMemberCardRecord.getDisableMemberCardNo(),
                        UserCostTypeEnum.COST_TYPE_FREEZE_PACKAGE.getCode(), updateEleDisableMemberCardRecord.getDisableMemberCardTime());
                sendUserOperateRecord(eleDisableMemberCardRecord, status);
                return R.ok();
            }
            
            //审核通过 生成滞纳金订单
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
            
            List<String> batteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
            Set<String> batteryTypeSet = null;
            if (CollectionUtils.isNotEmpty(batteryTypes)) {
                batteryTypeSet = new HashSet<>(batteryTypes);
            }
            
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder()
                    .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                    .name(userInfo.getName()).payAmount(BigDecimal.ZERO).status(EleDepositOrder.STATUS_INIT).batteryServiceFeeGenerateTime(System.currentTimeMillis())
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(tenantId).source(EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD)
                    .franchiseeId(franchisee.getId()).storeId(userInfo.getStoreId()).modelType(franchisee.getModelType())
                    .batteryType(CollectionUtils.isEmpty(batteryTypeSet) ? "" : JsonUtil.toJson(batteryTypeSet))
                    .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn()).batteryServiceFee(batteryMemberCard.getServiceCharge()).build();
            eleBatteryServiceFeeOrderService.insert(eleBatteryServiceFeeOrder);
            
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
            serviceFeeUserInfoUpdate.setPauseOrderNo(eleBatteryServiceFeeOrder.getOrderId());
            serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            
            //记录企业用户冻结套餐记录
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForBattery(userInfo.getUid(),
                    updateEleDisableMemberCardRecord.getId() + "_" + updateEleDisableMemberCardRecord.getDisableMemberCardNo(), UserCostTypeEnum.COST_TYPE_FREEZE_PACKAGE.getCode(),
                    updateEleDisableMemberCardRecord.getDisableMemberCardTime());
        }
        sendUserOperateRecord(eleDisableMemberCardRecord, status);
        return R.ok();
    }
    
    @Slave
    @Override
    public R queryCount(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        return R.ok(eleDisableMemberCardRecordMapper.queryCount(electricityMemberCardRecordQuery));
    }
    
    @Override
    public List<EleDisableMemberCardRecord> queryDisableCardExpireRecord(Integer offset, Integer size, Long nowTime) {
        return eleDisableMemberCardRecordMapper.queryDisableCardExpireRecord(offset, size, nowTime);
    }
    
    @Override
    public int updateBYId(EleDisableMemberCardRecord eleDisableMemberCardRecord) {
        return eleDisableMemberCardRecordMapper.updateById(eleDisableMemberCardRecord);
    }
    
    @Override
    public EleDisableMemberCardRecord queryByDisableMemberCardNo(String disableMemberCardNo, Integer tenantId) {
        return eleDisableMemberCardRecordMapper.queryByDisableMemberCardNo(disableMemberCardNo, tenantId);
    }
    
    @Override
    public EleDisableMemberCardRecord selectByDisableMemberCardNo(String disableMemberCardNo) {
        return eleDisableMemberCardRecordMapper.selectOne(
                new LambdaQueryWrapper<EleDisableMemberCardRecord>().eq(EleDisableMemberCardRecord::getDisableMemberCardNo, disableMemberCardNo));
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
        return eleDisableMemberCardRecordMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    
    private void sendUserOperateRecord(EleDisableMemberCardRecord eleDisableMemberCardRecord, Integer status) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("username", eleDisableMemberCardRecord.getUserName());
            map.put("phone", eleDisableMemberCardRecord.getPhone());
            map.put("packageName", eleDisableMemberCardRecord.getMemberCardName());
            map.put("approve", Objects.equals(status, EleBatteryServiceFeeOrder.STATUS_SUCCESS) ? 0 : 1);
            map.put("residue", eleDisableMemberCardRecord.getChooseDays());
            operateRecordUtil.record(null, map);
        } catch (Throwable e) {
            log.error("Recording user operation records failed because:", e);
        }
    }
}
