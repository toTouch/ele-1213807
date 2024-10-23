package com.xiliulou.electricity.service.impl.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.mapper.AnotherPayMembercardRecordMapper;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 代付记录表(AnotherPayMembercardRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-10-10 15:07:39
 */
@Service("anotherPayMembercardRecordService")
@Slf4j
public class AnotherPayMembercardRecordServiceImpl implements AnotherPayMembercardRecordService {
    
    @Resource
    private AnotherPayMembercardRecordMapper anotherPayMembercardRecordMapper;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    UserInfoService userInfoService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AnotherPayMembercardRecord queryByIdFromDB(Long id) {
        return this.anotherPayMembercardRecordMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AnotherPayMembercardRecord queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param anotherPayMembercardRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(AnotherPayMembercardRecord anotherPayMembercardRecord) {
        return this.anotherPayMembercardRecordMapper.update(anotherPayMembercardRecord);
        
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
        return this.anotherPayMembercardRecordMapper.deleteById(id) > 0;
    }
    
    @Override
    public int saveAnotherPayMembercardRecord(Long uid, String orderId, Integer tenantId) {
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.error("save Another Pay Membercard Record error!not found electricityMemberCardOrder,orderId={}", orderId);
            return 0;
        }
        
        Long startTime = System.currentTimeMillis();
        Long endTime = System.currentTimeMillis() + electricityMemberCardOrder.getValidDays() * 24 * 60 * 60 * 1000L;
        AnotherPayMembercardRecord latestAnotherPayMembercardRecord = this.anotherPayMembercardRecordMapper.selectLatestByUid(uid);
        if (Objects.nonNull(latestAnotherPayMembercardRecord)) {
            startTime = latestAnotherPayMembercardRecord.getEndTime();
            endTime = latestAnotherPayMembercardRecord.getEndTime() + electricityMemberCardOrder.getValidDays() * 24 * 60 * 60 * 1000L;
        }
        
        AnotherPayMembercardRecord anotherPayMembercardRecord = new AnotherPayMembercardRecord();
        anotherPayMembercardRecord.setOrderId(orderId);
        anotherPayMembercardRecord.setUid(uid);
        anotherPayMembercardRecord.setBeginTime(startTime);
        anotherPayMembercardRecord.setEndTime(endTime);
        anotherPayMembercardRecord.setCreateTime(System.currentTimeMillis());
        anotherPayMembercardRecord.setUpdateTime(System.currentTimeMillis());
        anotherPayMembercardRecord.setTenantId(tenantId);
        
        return this.anotherPayMembercardRecordMapper.insert(anotherPayMembercardRecord);
    }
    
    @Slave
    @Override
    public List<AnotherPayMembercardRecord> selectByUid(Long uid) {
        return this.anotherPayMembercardRecordMapper.selectList(new LambdaQueryWrapper<AnotherPayMembercardRecord>().eq(AnotherPayMembercardRecord::getUid, uid));
    }
    
    @Override
    public int deleteByUid(Long uid) {
        return this.anotherPayMembercardRecordMapper.deleteByUid(uid);
    }
    
    @Slave
    @Override
    public List<AnotherPayMembercardRecord> selectListByEnterpriseId(Long enterpriseId) {
        return this.anotherPayMembercardRecordMapper.selectListByEnterpriseId(enterpriseId);
    }
    
    @Override
    public AnotherPayMembercardRecord selectByOrderId(String orderId) {
        return this.anotherPayMembercardRecordMapper.selectOne(new LambdaQueryWrapper<AnotherPayMembercardRecord>().eq(AnotherPayMembercardRecord::getOrderId,orderId));
    }
    
    /**
     * 修改企业用户启用套餐以后对应的套餐生效时间
     * @param uid
     * @return
     */
    @Override
    public void enableMemberCardHandler(Long uid) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("channel user enable member card handler!not found enterpriseChannelUser,uid={}", uid);
            return;
        }
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("channel user enable member card handler!not found userBatteryMemberCard,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            return;
        }
    
        // 判断自主续费是否打开
        if (!Objects.equals(enterpriseChannelUser.getRenewalStatus(), RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode())) {
            log.warn("channel user enable member card handler!renewal status is open,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            return;
        }
    
        // 检测骑手的代付记录
        List<AnotherPayMembercardRecord> anotherPayMembercardRecords = selectByUid(uid);
        if (ObjectUtils.isEmpty(anotherPayMembercardRecords)) {
            log.warn("channel user enable member card handler!pay record is empty,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            return;
        }
    
        long currentTimeMillis = System.currentTimeMillis();
        long realDisableTime = System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime();
    
        // 判断骑手当前的套餐是否为企业套餐
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.equals(electricityMemberCardOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
            // 检测当前套餐对应的id是否存在
            Optional<AnotherPayMembercardRecord> payMembercardRecordOptional = anotherPayMembercardRecords.stream()
                    .filter(item -> Objects.equals(item.getOrderId(), userBatteryMemberCard.getOrderId())).findFirst();
            if (!payMembercardRecordOptional.isPresent()) {
                log.error("channel user enable member card handler!not found pay record,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            }
        
            AnotherPayMembercardRecord anotherPayMembercardRecord = payMembercardRecordOptional.get();
            // 计算套餐的冻结的真实时间
            AnotherPayMembercardRecord updateRecord = AnotherPayMembercardRecord.builder().id(anotherPayMembercardRecord.getId())
                    .endTime(anotherPayMembercardRecord.getEndTime() + realDisableTime).updateTime(currentTimeMillis).build();
            // 修改当前生效套餐对应的截至日期
            update(updateRecord);
        
            // 将支付记录按照购买的顺序升序排列
            List<AnotherPayMembercardRecord> notUseRecordList = anotherPayMembercardRecords.stream().filter(item -> item.getId() > anotherPayMembercardRecord.getId())
                    .collect(Collectors.toList());
            if (ObjectUtils.isEmpty(notUseRecordList)) {
                log.warn("channel user enable member card handler!not use pay record is empty,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
                return;
            }
        
            List<Long> idList = notUseRecordList.stream().map(AnotherPayMembercardRecord::getId).collect(Collectors.toList());
            this.anotherPayMembercardRecordMapper.batchUpdateBeginAndEndTimeByIds(idList, realDisableTime, currentTimeMillis);
        } else {
            List<Long> idList = anotherPayMembercardRecords.stream().map(AnotherPayMembercardRecord::getId).collect(Collectors.toList());
            this.anotherPayMembercardRecordMapper.batchUpdateBeginAndEndTimeByIds(idList, realDisableTime, currentTimeMillis);
        }
    }
    
    /**
     * 骑手套餐生效，修改企业代付记录的开始和结束时间
     * @param userBatteryMemberCardUpdate
     * @param uid
     */
    @Override
    public void handlerOrderEffect(UserBatteryMemberCard userBatteryMemberCardUpdate, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("handler order effect!not found userInfo,uid={}", uid);
            return;
        }
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("handler order effect!not found enterpriseChannelUser,uid={}", uid);
            return;
        }
        
        // 判断自主续费是否打开
        if (!Objects.equals(enterpriseChannelUser.getRenewalStatus(), RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode())) {
            log.warn("handler order effect!renewal status is open,uid={}", uid);
            return;
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCardUpdate.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("handler order effect!member card order not find,uid={}, orderId={}", uid, userBatteryMemberCardUpdate.getOrderId());
            return;
        }
    
        // 判断当前生效的套餐是否为企业套餐
        if (!Objects.equals(electricityMemberCardOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
            log.warn("handler order effect!member card order type is not enterprise,uid={}, orderId={}", uid, userBatteryMemberCardUpdate.getOrderId());
            return;
        }
        
        AnotherPayMembercardRecord anotherPayMembercardRecord = selectByOrderId(userBatteryMemberCardUpdate.getOrderId());
        if (Objects.isNull(anotherPayMembercardRecord)) {
            log.warn("handler order effect! pay record not find,uid={}, orderId={}", uid, userBatteryMemberCardUpdate.getOrderId());
            return;
        }
        
        AnotherPayMembercardRecord payMembercardRecordUpdate = AnotherPayMembercardRecord.builder().id(anotherPayMembercardRecord.getId())
                .beginTime(userBatteryMemberCardUpdate.getOrderEffectiveTime()).endTime(userBatteryMemberCardUpdate.getOrderExpireTime()).updateTime(System.currentTimeMillis())
                .build();
        // 修改套餐的开始，结束时间
        update(payMembercardRecordUpdate);
    }
    
    @Override
    public void systemEnableMemberCardHandler(Long uid, EleDisableMemberCardRecord eleDisableMemberCardRecord) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("channel user enable member card handler!not found enterpriseChannelUser,uid={}", uid);
            return;
        }
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("channel user enable member card handler!not found userBatteryMemberCard,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            return;
        }
    
        // 判断自主续费是否打开
        if (!Objects.equals(enterpriseChannelUser.getRenewalStatus(), RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode())) {
            log.warn("channel user enable member card handler!renewal status is open,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            return;
        }
    
        // 检测骑手的代付记录
        List<AnotherPayMembercardRecord> anotherPayMembercardRecords = selectByUid(uid);
        if (ObjectUtils.isEmpty(anotherPayMembercardRecords)) {
            log.warn("channel user enable member card handler!pay record is empty,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            return;
        }
    
        long currentTimeMillis = System.currentTimeMillis();
        long realDisableTime = eleDisableMemberCardRecord.getChooseDays() * TimeConstant.DAY_MILLISECOND;
    
        // 判断骑手当前的套餐是否为企业套餐
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.equals(electricityMemberCardOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
            // 检测当前套餐对应的id是否存在
            Optional<AnotherPayMembercardRecord> payMembercardRecordOptional = anotherPayMembercardRecords.stream()
                    .filter(item -> Objects.equals(item.getOrderId(), userBatteryMemberCard.getOrderId())).findFirst();
            if (!payMembercardRecordOptional.isPresent()) {
                log.error("channel user enable member card handler!not found pay record,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
            }
        
            AnotherPayMembercardRecord anotherPayMembercardRecord = payMembercardRecordOptional.get();
            // 计算套餐的冻结的真实时间
            AnotherPayMembercardRecord updateRecord = AnotherPayMembercardRecord.builder().id(anotherPayMembercardRecord.getId())
                    .endTime(anotherPayMembercardRecord.getEndTime() + realDisableTime).updateTime(currentTimeMillis).build();
            // 修改当前生效套餐对应的截至日期
            update(updateRecord);
        
            // 将支付记录按照购买的顺序升序排列
            List<AnotherPayMembercardRecord> notUseRecordList = anotherPayMembercardRecords.stream().filter(item -> item.getId() > anotherPayMembercardRecord.getId())
                    .collect(Collectors.toList());
            if (ObjectUtils.isEmpty(notUseRecordList)) {
                log.warn("channel user enable member card handler!not use pay record is empty,uid={}, orderId={}", uid, userBatteryMemberCard.getOrderId());
                return;
            }
        
            List<Long> idList = notUseRecordList.stream().map(AnotherPayMembercardRecord::getId).collect(Collectors.toList());
            this.anotherPayMembercardRecordMapper.batchUpdateBeginAndEndTimeByIds(idList, realDisableTime, currentTimeMillis);
        } else {
            List<Long> idList = anotherPayMembercardRecords.stream().map(AnotherPayMembercardRecord::getId).collect(Collectors.toList());
            this.anotherPayMembercardRecordMapper.batchUpdateBeginAndEndTimeByIds(idList, realDisableTime, currentTimeMillis);
        }
    }
    
    @Override
    @Slave
    public boolean existPayRecordByUid(Long uid) {
        Integer count = anotherPayMembercardRecordMapper.existPayRecordByUid(uid);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
}
