package com.xiliulou.electricity.service.impl.enterprise;

import com.google.common.collect.Lists;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.EnterpriseUserCostRecordDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseUserCostRecord;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.enterprise.EnterpriseUserCostRecordTypeEnum;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseUserCostRecordMapper;
import com.xiliulou.electricity.mq.producer.EnterpriseUserCostRecordProducer;
import com.xiliulou.electricity.query.enterprise.EnterpriseUserCostRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostDetailsVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostRecordRemarkVO;
import lombok.extern.slf4j.Slf4j;
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
    
    @Override
    public List<EnterpriseUserCostDetailsVO> queryUserCostRecordList(EnterpriseUserCostRecordQuery enterpriseUserCostRecordQuery) {
    
        List<EnterpriseUserCostDetailsVO> enterpriseUserCostDetailsVOList = Lists.newArrayList();
        List<EnterpriseUserCostRecord> enterpriseUserCostRecordList = enterpriseUserCostRecordMapper.selectUserCostList(enterpriseUserCostRecordQuery);
        
        for (EnterpriseUserCostRecord enterpriseUserCostRecord : enterpriseUserCostRecordList){
    
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
    
            enterpriseUserCostDetailsVO.setPackageId(enterpriseUserCostRecord.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterpriseUserCostRecord.getPackageName());
            enterpriseUserCostDetailsVO.setEnterpriseId(enterpriseUserCostRecord.getEnterpriseId());
            enterpriseUserCostDetailsVO.setUid(enterpriseUserCostRecord.getUid());
            enterpriseUserCostDetailsVO.setOrderNo(enterpriseUserCostRecord.getOrderId());
            enterpriseUserCostDetailsVO.setCostType(enterpriseUserCostRecord.getCostType());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseUserCostRecord.getCreateTime());
    
            String remark = enterpriseUserCostRecord.getRemark();
            if(StringUtils.isNotEmpty(remark)){
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
        
        if(!BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())){
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
        //enterpriseUserCostRecordProducer.sendAsyncMessage(JsonUtil.toJson(enterpriseUserCostRecordDTO));
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
    
        if(!BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())){
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
        enterpriseUserCostRecordProducer.sendAsyncMessage(JsonUtil.toJson(enterpriseUserCostRecordDTO));
    
    }
    
    
}
