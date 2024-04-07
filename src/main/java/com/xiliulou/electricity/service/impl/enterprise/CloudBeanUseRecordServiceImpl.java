package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.mapper.enterprise.CloudBeanUseRecordMapper;
import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.CloudBeanOrderExcelVO;
import com.xiliulou.electricity.vo.enterprise.CloudBeanSumVO;
import com.xiliulou.electricity.vo.enterprise.CloudBeanUseRecordVO;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import com.xiliulou.storage.service.impl.AliyunOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 云豆使用记录表(CloudBeanUseRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-18 10:35:13
 */
@Service("cloudBeanUseRecordService")
@Slf4j
public class CloudBeanUseRecordServiceImpl implements CloudBeanUseRecordService {
    
    private static final String CLOUD_BEAN_BILL_PATH = "saas/";
    
    @Autowired
    StorageConfig storageConfig;
    
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;
    
    @Autowired
    AliyunOssService aliyunOssService;
    
    @Resource
    private CloudBeanUseRecordMapper cloudBeanUseRecordMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    private EnterpriseInfoService enterpriseInfoService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    private EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Autowired
    private EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;
    
    @Autowired
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    private InsuranceOrderService insuranceOrderService;
    
    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    private EnterpriseRentRecordService enterpriseRentRecordService;
    
    @Override
    public CloudBeanUseRecord queryByIdFromDB(Long id) {
        return this.cloudBeanUseRecordMapper.queryById(id);
    }
    
    @Override
    public Integer insert(CloudBeanUseRecord cloudBeanUseRecord) {
        return this.cloudBeanUseRecordMapper.insert(cloudBeanUseRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CloudBeanUseRecord cloudBeanUseRecord) {
        return this.cloudBeanUseRecordMapper.update(cloudBeanUseRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.cloudBeanUseRecordMapper.deleteById(id) > 0;
    }
    
    @Override
    public Double selectCloudBeanByEnterpriseIdAndType(Long uid, Integer type) {
        return this.cloudBeanUseRecordMapper.selectCloudBeanByEnterpriseIdAndType(uid, type);
    }
    
    @Slave
    @Override
    public List<CloudBeanUseRecord> selectByEnterpriseIdAndType(Long enterpriseId, Integer type) {
        return this.cloudBeanUseRecordMapper.selectByEnterpriseIdAndType(enterpriseId, type);
    }
    
    @Slave
    @Override
    public List<CloudBeanUseRecord> selectCanRecycleRecord(Long enterpriseId, long currentTimeMillis) {
        return this.cloudBeanUseRecordMapper.selectCanRecycleRecord(enterpriseId, currentTimeMillis);
    }
    
    @Slave
    @Override
    public BigDecimal acquireUserRecycledCloudBean(Long uid) {
        List<CloudBeanUseRecord> cloudBeanUseRecords = this.cloudBeanUseRecordMapper
                .selectList(new LambdaQueryWrapper<CloudBeanUseRecord>().eq(CloudBeanUseRecord::getUid, uid).eq(CloudBeanUseRecord::getType, CloudBeanUseRecord.TYPE_RECYCLE));
        if (CollectionUtils.isEmpty(cloudBeanUseRecords)) {
            return BigDecimal.ZERO;
        }
        
        return cloudBeanUseRecords.stream().map(CloudBeanUseRecord::getBeanAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal acquireUserCanRecycleCloudBean(Long uid) {
        BigDecimal result = BigDecimal.ZERO;
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ACQUIRE CAN RECYCLE WARN!not found userInfo,uid={}", uid);
            return result;
        }
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("ACQUIRE CAN RECYCLE WARN! user illegal,uid={}", uid);
            return result;
        }
        
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(enterpriseChannelUser.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.warn("ACQUIRE CAN RECYCLE WARN! not found enterpriseInfo,enterpriseId={},uid={}", enterpriseChannelUser.getEnterpriseId(), uid);
            return result;
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("ACQUIRE CAN RECYCLE WARN!not found userBatteryDeposit,uid={}", uid);
            return result;
        }
    
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if(Objects.isNull(eleDepositOrder)){
            log.warn("ACQUIRE CAN RECYCLE WARN!not found eleDepositOrder,uid={}", uid);
            return result;
        }
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if(Objects.isNull(userBatteryMemberCard)){
            log.warn("ACQUIRE CAN RECYCLE WARN!not found userBatteryMemberCard,uid={}", uid);
            return result;
        }
    
        //押金云豆数: 免押用户不需要计算
        if (Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT) && Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
            result = result.add(eleDepositOrder.getPayAmount());
        }
    
        //代付套餐记录
        List<AnotherPayMembercardRecord> anotherPayMembercardRecords = anotherPayMembercardRecordService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(anotherPayMembercardRecords)) {
            return result;
        }
    
        Map<String, AnotherPayMembercardRecord> payMembercardRecordMap = anotherPayMembercardRecords.stream().collect(Collectors.toMap(AnotherPayMembercardRecord::getOrderId, entity -> entity));
    
        //套餐总的云豆数
        BigDecimal totalCloudBean = BigDecimal.ZERO;
        for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(anotherPayMembercardRecord.getOrderId());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                continue;
            }
            totalCloudBean = totalCloudBean.add(electricityMemberCardOrder.getPayAmount());
        }
        log.info("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD INFO!totalCloudBean={},uid={}", totalCloudBean.doubleValue(), userInfo.getUid());
    
        //租退电记录
        List<EnterpriseRentRecord> enterpriseRentRecords = enterpriseRentRecordService.selectByUid(userInfo.getUid());
    
        //若未租退电
        if (CollectionUtils.isEmpty(enterpriseRentRecords)) {
            return result.add(totalCloudBean);
        }
    
        //租退电消耗的云豆数
        BigDecimal totalUsedCloudBean = BigDecimal.ZERO;
        for (EnterpriseRentRecord enterpriseRentRecord : enterpriseRentRecords) {
            if (Objects.nonNull(enterpriseRentRecord.getRentTime()) && Objects.nonNull(enterpriseRentRecord.getReturnTime())) {
    
                //租退电是否在同一个套餐内
                if(ObjectUtil.equal( enterpriseRentRecord.getRentMembercardOrderId(), enterpriseRentRecord.getReturnMembercardOrderId())){
                    ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getRentMembercardOrderId());
                    if (Objects.nonNull(electricityMemberCardOrder)) {
                        AnotherPayMembercardRecord returnAnotherPayMembercardRecord = payMembercardRecordMap.getOrDefault(enterpriseRentRecord.getRentMembercardOrderId(), null);
    
                        Long beginTime = enterpriseRentRecord.getRentTime();
    
                        Long endTime = Objects.nonNull(returnAnotherPayMembercardRecord) && Objects.nonNull(returnAnotherPayMembercardRecord.getEndTime())&& Objects.nonNull(enterpriseRentRecord.getReturnTime()) && enterpriseRentRecord.getReturnTime() > returnAnotherPayMembercardRecord.getEndTime() ? returnAnotherPayMembercardRecord.getEndTime() : enterpriseRentRecord.getReturnTime();
    
                        long useDays = DateUtils.diffDayV2(beginTime , endTime);
            
                        //退电套餐单价
                        BigDecimal price = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
            
                        totalUsedCloudBean = totalUsedCloudBean.add(price.multiply(BigDecimal.valueOf(useDays)));
                        log.info("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD INFO!one membercard used cloud bean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                    }
                } else {
                    //租电套餐消耗的云豆
                    AnotherPayMembercardRecord rentAnotherPayMembercardRecord = payMembercardRecordMap.getOrDefault(enterpriseRentRecord.getRentMembercardOrderId(),null);
                    ElectricityMemberCardOrder rentElectricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getRentMembercardOrderId());
                    if (Objects.nonNull(rentAnotherPayMembercardRecord) && Objects.nonNull(rentElectricityMemberCardOrder)) {
                        //单价
                        BigDecimal price = rentElectricityMemberCardOrder.getPayAmount()
                                .divide(BigDecimal.valueOf(rentElectricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
                        //使用天数
                        long useDays = DateUtils.diffDayV2(enterpriseRentRecord.getRentTime(), rentAnotherPayMembercardRecord.getEndTime());
        
                        totalUsedCloudBean = totalUsedCloudBean.add(price.multiply(BigDecimal.valueOf(useDays)));
                        log.info("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD INFO!rentUsedCloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                    }
    
                    List<String> membercardList = anotherPayMembercardRecords.stream().map(AnotherPayMembercardRecord::getOrderId).collect(Collectors.toList());
                    //租退电包含的套餐订单
                    if (CollectionUtils.isNotEmpty(membercardList) && membercardList.size() > 1) {
                        List<String> containMembercardList = membercardList.subList(membercardList.indexOf(enterpriseRentRecord.getRentMembercardOrderId()) + 1,
                                membercardList.indexOf(enterpriseRentRecord.getReturnMembercardOrderId()));
                        if (CollectionUtils.isNotEmpty(containMembercardList)) {
                            for (String orderId : containMembercardList) {
                                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
                                if (Objects.nonNull(electricityMemberCardOrder)) {
                                    totalUsedCloudBean = totalUsedCloudBean.add(electricityMemberCardOrder.getPayAmount());
                                    log.info("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD INFO!containUsedCloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                                }
                            }
                        }
                    }
    
                    //退电消耗的云豆
                    AnotherPayMembercardRecord returnAnotherPayMembercardRecord = payMembercardRecordMap.getOrDefault(enterpriseRentRecord.getReturnMembercardOrderId(), null);
                    ElectricityMemberCardOrder returnElectricityMemberCardOrder = electricityMemberCardOrderService
                            .selectByOrderNo(enterpriseRentRecord.getReturnMembercardOrderId());
                    if (Objects.nonNull(returnAnotherPayMembercardRecord) && Objects.nonNull(returnElectricityMemberCardOrder)) {
                        //单价
                        BigDecimal price = returnElectricityMemberCardOrder.getPayAmount()
                                .divide(BigDecimal.valueOf(returnElectricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
                        
                        Long beginTime = returnAnotherPayMembercardRecord.getBeginTime();
    
                        Long endTime = Objects.nonNull(enterpriseRentRecord.getReturnTime()) && enterpriseRentRecord.getReturnTime() > returnAnotherPayMembercardRecord.getEndTime() ? returnAnotherPayMembercardRecord.getEndTime() : enterpriseRentRecord.getReturnTime();
    
                        //使用天数
                        long useDays = DateUtils.diffDayV2( beginTime, endTime);
        
                        totalUsedCloudBean = totalUsedCloudBean.add(price.multiply(BigDecimal.valueOf(useDays)));
                        log.info("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD INFO!returnUsedCloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                    }
                }
            }
        }
        
        //待回收云豆=总的支付的云豆-总消耗的云豆
        return result.add(totalCloudBean.subtract(totalUsedCloudBean));
    }
    
    @Override
    public BigDecimal getReturnBatteryMembercardUsedCloudBean(UserInfo userInfo, EnterpriseInfo enterpriseInfo, EnterpriseRentRecord enterpriseRentRecord,
            List<AnotherPayMembercardRecord> anotherPayMembercardRecords) {
        BigDecimal result = BigDecimal.ZERO;
        
        AnotherPayMembercardRecord anotherPayMembercardRecord = anotherPayMembercardRecords.stream()
                .filter(item -> Objects.equals(enterpriseRentRecord.getReturnMembercardOrderId(), item.getOrderId())).findFirst().orElse(null);
        if (Objects.isNull(anotherPayMembercardRecord)) {
            log.warn("GET RETURN BATTERY MEMBERCARD USED CLOUD BEAN WARN! anotherPayMembercardRecord is null,uid={},orderId={}", enterpriseRentRecord.getUid(),
                    enterpriseRentRecord.getReturnMembercardOrderId());
            return result;
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getReturnMembercardOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("GET RETURN BATTERY MEMBERCARD USED CLOUD BEAN WARN! electricityMemberCardOrder is null,uid={},orderId={}", enterpriseRentRecord.getUid(),
                    anotherPayMembercardRecord.getOrderId());
            return result;
        }
        
        //退电套餐消耗天数
        long useDays = dateDifferent(enterpriseRentRecord.getReturnTime(), anotherPayMembercardRecord.getBeginTime());
        //退电套餐单价
        BigDecimal membercardPrice = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
    
        //消耗云豆数
        BigDecimal useCloudBean = membercardPrice.multiply(BigDecimal.valueOf(useDays));
        //回收云豆数
        BigDecimal remainingCloudBean = (electricityMemberCardOrder.getPayAmount().subtract(useCloudBean)).compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO
                : electricityMemberCardOrder.getPayAmount().subtract(useCloudBean);
        
        //保存回收记录
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(userInfo.getUid());
        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
        cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
        cloudBeanUseRecord.setBeanAmount(electricityMemberCardOrder.getPayAmount());
        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount().add(remainingCloudBean));
        cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        this.insert(cloudBeanUseRecord);
    
        enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(remainingCloudBean));
        
        return useCloudBean;
    }
    
    private BigDecimal getReturnBatteryMembercardUsedCloudBeanV2(EnterpriseRentRecord enterpriseRentRecord, Long currentTime) {
        
        BigDecimal result = BigDecimal.ZERO;
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(enterpriseRentRecord.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("GET RETURN BATTERY MEMBERCARD USED CLOUD BEAN WARN! userBatteryMemberCard is null,uid={}", enterpriseRentRecord.getUid());
            return result;
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("GET RETURN BATTERY MEMBERCARD USED CLOUD BEAN WARN! electricityMemberCardOrder is null,uid={},orderId={}", enterpriseRentRecord.getUid(),
                    userBatteryMemberCard.getOrderId());
            return result;
        }
        
        //退电套餐消耗天数
        long useDays = dateDifferent(currentTime, userBatteryMemberCard.getOrderEffectiveTime());
        
        //退电套餐单价
        BigDecimal membercardPrice = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
        
        return membercardPrice.multiply(BigDecimal.valueOf(useDays));
    }
    
    @Override
    public BigDecimal getRentBatteryMembercardUsedCloudBean(UserInfo userInfo, EnterpriseInfo enterpriseInfo, EnterpriseRentRecord enterpriseRentRecord,
            List<AnotherPayMembercardRecord> anotherPayMembercardRecords) {
        BigDecimal result = BigDecimal.ZERO;
        
        AnotherPayMembercardRecord anotherPayMembercardRecord = anotherPayMembercardRecords.stream()
                .filter(item -> Objects.equals(enterpriseRentRecord.getRentMembercardOrderId(), item.getOrderId())).findFirst().orElse(null);
        if (Objects.isNull(anotherPayMembercardRecord)) {
            log.warn("GET RENT BATTERY MEMBERCARD USED CLOUD BEAN WARN! anotherPayMembercardRecord is null,uid={},orderId={}", enterpriseRentRecord.getUid(),
                    enterpriseRentRecord.getRentMembercardOrderId());
            return result;
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getRentMembercardOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("GET RENT BATTERY MEMBERCARD USED CLOUD BEAN WARN! electricityMemberCardOrder is null,uid={},orderId={}", enterpriseRentRecord.getUid(),
                    anotherPayMembercardRecord.getOrderId());
            return result;
        }
        
        //租电套餐消耗天数
        long useDays = dateDifferent(enterpriseRentRecord.getRentTime(), anotherPayMembercardRecord.getEndTime());
        //租电套餐单价
        BigDecimal membercardPrice = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
        //消耗云豆数
        BigDecimal useCloudBean = membercardPrice.multiply(BigDecimal.valueOf(useDays));
        //回收云豆数
        BigDecimal remainingCloudBean = (electricityMemberCardOrder.getPayAmount().subtract(useCloudBean)).compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO
                : electricityMemberCardOrder.getPayAmount().subtract(useCloudBean);
        
        //保存回收记录
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(userInfo.getUid());
        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
        cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
        cloudBeanUseRecord.setBeanAmount(remainingCloudBean);
        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount().add(remainingCloudBean));
        cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        this.insert(cloudBeanUseRecord);
    
        enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(remainingCloudBean));
        
        return useCloudBean;
    }
    
    @Override
    public BigDecimal getBatteryMembercardUsedCloudBean(UserInfo userInfo, EnterpriseInfo enterpriseInfo, EnterpriseRentRecord enterpriseRentRecord,
            List<AnotherPayMembercardRecord> anotherPayMembercardRecords) {
        BigDecimal result = BigDecimal.ZERO;
        
        if(StringUtils.isBlank(enterpriseRentRecord.getReturnMembercardOrderId())){
            log.warn("GET MEMBERCARD USED CLOUD BEAN WARN! ReturnMembercardOrderId is null,uid={}", enterpriseRentRecord.getUid());
            return result;
        }
    
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getReturnMembercardOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("GET MEMBERCARD USED CLOUD BEAN WARN! electricityMemberCardOrder is null,uid={},orderId={}", enterpriseRentRecord.getUid(), enterpriseRentRecord.getReturnMembercardOrderId());
            return result;
        }
    
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if(Objects.isNull(batteryMemberCard)){
            log.warn("GET MEMBERCARD USED CLOUD BEAN WARN! batteryMemberCard is null,uid={},mid={}", enterpriseRentRecord.getUid(), electricityMemberCardOrder.getMemberCardId());
            return result;
        }
        
        //套餐单价
        BigDecimal rentPrice=batteryMemberCard.getRentPrice().divide(BigDecimal.valueOf(batteryMemberCard.getValidDays()),2, RoundingMode.HALF_UP);
        
        //租退电天数
        long useDays = DateUtils.diffDayV2(enterpriseRentRecord.getRentTime(), enterpriseRentRecord.getReturnTime());
        
        //总共消耗的云豆
        return BigDecimal.valueOf(useDays).multiply(rentPrice);
    }
    
    @Slave
    @Override
    public void checkCloudBeanTask() {
        List<Long> enterpriseIdList = cloudBeanUseRecordMapper.selectListEnterpriseId();
        if (ObjectUtils.isEmpty(enterpriseIdList)) {
            return;
        }
    
        enterpriseIdList.stream().forEach(enterpriseId -> {
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseId);
            if (Objects.isNull(enterpriseInfo)) {
                log.info("check cloud bean task enterprise not find enterpriseId={}", enterpriseId);
                return;
            }
            
            List<CloudBeanSumVO> cloudBeanSumVOList = cloudBeanUseRecordMapper.selectBeanAmountByEnterpriseId(enterpriseId);
            if (ObjectUtils.isEmpty(cloudBeanSumVOList)) {
                return;
            }
            
            // 计算每个类型云豆数量的总和
            Map<Integer, BigDecimal> beanAmountMap = cloudBeanSumVOList.stream().filter(cloudBeanSumVO -> Objects.nonNull(cloudBeanSumVO.getBeanAmount()))
                    .collect(Collectors.groupingBy(CloudBeanSumVO::getType, Collectors.reducing(BigDecimal.ZERO, CloudBeanSumVO::getBeanAmount, BigDecimal::add)));
            
            // 企业总充值的：t_cloud_bean_use_record :type 2，3，4  - 5
            BigDecimal rechargeSum = getRechargeSum(beanAmountMap);
            
            // 计算真实消费的云豆数量：总代扣的减去-总回收的
            BigDecimal realConsumeSum = getRealConsumeSum(beanAmountMap);
    
            BigDecimal totalBeanAmount = realConsumeSum.add(enterpriseInfo.getTotalBeanAmount());
            if (!Objects.equals(totalBeanAmount.compareTo(rechargeSum), NumberConstant.ZERO)) {
                log.error("check cloud bean task calculate error enterpriseId={}, rechargeSum={}, totalBeanAmount={}", enterpriseId, totalBeanAmount);
            }
        });
    }
    
    private BigDecimal getRealConsumeSum(Map<Integer, BigDecimal> beanAmountMap) {
        BigDecimal sum = BigDecimal.ZERO;
        // 代扣总和
        if (ObjectUtils.isNotEmpty(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_PAY_MEMBERCARD))) {
            sum = sum.add(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_PAY_MEMBERCARD));
        }
        // 回收总和
        if (ObjectUtils.isNotEmpty(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_RECYCLE))) {
            sum = sum.subtract(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_RECYCLE));
        }
        
        return sum;
    }
    
    private BigDecimal getRechargeSum(Map<Integer, BigDecimal> beanAmountMap) {
        BigDecimal sum = BigDecimal.ZERO;
        
        if (ObjectUtils.isNotEmpty(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_ADMIN_RECHARGE))) {
            sum = sum.add(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_ADMIN_RECHARGE));
        }
    
        if (ObjectUtils.isNotEmpty(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_PRESENT))) {
            sum = sum.add(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_PRESENT));
        }
        
        if (ObjectUtils.isNotEmpty(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_USER_RECHARGE))) {
            sum = sum.add(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_USER_RECHARGE));
        }
    
        if (ObjectUtils.isNotEmpty(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_ADMIN_DEDUCT))) {
            sum = sum.subtract(beanAmountMap.get(EnterpriseCloudBeanOrder.TYPE_ADMIN_DEDUCT));
        }
        
        return sum;
    }
    
    private BigDecimal getContainMembercardUsedCloudBeanV2(EnterpriseRentRecord enterpriseRentRecord, List<AnotherPayMembercardRecord> anotherPayMembercardRecords,
            Long currentTime) {
        
        BigDecimal result = BigDecimal.ZERO;
        
        for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
            //套餐是否在租退电时间内
            if (((enterpriseRentRecord.getRentTime() - anotherPayMembercardRecord.getBeginTime()) <= 0) && ((anotherPayMembercardRecord.getEndTime() - currentTime) <= 0)
                    || ((currentTime - anotherPayMembercardRecord.getBeginTime()) >= 0) && ((enterpriseRentRecord.getRentTime() - anotherPayMembercardRecord.getEndTime()) <= 0)) {
                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getRentMembercardOrderId());
                if (Objects.isNull(electricityMemberCardOrder)) {
                    log.warn("GET CONTAIN MEMBERCARD USED CLOUD BEAN WARN! electricityMemberCardOrder is null,uid={},orderId={}", enterpriseRentRecord.getUid(),
                            anotherPayMembercardRecord.getOrderId());
                    continue;
                }
                
                result = result.add(electricityMemberCardOrder.getPayAmount());
            }
        }
        
        return result;
    }
    
    @Override
    public Triple<Boolean, String, Object> cloudBeanOrderDownload(Long beginTime, Long endTime) {
        if (endTime < beginTime || endTime - beginTime > 366 * 24 * 60 * 60 * 1000L) {
            return Triple.of(false, "100314", "时间参数不合法");
        }
        
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(SecurityUtils.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("CLOUD BEAN ORDER DOWNLOAD ERROR ! not found enterpriseInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100315", "企业配置不存在!");
        }
        
        List<CloudBeanUseRecord> list = cloudBeanUseRecordMapper.selectByTime(beginTime, endTime, enterpriseInfo.getId());
        if (CollectionUtils.isEmpty(list)) {
            log.error("CLOUD BEAN ORDER DOWNLOAD ERROR ! list is empty,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100316", "所选时间段内无可用账单数据，无法下载");
        }
        
        List<CloudBeanOrderExcelVO> cloudBeanOrderExcelVOList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        int index = 0;
        for (CloudBeanUseRecord cloudBeanUseRecord : list) {
            index++;
            UserInfo userInfo = userInfoService.queryByUidFromCache(cloudBeanUseRecord.getUid());
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(cloudBeanUseRecord.getPackageId());
            
            CloudBeanOrderExcelVO cloudBeanOrderExcelVO = new CloudBeanOrderExcelVO();
            cloudBeanOrderExcelVO.setId(index);
            cloudBeanOrderExcelVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
            cloudBeanOrderExcelVO.setPhone(Objects.isNull(userInfo) ? "" : userInfo.getPhone());
            cloudBeanOrderExcelVO.setType(acquireOrderType(cloudBeanUseRecord.getType()));
            cloudBeanOrderExcelVO.setBeanAmount(cloudBeanUseRecord.getBeanAmount().toPlainString());
            cloudBeanOrderExcelVO.setRemainingBeanAmount(cloudBeanUseRecord.getRemainingBeanAmount().toPlainString());
            cloudBeanOrderExcelVO.setPackageName(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
            cloudBeanOrderExcelVO.setCreateTime(simpleDateFormat.format(new Date(cloudBeanUseRecord.getCreateTime())));
            if (Objects.equals(cloudBeanUseRecord.getType(), CloudBeanUseRecord.TYPE_ADMIN_DEDUCT) || Objects
                    .equals(cloudBeanUseRecord.getType(), CloudBeanUseRecord.TYPE_ADMIN_RECHARGE)) {
                EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = enterpriseCloudBeanOrderService.selectByOrderId(cloudBeanUseRecord.getRef());
                if (Objects.nonNull(enterpriseCloudBeanOrder)) {
                    User user = userService.queryByUidFromCache(enterpriseCloudBeanOrder.getOperateUid());
                    cloudBeanOrderExcelVO.setOperateName(Objects.isNull(user) ? "" : user.getName());
                }
            }
            
            cloudBeanOrderExcelVOList.add(cloudBeanOrderExcelVO);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            EasyExcel.write(out, CloudBeanOrderExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(cloudBeanOrderExcelVOList);
            
            String excelPath = CLOUD_BEAN_BILL_PATH + IdUtil.simpleUUID() + ".xlsx";
            
            aliyunOssService.uploadFile(storageConfig.getBucketName(), excelPath, new ByteArrayInputStream(out.toByteArray()));
            
            return Triple.of(true, null, StorageConfig.HTTPS + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" +excelPath);
        } catch (Exception e) {
            log.error("导出云豆账单失败！", e);
        }
        
        return Triple.of(false, null, "导出云豆账单失败");
    }
    
    @Slave
    @Override
    public List<CloudBeanUseRecordVO> selectByUserPage(CloudBeanUseRecordQuery query) {
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(SecurityUtils.getUid());
        if(Objects.isNull(enterpriseInfo)){
            return Collections.emptyList();
        }else {
            query.setEnterpriseId(enterpriseInfo.getId());
        }
    
        List<CloudBeanUseRecord> list = this.cloudBeanUseRecordMapper.selectByUserPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.stream().map(item -> {
            CloudBeanUseRecordVO cloudBeanUseRecordVO = new CloudBeanUseRecordVO();
            BeanUtils.copyProperties(item, cloudBeanUseRecordVO);
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            if (Objects.nonNull(userInfo)) {
                cloudBeanUseRecordVO.setUsername(userInfo.getName());
                cloudBeanUseRecordVO.setPhone(userInfo.getPhone());
            }
            
            if (!Objects.equals(item.getPackageId(), NumberConstant.ZERO_L)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getPackageId());
                cloudBeanUseRecordVO.setBatteryMemberCard(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
            }
            
            return cloudBeanUseRecordVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public CloudBeanUseRecordVO cloudBeanUseStatisticsByUid(CloudBeanUseRecordQuery query) {
        
        CloudBeanUseRecordVO cloudBeanUseRecordVO = new CloudBeanUseRecordVO();
        cloudBeanUseRecordVO.setIncome(BigDecimal.ZERO);
        cloudBeanUseRecordVO.setExpend(BigDecimal.ZERO);
    
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(SecurityUtils.getUid());
        if(Objects.isNull(enterpriseInfo)){
            return cloudBeanUseRecordVO;
        }else {
            query.setEnterpriseId(enterpriseInfo.getId());
        }
        
        query.setSize(Long.MAX_VALUE);
        query.setOffset(NumberConstant.ZERO_L);
        
        List<CloudBeanUseRecord> list = this.cloudBeanUseRecordMapper.selectByUserPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return cloudBeanUseRecordVO;
        }
        
        //支出
        BigDecimal expend = list.stream()
                .filter(item -> Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_ADMIN_DEDUCT))
                .map(CloudBeanUseRecord::getBeanAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //收入
        BigDecimal income = list.stream()
                .filter(item -> Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_ADMIN_RECHARGE) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_PRESENT) || Objects
                        .equals(item.getType(), CloudBeanUseRecord.TYPE_USER_RECHARGE) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_RECYCLE))
                .map(CloudBeanUseRecord::getBeanAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        cloudBeanUseRecordVO.setIncome(Objects.isNull(income) ? BigDecimal.ZERO : income);
        cloudBeanUseRecordVO.setExpend(Objects.isNull(expend) ? BigDecimal.ZERO : expend);
        
        return cloudBeanUseRecordVO;
    }
    
    @Override
    public void recycleCloudBeanTask() {
        int offset = 0;
        int size = 200;
        
        long startTime = System.currentTimeMillis();
        
        while (true) {
            List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.selectExpireList(offset, size, startTime);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                return;
            }
    
            userBatteryMemberCardList.forEach(item -> {
                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                if (Objects.isNull(userInfo)) {
                    return;
                }
        
                if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                    return;
                }
        
                if (Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                    log.warn("RECYCLE TASK WARN! user's member card is stop,uid={}", item.getUid());
                    return;
                }
        
                if (Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                    log.warn("RECYCLE TASK WARN! user stop member card review,uid={}", item.getUid());
                    return;
                }
        
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("RECYCLE TASK WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), item.getMemberCardId());
                    return;
                }
        
                Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                        .acquireUserBatteryServiceFee(userInfo, item, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                    log.warn("RECYCLE TASK WARN! user exist battery service fee,uid={}", userInfo.getUid());
                    return;
                }
        
                EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(userInfo.getUid());
                if (Objects.isNull(enterpriseChannelUser)) {
                    return;
                }
        
                EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseChannelUser.getEnterpriseId());
                if (Objects.isNull(enterpriseInfo)) {
                    log.error("RECYCLE CLOUD BEAN TASK ERROR! not found enterpriseInfo,enterpriseId={}", enterpriseChannelUser.getEnterpriseId());
                    return;
                }
    
                //回收押金
                Triple<Boolean, String, Object> batteryDepositTriple = enterpriseInfoService.recycleBatteryDeposit(userInfo, enterpriseInfo);
                if (Boolean.FALSE.equals(batteryDepositTriple.getLeft())) {
                    return;
                }
        
                //回收套餐
                Triple<Boolean, String, Object> recycleBatteryMembercard = enterpriseInfoService.recycleBatteryMembercard(userInfo, enterpriseInfo, item);
                if (Boolean.FALSE.equals(recycleBatteryMembercard.getLeft())) {
                    return;
                }
        
                //解绑用户数据
                enterpriseInfoService.unbindUserData(userInfo, enterpriseChannelUser);
    
                BigDecimal membercardTotalCloudBean = (BigDecimal) recycleBatteryMembercard.getRight();
                BigDecimal batteryDepositTotalCloudBean = (BigDecimal) batteryDepositTriple.getRight();
    
                enterpriseInfoService.addCloudBean(enterpriseInfo.getId(), membercardTotalCloudBean.add(batteryDepositTotalCloudBean));
            });
            
            offset += size;
        }
    }
    
    public String acquireOrderType(int type) {
        String orderType = null;
        switch (type) {
            case 0:
                orderType = "套餐代付";
                break;
            case 1:
                orderType = "套餐回收";
                break;
            case 2:
                orderType = "云豆充值";
                break;
            case 3:
                orderType = "赠送";
                break;
            case 4:
                orderType = "后台充值";
                break;
            case 5:
                orderType = "后台扣除";
                break;
            default:
                orderType = "";
        }
        return orderType;
    }
    
    /**
     * 计算间隔天数，向上取
     */
    public static long dateDifferent(long sartTime, long endTime) {
        Long result = 0L;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            Date date1 = new Date(sartTime);
            Date date2 = new Date(endTime);
            LocalDate date3 = LocalDate.parse(sdf.format(date1), fmt);
            LocalDate date4 = LocalDate.parse(sdf.format(date2), fmt);
            
            result = ChronoUnit.DAYS.between(date3, date4) + 1;
        } catch (Exception e) {
            log.error("dateDifferent error!", e);
        }
        return result;
    }
}
