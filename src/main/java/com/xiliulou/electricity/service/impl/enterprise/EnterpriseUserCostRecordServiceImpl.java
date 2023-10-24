package com.xiliulou.electricity.service.impl.enterprise;

import com.google.common.collect.Lists;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.EnterpriseUserCostRecordDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseUserCostRecord;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.enterprise.EnterpriseUserCostRecordTypeEnum;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseUserCostRecordMapper;
import com.xiliulou.electricity.mq.producer.EnterpriseUserCostRecordProducer;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseUserCostRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import com.xiliulou.electricity.vo.UserBatteryMemberCardInfoVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostDetailsVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostRecordRemarkVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserPackageDetailsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 11:12
 */

@Service("enterpriseUserCostRecordService")
@Slf4j
public class EnterpriseUserCostRecordServiceImpl implements EnterpriseUserCostRecordService {
    
    @Resource
    EnterpriseUserCostRecordMapper enterpriseUserCostRecordMapper;
    
    @Resource
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    EnterpriseUserCostRecordProducer enterpriseUserCostRecordProducer;
    
    @Resource
    EleDepositOrderService eleDepositOrderService;
    
    @Resource
    UserInfoService userInfoService;
    
    @Resource
    FranchiseeService franchiseeService;
    
    @Resource
    UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    InsuranceUserInfoService insuranceUserInfoService;
    
    @Override
    public Triple<Boolean, String, Object> queryRiderDetails(EnterpriseMemberCardQuery query) {
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("query rider details info, not found userInfo,uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
    
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
    
        enterpriseUserPackageDetailsVO.setModelType(Objects.isNull(franchisee) ? null : franchisee.getModelType());
        enterpriseUserPackageDetailsVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        enterpriseUserPackageDetailsVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        enterpriseUserPackageDetailsVO.setFranchiseeId(userInfo.getFranchiseeId());
        enterpriseUserPackageDetailsVO.setStoreId(userInfo.getStoreId());
        enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.NO);
    
        //设置骑手个人信息
        enterpriseUserPackageDetailsVO.setUid(userInfo.getUid());
        enterpriseUserPackageDetailsVO.setName(userInfo.getName());
        enterpriseUserPackageDetailsVO.setPhone(userInfo.getPhone());
        enterpriseUserPackageDetailsVO.setIdNumber(userInfo.getIdNumber());
    
        //查询骑手续费方式
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(query.getEnterpriseId(), query.getUid());
        log.info("query enterprise channel user, enterprise id = {}, uid = {}", query.getEnterpriseId(), query.getUid());
        if (Objects.isNull(enterpriseChannelUserVO)) {
            log.warn("query rider details info, not found enterprise channel user, uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        enterpriseUserPackageDetailsVO.setRenewalStatus(enterpriseChannelUserVO.getRenewalStatus());
    
        //判断是否存在orderNo参数，若不存在，则仅查看未代付用户记录信息
        if(StringUtils.isEmpty(query.getOrderNo())){
            log.warn("query rider details info, order no is null");
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
    
        //根据orderNo查询套餐购买记录信息
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(query.getOrderNo());
        if(Objects.isNull(electricityMemberCardOrder)){
            log.warn("query rider details info, query order info by order no, but order is null, order no = {}", query.getOrderNo());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        //根据套餐ID查询套餐详细信息
        BatteryMemberCard batteryPackage = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
    
        enterpriseUserPackageDetailsVO.setOrderId(electricityMemberCardOrder.getOrderId());
        enterpriseUserPackageDetailsVO.setMemberCardExpireTime(null);
        enterpriseUserPackageDetailsVO.setMemberCardId(batteryPackage.getId());
        enterpriseUserPackageDetailsVO.setMemberCardName(batteryPackage.getName());
        enterpriseUserPackageDetailsVO.setRentUnit(batteryPackage.getRentUnit());
        enterpriseUserPackageDetailsVO.setLimitCount(batteryPackage.getLimitCount());
        enterpriseUserPackageDetailsVO.setBatteryMembercardPayAmount(electricityMemberCardOrder.getPayAmount());
        enterpriseUserPackageDetailsVO.setMemberCardPayTime(electricityMemberCardOrder.getCreateTime());
    
        //获取关联押金信息
        EleDepositOrderVO eleDepositOrderVO = eleDepositOrderService.queryByUidAndSourceOrderNo(query.getUid(), electricityMemberCardOrder.getOrderId());
        if (Objects.nonNull(eleDepositOrderVO)) {
            enterpriseUserPackageDetailsVO.setBatteryDeposit(eleDepositOrderVO.getPayAmount());
            enterpriseUserPackageDetailsVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
        } else {
            //免押设置
            // enterprisePackageOrderVO.setBatteryDeposit(BigDecimal.ZERO);
            enterpriseUserPackageDetailsVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        }
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("query rider details failed, not found userBatteryMemberCard,uid = {}", userInfo.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
    
        //判断当前查询订单号关联套餐，是否和会员表中的套餐ID一致，若一致，则代表当前套餐为正在使用中的套餐
        if(userBatteryMemberCard.getMemberCardId().equals(electricityMemberCardOrder.getMemberCardId())){
            enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
            enterpriseUserPackageDetailsVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
            enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
            enterpriseUserPackageDetailsVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
            enterpriseUserPackageDetailsVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());
    
            if (Objects.equals(batteryPackage.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
                enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                        (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000.0) : 0);
            } else {
                enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                        (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 60 / 1000.0) : 0);
            }
    
            //查询用户当前押金状况
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit) || org.apache.commons.lang3.StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
                log.warn("query rider details failed, not found userBatteryDeposit,uid = {}", userInfo.getUid());
                return Triple.of(true, null, enterpriseUserPackageDetailsVO);
            }
    
            enterpriseUserPackageDetailsVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
            enterpriseUserPackageDetailsVO.setDepositType(userBatteryDeposit.getDepositType());
    
            //用户电池型号
            enterpriseUserPackageDetailsVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));
    
            //查询用户保险信息
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
            if (Objects.nonNull(insuranceUserInfo)) {
                BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
            }
            enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
        }
        
        return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        
    }
    
    @Override
    public List<EnterpriseUserCostDetailsVO> queryUserCostRecordList(EnterpriseUserCostRecordQuery enterpriseUserCostRecordQuery) {
        List<EnterpriseUserCostDetailsVO> enterpriseUserCostDetailsVOList = Lists.newArrayList();
        List<EnterpriseUserCostRecord> enterpriseUserCostRecordList = enterpriseUserCostRecordMapper.selectUserCostList(enterpriseUserCostRecordQuery);
        
        for (EnterpriseUserCostRecord enterpriseUserCostRecord : enterpriseUserCostRecordList) {
            
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            
            enterpriseUserCostDetailsVO.setPackageId(enterpriseUserCostRecord.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterpriseUserCostRecord.getPackageName());
            enterpriseUserCostDetailsVO.setEnterpriseId(enterpriseUserCostRecord.getEnterpriseId());
            enterpriseUserCostDetailsVO.setUid(enterpriseUserCostRecord.getUid());
            enterpriseUserCostDetailsVO.setOrderNo(enterpriseUserCostRecord.getOrderId());
            enterpriseUserCostDetailsVO.setCostType(enterpriseUserCostRecord.getCostType());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseUserCostRecord.getCreateTime());
            
            String remark = enterpriseUserCostRecord.getRemark();
            if (StringUtils.isNotEmpty(remark)) {
                EnterpriseUserCostRecordRemarkVO enterpriseUserCostRecordRemarkVO = JsonUtil.fromJson(remark, EnterpriseUserCostRecordRemarkVO.class);
                enterpriseUserCostDetailsVO.setPayAmount(enterpriseUserCostRecordRemarkVO.getPayAmount());
                enterpriseUserCostDetailsVO.setDepositAmount(enterpriseUserCostRecordRemarkVO.getDepositAmount());
                enterpriseUserCostDetailsVO.setInsuranceAmount(enterpriseUserCostRecordRemarkVO.getInsuranceAmount());
            }
            
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        return enterpriseUserCostDetailsVOList;
    }
    
    @Override
    public EnterpriseUserCostRecordDTO buildUserCostRecordForPurchasePackage(Long uid, String orderId, Long enterpriseId, Long packageId, Integer costType) {
        return null;
    }
    
    @Override
    public void asyncSaveUserCostRecordForBattery(Long uid, String orderId, Integer costType, Long createTime) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("enterprise channel user is null, uid = {}", uid);
            return;
        }
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("user battery memberCard is null, uid = {}", uid);
            return;
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("battery memberCard is null, memberCardId = {}, uid = {}", userBatteryMemberCard.getMemberCardId(), uid);
            return;
        }
        
        if (!BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())) {
            log.warn("the current package is not a enterprise package, memberCardId = {}, uid = {}", userBatteryMemberCard.getMemberCardId(), uid);
            return;
        }
        
        //记录企业代付订单信息
        EnterpriseUserCostRecordDTO enterpriseUserCostRecordDTO = new EnterpriseUserCostRecordDTO();
        enterpriseUserCostRecordDTO.setUid(uid);
        enterpriseUserCostRecordDTO.setEnterpriseId(enterpriseChannelUser.getEnterpriseId());
        enterpriseUserCostRecordDTO.setOrderId(orderId);
        enterpriseUserCostRecordDTO.setPackageId(batteryMemberCard.getId());
        enterpriseUserCostRecordDTO.setPackageName(batteryMemberCard.getName());
        enterpriseUserCostRecordDTO.setCostType(costType);
        enterpriseUserCostRecordDTO.setType(EnterpriseUserCostRecordTypeEnum.USER_COST_TYPE_BATTERY.getCode());
        enterpriseUserCostRecordDTO.setTenantId(tenantId.longValue());
        enterpriseUserCostRecordDTO.setCreateTime(createTime);
        enterpriseUserCostRecordDTO.setUpdateTime(System.currentTimeMillis());
        enterpriseUserCostRecordDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
        
        String message = JsonUtil.toJson(enterpriseUserCostRecordDTO);
        //MQ处理企业代付订单信息
        log.info("Async save enterprise user cost record.send async message, message is {}", message);
        enterpriseUserCostRecordProducer.sendAsyncMessage(message);
    }
    
    @Override
    public void asyncSaveUserCostRecordForRefundDeposit(Long uid, Integer costType, EleRefundOrder eleRefundOrder) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            return;
        }
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            return;
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return;
        }
        
        if (!BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())) {
            return;
        }
        
        //记录企业代付订单信息
        EnterpriseUserCostRecordDTO enterpriseUserCostRecordDTO = new EnterpriseUserCostRecordDTO();
        enterpriseUserCostRecordDTO.setUid(uid);
        enterpriseUserCostRecordDTO.setEnterpriseId(enterpriseChannelUser.getEnterpriseId());
        enterpriseUserCostRecordDTO.setOrderId(eleRefundOrder.getRefundOrderNo());
        enterpriseUserCostRecordDTO.setPackageId(batteryMemberCard.getId());
        enterpriseUserCostRecordDTO.setPackageName(batteryMemberCard.getName());
        enterpriseUserCostRecordDTO.setCostType(costType);
        enterpriseUserCostRecordDTO.setType(EnterpriseUserCostRecordTypeEnum.USER_COST_TYPE_BATTERY.getCode());
        enterpriseUserCostRecordDTO.setTenantId(tenantId.longValue());
        enterpriseUserCostRecordDTO.setCreateTime(eleRefundOrder.getCreateTime());
        enterpriseUserCostRecordDTO.setUpdateTime(System.currentTimeMillis());
        enterpriseUserCostRecordDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
        
        EnterpriseUserCostRecordRemarkVO enterpriseUserCostRecordRemarkVO = new EnterpriseUserCostRecordRemarkVO();
        enterpriseUserCostRecordRemarkVO.setPayAmount(eleRefundOrder.getPayAmount());
        enterpriseUserCostRecordRemarkVO.setDepositAmount(eleRefundOrder.getRefundAmount());
        enterpriseUserCostRecordDTO.setRemark(JsonUtil.toJson(enterpriseUserCostRecordRemarkVO));
        
        String message = JsonUtil.toJson(enterpriseUserCostRecordDTO);
        
        //MQ处理企业代付订单信息
        log.info("Async save enterprise user cost record for refund deposit. send async message, message is {}", message);
        enterpriseUserCostRecordProducer.sendAsyncMessage(message);
        
    }
    
    
}
