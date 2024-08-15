package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.merchant.MerchantConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecordDetail;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecordDetail;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.enterprise.CloudBeanStatusEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseBatteryPackageMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseInfoMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.UserCloudBeanRechargeQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordDetailService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordDetailService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CloudBeanGeneralViewVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoPackageVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePurchasedPackageResultVO;
import com.xiliulou.electricity.vo.enterprise.UserCloudBeanDetailVO;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 企业用户信息表(EnterpriseInfo)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-14 10:15:08
 */
@Service("enterpriseInfoService")
@Slf4j
public class EnterpriseInfoServiceImpl implements EnterpriseInfoService {
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("CLOUD-BEAN-RECYCLE-POOL", 1, "unFreeDepositOrder:");
    
    @Resource
    private EnterpriseInfoMapper enterpriseInfoMapper;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private EnterprisePackageService enterprisePackageService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    private UserOauthBindService userOauthBindService;
    
    @Autowired
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Autowired
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    private InsuranceOrderService insuranceOrderService;
    
    @Autowired
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    private EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    private ElectricityMemberCardOrderService batteryMemberCardOrderService;
    
    @Resource
    EnterpriseBatteryPackageMapper enterpriseBatteryPackageMapper;
    
    @Autowired
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Autowired
    private EnterpriseRentRecordService enterpriseRentRecordService;
    
    @Autowired
    private EnterpriseRentRecordDetailService enterpriseRentRecordDetailService;
    
    @Autowired
    private EleDepositOrderService eleDepositOrderService;
    
    @Resource
    EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    @Autowired
    private FreeDepositOrderService freeDepositOrderService;
    
    @Autowired
    private PxzConfigService pxzConfigService;
    
    @Autowired
    private PxzDepositService pxzDepositService;
    
    @Resource
    private BatteryMemberCardService memberCardService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private EnterpriseRentRecordDetailService rentRecordDetailService;
    
    @Resource
    private EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Resource
    private CloudBeanUseRecordDetailService cloudBeanUseRecordDetailService;
    
    @Resource
    private MerchantConfig merchantConfig;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseInfo queryByIdFromDB(Long id) {
        return this.enterpriseInfoMapper.queryById(id);
    }
    
    
    @Override
    public EnterpriseInfo queryByIdFromCache(Long id) {
        
        EnterpriseInfo cacheEnterpriseInfo = redisService.getWithHash(CacheConstant.CACHE_ENTERPRISE_INFO + id, EnterpriseInfo.class);
        if (Objects.nonNull(cacheEnterpriseInfo)) {
            return cacheEnterpriseInfo;
        }
        
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(id);
        if (Objects.isNull(enterpriseInfo)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_ENTERPRISE_INFO + id, enterpriseInfo);
        
        return enterpriseInfo;
    }
    
    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseInfo enterpriseInfo) {
        int update = this.enterpriseInfoMapper.update(enterpriseInfo);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + enterpriseInfo.getId());
            return null;
        });
        
        return update;
    }
    
    @Override
    public int addCloudBean(Long id, BigDecimal totalCloudBean) {
        int update = this.enterpriseInfoMapper.addCloudBean(id, totalCloudBean);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + id);
            return null;
        });
        
        return update;
    }
    
    @Override
    public int subtractCloudBean(Long id, BigDecimal subtract, long updateTime) {
        int update = this.enterpriseInfoMapper.subtractCloudBean(id, subtract, updateTime);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + id);
            return null;
        });
        
        return update;
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
        return enterpriseInfoMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    @Override
    @Transactional
    public Triple<Boolean, String, Object> saveMerchantEnterprise(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfoExit = this.selectByName(enterpriseInfoQuery.getName());
        if (Objects.nonNull(enterpriseInfoExit)) {
            return Triple.of(false, "", "商户名称重复，请修改后操作");
        }
        
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfo);
        enterpriseInfo.setBusinessId(Long.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + RandomUtil.randomInt(1000, 9999)));
        enterpriseInfo.setRecoveryMode(EnterpriseInfo.RECOVERY_MODE_RETURN);
        enterpriseInfo.setTotalBeanAmount(BigDecimal.ZERO);
        enterpriseInfo.setDelFlag(EnterpriseInfo.DEL_NORMAL);
        enterpriseInfo.setTenantId(TenantContextHolder.getTenantId());
        enterpriseInfo.setCreateTime(System.currentTimeMillis());
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        this.enterpriseInfoMapper.insert(enterpriseInfo);
        
        enterpriseInfoQuery.setId(enterpriseInfo.getId());
        
        if (ObjectUtils.isNotEmpty(enterpriseInfoQuery.getPackageIds())) {
            List<EnterprisePackage> packageList = enterpriseInfoQuery.getPackageIds().stream().map(item -> {
                EnterprisePackage enterprisePackage = new EnterprisePackage();
                enterprisePackage.setEnterpriseId(enterpriseInfo.getId());
                enterprisePackage.setPackageId(item);
                enterprisePackage.setPackageType(enterpriseInfoQuery.getPackageType());
                enterprisePackage.setTenantId(enterpriseInfo.getTenantId());
                enterprisePackage.setCreateTime(System.currentTimeMillis());
                enterprisePackage.setUpdateTime(System.currentTimeMillis());
                return enterprisePackage;
            }).collect(Collectors.toList());
            
            enterprisePackageService.batchInsert(packageList);
        }
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public List<EnterpriseInfo> queryListByIdList(List<Long> enterpriseIdList) {
        return enterpriseInfoMapper.selectListByIdList(enterpriseIdList);
    }
    
    /**
     * 回收套餐新逻辑
     * @param userInfo
     * @param enterpriseInfo
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> recycleBatteryMemberCardV2(UserInfo userInfo, EnterpriseInfo enterpriseInfo, UserBatteryMemberCard userBatteryMemberCard) {
        BigDecimal result = BigDecimal.ZERO;
    
        List<AnotherPayMembercardRecord> anotherPayMembercardRecords = anotherPayMembercardRecordService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(anotherPayMembercardRecords)) {
            return Triple.of(true, null, result);
        }
    
        Map<String, AnotherPayMembercardRecord> payMembercardRecordMap = anotherPayMembercardRecords.stream()
                .collect(Collectors.toMap(AnotherPayMembercardRecord::getOrderId, entity -> entity));
        List<String> orderList = new ArrayList<>(payMembercardRecordMap.keySet());
        Map<String, ElectricityMemberCardOrder> orderMap = new HashMap<>();
    
        List<ElectricityMemberCardOrder> electricityMemberCardOrderList = electricityMemberCardOrderService.queryListByOrderIds(orderList);
        if (ObjectUtils.isNotEmpty(electricityMemberCardOrderList)) {
            orderMap = electricityMemberCardOrderList.stream().collect(Collectors.toMap(ElectricityMemberCardOrder::getOrderId, Function.identity(), (key, key1) -> key1));
        }
    
        //套餐总的云豆数
        BigDecimal totalCloudBean = BigDecimal.ZERO;
        for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
            ElectricityMemberCardOrder electricityMemberCardOrder = orderMap.get(anotherPayMembercardRecord.getOrderId());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                continue;
            }
        
            totalCloudBean = totalCloudBean.add(electricityMemberCardOrder.getPayAmount());
        }
    
        log.info("RECYCLE BATTERY MEMBERCARD INFO!totalCloudBean={},uid={}", totalCloudBean.doubleValue(), userInfo.getUid());
    
        // 租退电记录详情
        List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList = rentRecordDetailService.queryListByUid(userInfo.getUid());
        // 没有租电记录
        if (ObjectUtils.isEmpty(enterpriseRentRecordDetailList)) {
            recycleEmptyRentRecord(anotherPayMembercardRecords, orderMap, userInfo, enterpriseInfo);
            return Triple.of(true, null, totalCloudBean);
        }
    
        // 过滤非法数据
        enterpriseRentRecordDetailList = enterpriseRentRecordDetailList.stream().filter(item -> Objects.nonNull(item.getRentTime()) && Objects.nonNull(item.getReturnTime())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(enterpriseRentRecordDetailList)) {
            recycleEmptyRentRecord(anotherPayMembercardRecords, orderMap, userInfo, enterpriseInfo);
            return Triple.of(true, null, totalCloudBean);
        }
    
        List<String> orderIdList = enterpriseRentRecordDetailList.stream().map(EnterpriseRentRecordDetail::getOrderId).distinct().collect(Collectors.toList());
        
        
        List<CloudBeanUseRecord> cloudBeanUseRecordList = new ArrayList<>();
        // 将租电记录根据订单id进行分组
        Map<String, List<EnterpriseRentRecordDetail>> recordDetailMap = enterpriseRentRecordDetailList.stream()
                .collect(Collectors.groupingBy(EnterpriseRentRecordDetail::getOrderId));
        List<CloudBeanUseRecordDetail> cloudBeanUseRecordDetailList = new ArrayList<>();
        long currentTimeMillis = System.currentTimeMillis();
        //租退电消耗的云豆数
        BigDecimal totalUsedCloudBean = BigDecimal.ZERO;
    
        for (String orderId : orderIdList) {
            AnotherPayMembercardRecord payRecord = payMembercardRecordMap.get(orderId);
            if (Objects.isNull(payRecord)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found pay record,uid={},orderId={}", userInfo.getUid(), orderId);
                continue;
            }
        
            ElectricityMemberCardOrder electricityMemberCardOrder = orderMap.get(orderId);
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not find member card order,uid={},orderId={}", userInfo.getUid(), orderId);
                continue;
            }
        
            int totalUseDay = 0;
            List<EnterpriseRentRecordDetail> detailList = recordDetailMap.get(orderId);
    
            // 租退在同一天的按照同一天的按照一天计算，不在同一天的则开始的第一天不作为消耗天数计算
            for (EnterpriseRentRecordDetail rentRecordDetail : detailList) {
                int day = getRentDayNum(rentRecordDetail.getRentTime(), rentRecordDetail.getReturnTime());
                totalUseDay = totalUseDay + day;
            }
    
            List<Long> rentDetailIdList = detailList.stream().map(EnterpriseRentRecordDetail::getId).collect(Collectors.toList());
            CloudBeanUseRecordDetail cloudBeanUseRecordDetail = CloudBeanUseRecordDetail.builder().uid(userInfo.getUid()).enterpriseId(enterpriseInfo.getId()).orderId(orderId)
                    .startTime(payRecord.getBeginTime()).endTime(payRecord.getEndTime()).totalUseDay(totalUseDay)
                    .rentRecordDetail(StringUtils.join(rentDetailIdList, StringConstant.COMMA_EN)).tenantId(userInfo.getTenantId()).createTime(currentTimeMillis)
                    .updateTime(currentTimeMillis).build();
    
            cloudBeanUseRecordDetailList.add(cloudBeanUseRecordDetail);
        
        
            // 套餐回收的总的消耗天数大于订单的有效天数则按照有效天数进行云豆回收
            if (totalUseDay > electricityMemberCardOrder.getValidDays()) {
                totalUseDay = electricityMemberCardOrder.getValidDays();
                log.info("RECYCLE BATTERY MEMBERCARD INFO!cloud bean use day is error, uid={}, orderId={}", userInfo.getUid(), orderId);
            }
            
            // 订单每天的单价
            BigDecimal price = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
            // 花费金额
            BigDecimal usedAmount = price.multiply(BigDecimal.valueOf(totalUseDay)).setScale(2, RoundingMode.HALF_UP);
            // 剩余金额
            BigDecimal residueAmount = electricityMemberCardOrder.getPayAmount().subtract(usedAmount);
    
            // 套餐全部用完
            if (Objects.equals(totalUseDay, electricityMemberCardOrder.getValidDays())) {
                usedAmount = electricityMemberCardOrder.getPayAmount();
                residueAmount = BigDecimal.ZERO;
            }
            
            // 一般出现在套餐用完的情况下
            if (Objects.equals(residueAmount.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                residueAmount = BigDecimal.ZERO;
                log.info("RECYCLE BATTERY MEMBERCARD INFO!residue amount is error, uid={}, orderId={}", userInfo.getUid(), orderId);
            }
            
            // 总的使用的云豆数量
            totalUsedCloudBean = totalUsedCloudBean.add(usedAmount);
            // 设置企业的剩余云豆
            enterpriseInfo.setTotalBeanAmount(
                    enterpriseInfo.getTotalBeanAmount().add(residueAmount));
            
            log.info("RECYCLE BATTERY MEMBERCARD INFO!return battery used cloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
        
            //回收记录
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(userInfo.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
            cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
            cloudBeanUseRecord.setBeanAmount(residueAmount);
            cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
            cloudBeanUseRecord.setPackageId(userBatteryMemberCard.getMemberCardId());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        
            cloudBeanUseRecordList.add(cloudBeanUseRecord);
    
            orderMap.remove(orderId);
        }
        
        // 批量保存回收套餐记录
        if (ObjectUtils.isNotEmpty(cloudBeanUseRecordList)) {
            cloudBeanUseRecordService.batchInsert(cloudBeanUseRecordList);
        }
    
        // 批量保存回收套餐记录详情
        if (ObjectUtils.isNotEmpty(cloudBeanUseRecordDetailList)) {
            Map<String, Long> cloudBeanUseRecordMap = cloudBeanUseRecordList.stream().collect(Collectors.toMap(CloudBeanUseRecord::getRef, CloudBeanUseRecord::getId, (key, key1) -> key1));
            cloudBeanUseRecordDetailList.parallelStream().forEach(item -> {
                Long cloudBeanUseRecordId = cloudBeanUseRecordMap.get(item.getOrderId());
                if (Objects.nonNull(cloudBeanUseRecordId)) {
                    item.setCloudBeanUseRecordId(cloudBeanUseRecordId);
                }
            });
        
            cloudBeanUseRecordDetailService.batchInsert(cloudBeanUseRecordDetailList);
        }
    
        if (!CollectionUtils.isEmpty(orderMap)) {
            orderMap.forEach((k, v) -> {
                enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(v.getPayAmount()));
            
                CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                cloudBeanUseRecord.setUid(userInfo.getUid());
                cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
                cloudBeanUseRecord.setBeanAmount(v.getPayAmount());
                cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
                cloudBeanUseRecord.setPackageId(userBatteryMemberCard.getMemberCardId());
                cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                cloudBeanUseRecord.setRef(k);
                cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                cloudBeanUseRecordService.insert(cloudBeanUseRecord);
            
                log.info("RECYCLE BATTERY MEMBERCARD INFO!not used membercard cloudBean={},uid={}", v.getPayAmount().doubleValue(), userInfo.getUid());
            });
        }
    
        BigDecimal res = totalCloudBean.subtract(totalUsedCloudBean);
        // 如果结果小于零 则默认为零
        if (Objects.equals(res.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
            res = BigDecimal.ZERO;
            log.info("RECYCLE BATTERY MEMBERCARD INFO! cloud count is minus totalCloudBean={},totalUsedCloudBean={},uid={}", totalCloudBean.doubleValue(), totalUsedCloudBean.doubleValue(), userInfo.getUid());
        }
    
        //待回收云豆=总的支付的云豆-总消耗的云豆
        return Triple.of(true, null, result.add(res));
    }
    
    @Slave
    @Override
    public List<EnterpriseInfo> queryList(Integer tenantId) {
        return enterpriseInfoMapper.selectList(tenantId);
    }
    
    @Override
    public void deleteCacheByEnterpriseId(Long enterpriseId) {
        redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + enterpriseId);
    }
    
    private int getRentDayNum(Long beginTime, Long endTime) {
        int maxDaySize = 1;
        
        // 计算分段的最大天数
        List<DateTime> dateTimes = DateUtil.rangeToList(new Date(DateUtils.getTimeByTimeStamp(beginTime)), new Date(DateUtils.getTimeByTimeStamp(endTime)), DateField.DAY_OF_MONTH);
        if (dateTimes.size() > 1) {
            maxDaySize = dateTimes.size() - 1;
        }
        
        return maxDaySize;
    }
    
    private void recycleEmptyRentRecord(List<AnotherPayMembercardRecord> anotherPayMembercardRecords, Map<String, ElectricityMemberCardOrder> orderMap,
            UserInfo userInfo, EnterpriseInfo enterpriseInfo) {
        List<CloudBeanUseRecord> cloudBeanUseRecordList = new ArrayList<>();
        
        //生成退租记录、回收记录
        for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
            ElectricityMemberCardOrder electricityMemberCardOrder = orderMap.get(anotherPayMembercardRecord.getOrderId());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                continue;
            }
            
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(userInfo.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
            cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
            cloudBeanUseRecord.setBeanAmount(electricityMemberCardOrder.getPayAmount());
            cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount().add(electricityMemberCardOrder.getPayAmount()));
            cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
            cloudBeanUseRecordList.add(cloudBeanUseRecord);
            
            enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(electricityMemberCardOrder.getPayAmount()));
        }
        
        if (ObjectUtils.isNotEmpty(cloudBeanUseRecordList)) {
            cloudBeanUseRecordService.batchInsert(cloudBeanUseRecordList);
        }
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteById(Long id) {
        int delete = this.enterpriseInfoMapper.deleteById(id);
        
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + id);
            return null;
        });
        
        return delete;
    }
    
    @Slave
    @Override
    public List<EnterpriseInfoVO> selectByPage(EnterpriseInfoQuery query) {
        List<EnterpriseInfoPackageVO> list = this.enterpriseInfoMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        
        Set<Long> enterpriseIdList = list.stream().map(EnterpriseInfoPackageVO::getId).collect(Collectors.toSet());
        List<EnterpriseChannelUser> enterpriseChannelUserList = enterpriseChannelUserService.listByEnterpriseId(enterpriseIdList);
        Map<Long, Integer> userMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(enterpriseChannelUserList)) {
            userMap = enterpriseChannelUserList.stream().collect(Collectors.groupingBy(EnterpriseChannelUser::getEnterpriseId, Collectors.collectingAndThen(Collectors.toList(), e -> e.size())));
        }
        
        Map<Long, Integer> finalUserMap = userMap;
        return list.stream().map(item -> {
            EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
            BeanUtils.copyProperties(item, enterpriseInfoVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            enterpriseInfoVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            enterpriseInfoVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
            enterpriseInfoVO.setPhone(Objects.isNull(userInfo) ? "" : userInfo.getPhone());
            
            Pair<List<Long>, List<String>> listPair = getMembercardNames(item.getId());
            enterpriseInfoVO.setMemcardName(listPair.getRight());
            enterpriseInfoVO.setPackageIds(listPair.getLeft());
            
            Optional.ofNullable(finalUserMap.get(item.getId())).ifPresent(integer -> {
                enterpriseInfoVO.setChannelUserCount(integer);
            });
            return enterpriseInfoVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer selectByPageCount(EnterpriseInfoQuery query) {
        return this.enterpriseInfoMapper.selectByPageCount(query);
    }
    
    @Override
    public Triple<Boolean, String, Object> rechargeForUser(UserCloudBeanRechargeQuery query, HttpServletRequest request) {
        Long uid = SecurityUtils.getUid();
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("CLOUD BEAN RECHARGE ERROR! not found user,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CLOUD_BEAN_RECHARGE_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            if (Objects.equals(user.getLockFlag(), User.USER_LOCK)) {
                log.warn("CLOUD BEAN RECHARGE ERROR! user is unUsable,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            EnterpriseInfo enterpriseInfo = this.selectByUid(uid);
            if (Objects.isNull(enterpriseInfo)) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found enterpriseInfo,uid={}", uid);
                return Triple.of(false, "100315", "企业配置信息不存在");
            }
            
            if (query.getTotalBeanAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
                log.error("CLOUD BEAN RECHARGE ERROR!illegal totalBeanAmount,uid={},totalBeanAmount={}", uid, query.getTotalBeanAmount());
                return Triple.of(false, "100314", "支付金额不合法");
            }
            
            //查询支付配置详情
            WechatPayParamsDetails wechatPayParamsDetails = null;
    
            try {
                wechatPayParamsDetails  = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(TenantContextHolder.getTenantId(), enterpriseInfo.getFranchiseeId());
            } catch (Exception e) {
                log.error("CLOUD BEAN RECHARGE ERROR, get wechat pay params details error, tenantId = {}, franchiseeId={}", TenantContextHolder.getTenantId(), enterpriseInfo.getFranchiseeId(), e);
                return Triple.of(false, "PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
            }
            
            if (Objects.isNull(wechatPayParamsDetails)) {
                log.error("CLOUD BEAN RECHARGE ERROR, wechat pay params details is null, tenantId = {}, franchiseeId={}", TenantContextHolder.getTenantId(), enterpriseInfo.getFranchiseeId());
                return Triple.of(false, "120017", "未配置支付参数");
            }
    
    
            //兼容商户小程序充值
            wechatPayParamsDetails.setMerchantMinProAppId(merchantConfig.getMerchantAppletId());
            wechatPayParamsDetails.setMerchantAppletSecret(merchantConfig.getMerchantAppletSecret());
            
            
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(uid, TenantContextHolder.getTenantId(),UserOauthBind.SOURCE_WX_PRO);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found userOauthBind,uid={}", uid);
                return Triple.of(false, "100314", "未找到用户的第三方授权信息!");
            }
            
            //生成充值订单
            EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
            enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
            enterpriseCloudBeanOrder.setUid(uid);
            enterpriseCloudBeanOrder.setOperateUid(uid);
            enterpriseCloudBeanOrder.setPayAmount(query.getTotalBeanAmount());
            enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
            enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_INIT);
            enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.ONLINE_PAYMENT);
            enterpriseCloudBeanOrder.setType(EnterpriseCloudBeanOrder.TYPE_USER_RECHARGE);
            enterpriseCloudBeanOrder.setRemark("");
            enterpriseCloudBeanOrder.setBeanAmount(query.getTotalBeanAmount());
            enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            enterpriseCloudBeanOrder.setTenantId(user.getTenantId());
            enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
            enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
            enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
            
            CommonPayOrder commonPayOrder = CommonPayOrder.builder().orderId(enterpriseCloudBeanOrder.getOrderId()).uid(uid).payAmount(query.getTotalBeanAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_CLOUD_BEAN_RECHARGE).attach(ElectricityTradeOrder.ATTACH_CLOUD_BEAN_RECHARGE).description("云豆充值")
                    .tenantId(TenantContextHolder.getTenantId()).build();
    
            WechatJsapiOrderResultDTO resultDTO = electricityTradeOrderService
                    .commonCreateTradeOrderAndGetPayParams(commonPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (Exception e) {
            log.error("CLOUD BEAN RECHARGE ERROR! recharge fail,uid={}", uid, e);
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_CLOUD_BEAN_RECHARGE_LOCK_KEY + SecurityUtils.getUid());
        }
        
        return Triple.of(false, "", "支付未成功，请联系客服处理");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery) {
        if (CollectionUtils.isEmpty(enterpriseInfoQuery.getPackageIds())) {
            return Triple.of(false, "", "参数不合法");
        }
        
        EnterpriseInfo enterpriseInfoOld = this.selectByUid(enterpriseInfoQuery.getUid());
        if (Objects.nonNull(enterpriseInfoOld)) {
            return Triple.of(false, "", "用户已存在");
        }
        
        EnterpriseInfo enterpriseInfoExit = this.selectByName(enterpriseInfoQuery.getName());
        if (Objects.nonNull(enterpriseInfoExit)) {
            return Triple.of(false, "", "企业已存在");
        }
        
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfo);
        enterpriseInfo.setBusinessId(Long.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + RandomUtil.randomInt(1000, 9999)));
        enterpriseInfo.setRecoveryMode(EnterpriseInfo.RECOVERY_MODE_RETURN);
        enterpriseInfo.setTotalBeanAmount(BigDecimal.ZERO);
        enterpriseInfo.setDelFlag(EnterpriseInfo.DEL_NORMAL);
        enterpriseInfo.setTenantId(TenantContextHolder.getTenantId());
        enterpriseInfo.setCreateTime(System.currentTimeMillis());
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        this.enterpriseInfoMapper.insert(enterpriseInfo);
        
        enterpriseInfoQuery.setId(enterpriseInfo.getId());
        
        List<EnterprisePackage> packageList = enterpriseInfoQuery.getPackageIds().stream().map(item -> {
            EnterprisePackage enterprisePackage = new EnterprisePackage();
            enterprisePackage.setEnterpriseId(enterpriseInfo.getId());
            enterprisePackage.setPackageId(item);
            enterprisePackage.setPackageType(enterpriseInfoQuery.getPackageType());
            enterprisePackage.setTenantId(enterpriseInfo.getTenantId());
            enterprisePackage.setCreateTime(System.currentTimeMillis());
            enterprisePackage.setUpdateTime(System.currentTimeMillis());
            return enterprisePackage;
        }).collect(Collectors.toList());
        
        enterprisePackageService.batchInsert(packageList);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseInfoQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "100315", "企业配置不存在");
        }
        
        EnterpriseInfo enterpriseInfoOld = this.selectByUid(enterpriseInfoQuery.getUid());
        if (Objects.nonNull(enterpriseInfoOld) && !Objects.equals(enterpriseInfoOld.getUid(), enterpriseInfo.getUid())) {
            return Triple.of(false, "", "用户已存在");
        }
        
        EnterpriseInfo enterpriseInfoExit = this.selectByName(enterpriseInfoQuery.getName());
        if (Objects.nonNull(enterpriseInfoExit) && !Objects.equals(enterpriseInfoExit.getId(), enterpriseInfo.getId())) {
            return Triple.of(false, "", "企业已存在");
        }
        
        enterprisePackageService.deleteByEnterpriseId(enterpriseInfo.getId());
        if (!CollectionUtils.isEmpty(enterpriseInfoQuery.getPackageIds())) {
            List<EnterprisePackage> packageList = enterpriseInfoQuery.getPackageIds().stream().map(item -> {
                EnterprisePackage enterprisePackage = new EnterprisePackage();
                enterprisePackage.setEnterpriseId(enterpriseInfo.getId());
                enterprisePackage.setPackageId(item);
                enterprisePackage.setPackageType(enterpriseInfoQuery.getPackageType());
                enterprisePackage.setTenantId(enterpriseInfo.getTenantId());
                enterprisePackage.setCreateTime(System.currentTimeMillis());
                enterprisePackage.setUpdateTime(System.currentTimeMillis());
                return enterprisePackage;
            }).collect(Collectors.toList());
            enterprisePackageService.batchInsert(packageList);
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfoUpdate);
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateMerchantEnterprise(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseInfoQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "120212", "商户不存在");
        }
        
        EnterpriseInfo enterpriseInfoExit = this.selectByName(enterpriseInfoQuery.getName());
        if (Objects.nonNull(enterpriseInfoExit) && !Objects.equals(enterpriseInfoExit.getId(), enterpriseInfo.getId())) {
            return Triple.of(false, "120233", "商户名称重复，请修改后操作");
        }
        
        enterprisePackageService.deleteByEnterpriseId(enterpriseInfo.getId());
        if (!CollectionUtils.isEmpty(enterpriseInfoQuery.getPackageIds())) {
            List<EnterprisePackage> packageList = enterpriseInfoQuery.getPackageIds().stream().map(item -> {
                EnterprisePackage enterprisePackage = new EnterprisePackage();
                enterprisePackage.setEnterpriseId(enterpriseInfo.getId());
                enterprisePackage.setPackageId(item);
                enterprisePackage.setPackageType(enterpriseInfoQuery.getPackageType());
                enterprisePackage.setTenantId(enterpriseInfo.getTenantId());
                enterprisePackage.setCreateTime(System.currentTimeMillis());
                enterprisePackage.setUpdateTime(System.currentTimeMillis());
                return enterprisePackage;
            }).collect(Collectors.toList());
            enterprisePackageService.batchInsert(packageList);
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfoUpdate);
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> delete(Long id) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(id);
        if (Objects.isNull(enterpriseInfo) || !Objects.equals(TenantContextHolder.getTenantId(), enterpriseInfo.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        //校验企业用户云豆是否都已回收
        if (enterpriseChannelUserService.queryNotRecycleUserCount(id) > 0) {
            return Triple.of(false, null, "企业用户云豆未回收");
        }
        
        if (BigDecimal.ZERO.compareTo(enterpriseInfo.getTotalBeanAmount()) < 0) {
            return Triple.of(false, null, "企业云豆未结算");
        }
        
        enterpriseChannelUserService.deleteByEnterpriseId(id);
        
        enterprisePackageService.deleteByEnterpriseId(id);
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setDelFlag(EnterpriseInfo.DEL_DEL);
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> deleteMerchantEnterprise(Long id) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(id);
        if (Objects.isNull(enterpriseInfo) || !Objects.equals(TenantContextHolder.getTenantId(), enterpriseInfo.getTenantId())) {
            return Triple.of(false, "120212", "商户不存在");
        }
        
        // 校验企业是否存在自主续费关闭的用户
        if (enterpriseChannelUserService.existsRenewCloseUser(id) > 0) {
            return Triple.of(false, "120238", "该商户下还有代付用户，暂无法删除，请联系商户开启名下骑手自主续费，解除关系后操作");
        }
        
        if (BigDecimal.ZERO.compareTo(enterpriseInfo.getTotalBeanAmount()) < 0) {
            return Triple.of(false, "120237", "该商户下还有未回收的云豆，请先处理后操作");
        }
        
        enterpriseChannelUserService.deleteByEnterpriseId(id);
        
        enterprisePackageService.deleteByEnterpriseId(id);
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setDelFlag(EnterpriseInfo.DEL_DEL);
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> rechargeForAdmin(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseCloudBeanRechargeQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "100315", "企业配置不存在");
        }
        
        // 检测加盟商的绑定的id和企业的加盟商的id是否一致
        if (ObjectUtils.isNotEmpty(enterpriseCloudBeanRechargeQuery.getBindFranchiseeIdList()) && !enterpriseCloudBeanRechargeQuery.getBindFranchiseeIdList().contains(enterpriseInfo.getFranchiseeId())) {
            log.info("recharge for admin cloud bean info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", enterpriseCloudBeanRechargeQuery.getId(), enterpriseInfo.getFranchiseeId(), enterpriseCloudBeanRechargeQuery.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        if (Objects.equals(EnterpriseCloudBeanOrder.TYPE_ADMIN_DEDUCT, enterpriseCloudBeanRechargeQuery.getType())) {
            enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().subtract(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        } else {
            enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        }
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        
        //云豆订单
        EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
        enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
        enterpriseCloudBeanOrder.setUid(enterpriseInfo.getUid());
        enterpriseCloudBeanOrder.setOperateUid(SecurityUtils.getUid());
        enterpriseCloudBeanOrder
                .setPayAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
        enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
        enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.OFFLINE_PAYMENT);
        enterpriseCloudBeanOrder.setType(enterpriseCloudBeanRechargeQuery.getType());
        enterpriseCloudBeanOrder
                .setBeanAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        enterpriseCloudBeanOrder.setTenantId(enterpriseInfo.getTenantId());
        enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setRemark(enterpriseCloudBeanRechargeQuery.getRemark());
        enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
        
        //云豆记录
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(enterpriseInfo.getUid());
        cloudBeanUseRecord.setType(enterpriseCloudBeanRechargeQuery.getType());
        cloudBeanUseRecord
                .setBeanAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfoUpdate.getTotalBeanAmount());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(enterpriseCloudBeanOrder.getOrderId());
        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public EnterpriseInfo selectByUid(Long uid) {
        return this.enterpriseInfoMapper.selectByUid(uid);
    }
    
    @Slave
    @Override
    public EnterpriseInfo selectByName(String name) {
        return this.enterpriseInfoMapper
                .selectOne(new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getDelFlag, EnterpriseInfo.DEL_NORMAL).eq(EnterpriseInfo::getName, name).last("limit 0,1"));
    }
    
    @Slave
    @Override
    public EnterpriseInfoVO selectDetailByUid(Long uid) {
        EnterpriseInfo enterpriseInfo = this.enterpriseInfoMapper.selectByUid(uid);
        if (Objects.isNull(enterpriseInfo)) {
            return null;
        }
        
        EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
        BeanUtils.copyProperties(enterpriseInfo, enterpriseInfoVO);
        
        User userInfo = userService.queryByUidFromCache(enterpriseInfo.getUid());
        enterpriseInfoVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
        
        return enterpriseInfoVO;
    }
    
    @Slave
    @Override
    public EnterpriseInfoVO selectEnterpriseInfoByUid(Long uid) {
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectByUid(uid);
        EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
        BeanUtil.copyProperties(enterpriseInfo, enterpriseInfoVO);
        
        return enterpriseInfoVO;
    }
    
    @Slave
    @Override
    public UserCloudBeanDetailVO cloudBeanDetail() {
        EnterpriseInfo enterpriseInfo = this.selectByUid(SecurityUtils.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("USER CLOUD BEAN DETAIL ERROR!not found enterpriseInfo,uid={}", SecurityUtils.getUid());
            return null;
        }
        
        UserCloudBeanDetailVO userCloudBeanDetailVO = new UserCloudBeanDetailVO();
        userCloudBeanDetailVO.setTotalCloudBean(enterpriseInfo.getTotalBeanAmount());
        
        //已分配云豆数
        Double distributableCloudBean = cloudBeanUseRecordService.selectCloudBeanByEnterpriseIdAndType(enterpriseInfo.getId(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
        userCloudBeanDetailVO.setDistributableCloudBean(Objects.isNull(distributableCloudBean) ? NumberConstant.ZERO_D : distributableCloudBean);
        
        //已回收云豆数
        Double recoveredCloudBean = cloudBeanUseRecordService.selectCloudBeanByEnterpriseIdAndType(enterpriseInfo.getId(), CloudBeanUseRecord.TYPE_RECYCLE);
        userCloudBeanDetailVO.setRecoveredCloudBean(Objects.isNull(recoveredCloudBean) ? NumberConstant.ZERO_D : recoveredCloudBean);
        
        //可回收云豆数
        List<AnotherPayMembercardRecord> canRecycleList = anotherPayMembercardRecordService.selectListByEnterpriseId(enterpriseInfo.getId());
        if (CollectionUtils.isEmpty(canRecycleList)) {
            userCloudBeanDetailVO.setRecyclableCloudBean(BigDecimal.ZERO.doubleValue());
        } else {
            BigDecimal canRecycleCloudBean = BigDecimal.ZERO;
            List<Long> uidList = canRecycleList.stream().map(AnotherPayMembercardRecord::getUid).distinct().collect(Collectors.toList());
            for (Long uid : uidList) {
                canRecycleCloudBean = canRecycleCloudBean.add(cloudBeanUseRecordService.acquireUserCanRecycleCloudBean(uid));
                userCloudBeanDetailVO.setRecyclableCloudBean(canRecycleCloudBean.doubleValue());
            }
        }
        
        return userCloudBeanDetailVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> recycleCloudBean(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("RECYCLE WARN! not found user,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.CACHE_RECYCLE_CLOUD_BEAN_LOCK + uid, String.valueOf(System.currentTimeMillis()), 3000L, false)) {
            log.warn("RECYCLE WARN! Frequency too fast");
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("RECYCLE WARN! user is unUsable,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("RECYCLE WARN! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        //        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
        //            log.warn("RECYCLE WARN! user not pay deposit,uid={}", uid);
        //            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        //        }
        
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("RECYCLE WARN! user rent battery,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0045", "已绑定电池");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("RECYCLE WARN! userBatteryMemberCard is null,uid={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("RECYCLE WARN! user's member card is stop,uid={}", uid);
            return Triple.of(false, "100211", "该骑手套餐已暂停，无法回收云豆，请联系骑手启用后操作");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("RECYCLE WARN! user stop member card review,uid={}", uid);
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("RECYCLE WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                .acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("RECYCLE WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("RECYCLE WARN! user illegal,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseChannelUser.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("RECYCLE CLOUD BEAN ERROR! not found enterpriseInfo,enterpriseId={},uid={}", enterpriseChannelUser.getEnterpriseId(), uid);
            return Triple.of(false, "ELECTRICITY.0019", "企业信息不存在");
        }
        
        //回收押金
        Triple<Boolean, String, Object> batteryDepositTriple = recycleBatteryDeposit(userInfo, enterpriseInfo);
        if (Boolean.FALSE.equals(batteryDepositTriple.getLeft())) {
            return batteryDepositTriple;
        }
        
        //回收套餐
        Triple<Boolean, String, Object> membercardTriple = recycleBatteryMemberCardV2(userInfo, enterpriseInfo, userBatteryMemberCard);
        if (Boolean.FALSE.equals(membercardTriple.getLeft())) {
            return membercardTriple;
        }
        
        //解绑用户相关信息
        unbindUserData(userInfo, enterpriseChannelUser);
        
        BigDecimal membercardTotalCloudBean = (BigDecimal) membercardTriple.getRight();
        BigDecimal batteryDepositTotalCloudBean = (BigDecimal) batteryDepositTriple.getRight();
        
        this.addCloudBean(enterpriseInfo.getId(), membercardTotalCloudBean.add(batteryDepositTotalCloudBean));
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public void unbindUserData(UserInfo userInfo, EnterpriseChannelUser enterpriseChannelUser) {
        //清除用户租退电、购买套餐记录
        anotherPayMembercardRecordService.deleteByUid(userInfo.getUid());
        
        enterpriseRentRecordService.deleteByUid(userInfo.getUid());
    
        enterpriseRentRecordDetailService.removeByUid(userInfo.getUid());
        
        //更新用户云豆状态为已回收
        EnterpriseChannelUser enterpriseChannelUserUpdate = new EnterpriseChannelUser();
        enterpriseChannelUserUpdate.setId(enterpriseChannelUser.getId());
        enterpriseChannelUserUpdate.setCloudBeanStatus(CloudBeanStatusEnum.RECOVERED.getCode());
        enterpriseChannelUserUpdate.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
        enterpriseChannelUserUpdate.setUpdateTime(System.currentTimeMillis());
        enterpriseChannelUserService.update(enterpriseChannelUserUpdate);
        
        // 因为再回收押金之前已经做过为空判断，无需再次进行判断
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        boolean isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
        
        // 企业渠道用户
        if (!isMember) {
            //解绑用户绑定信息
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
            
            //更新用户套餐订单为已失效
            electricityMemberCardOrderService.batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()),
                    ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            
            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
                //更新用户保险订单为已失效
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
            }
            
            //退押金解绑用户所属加盟商
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            
            //更新用户套餐订单为已失效
            electricityMemberCardOrderService
                    .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            
            //删除用户电池套餐资源包
            userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
            
            //删除用户电池型号
            userBatteryTypeService.deleteByUid(userInfo.getUid());
            
            //删除用户电池服务费
            serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
        } else {
            //更新用户套餐订单为已失效
            electricityMemberCardOrderService.batchUpdateChannelOrderStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()),
                    ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            
            // 如果会员的当前的电池套餐为企业套餐则删除
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(userBatteryMemberCard)) {
                Long memberCardId = userBatteryMemberCard.getMemberCardId();
                BatteryMemberCard batteryMemberCard = memberCardService.queryByIdFromCache(memberCardId);
                if (Objects.nonNull(batteryMemberCard) && Objects.equals(batteryMemberCard.getBusinessType(), BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)) {
                    userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                    
                    //删除用户电池服务费
                    serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
                }
                
                if (Objects.nonNull(batteryMemberCard) && !Objects.equals(batteryMemberCard.getBusinessType(), BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)) {
                    // 当前套餐为换电套餐并且存在企业套餐则需将：t_user_battery_member_card的总的到期时间和次数减去未使用的企业套餐的总的时间和次数
                    List<UserBatteryMemberCardPackage> packages = userBatteryMemberCardPackageService.queryChannelListByUid(userInfo.getUid());
                    
                    if (ObjectUtils.isNotEmpty(packages)) {
                        Long expireTimeSum = 0L;
                        Long remainingNumber = 0L;
                        
                        for (UserBatteryMemberCardPackage memberCardPackage : packages) {
                            BatteryMemberCard batteryMemberCard1 = memberCardService.queryByIdFromCache(memberCardPackage.getMemberCardId());
                            if (Objects.isNull(batteryMemberCard1)) {
                                log.error("recycle cloud bean batter member card is null, memberCardId={}", memberCardPackage.getMemberCardId());
                                continue;
                            }
                            
                            String orderId = memberCardPackage.getOrderId();
                            ElectricityMemberCardOrder memberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderId);
                            if (Objects.isNull(memberCardOrder)) {
                                log.error("recycle cloud bean batter member card order is null, orderId={}", orderId);
                                continue;
                            }
                            
                            Long expireTime = batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard1, memberCardOrder);
                            expireTimeSum += expireTime;
                            // 剩余次数
                            if (Objects.nonNull(memberCardOrder.getMaxUseCount())) {
                                remainingNumber += memberCardOrder.getMaxUseCount();
                            }
                        }
                        
                        // 修改用户套餐信息
                        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                        userBatteryMemberCardUpdate.setId(userBatteryMemberCard.getId());
                        userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                        
                        if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime())) {
                            userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - expireTimeSum);
                        }
                        
                        if (Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) && remainingNumber > NumberConstant.ZERO_L) {
                            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() - remainingNumber);
                        }
                        
                        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
                    }
                    
                }
                
                
            }
            //删除用户电池套餐资源包
            userBatteryMemberCardPackageService.deleteChannelMemberCardByUid(userInfo.getUid());
        }
    }
    
    
    @Override
    public Triple<Boolean, String, Object> recycleBatteryMembercard(UserInfo userInfo, EnterpriseInfo enterpriseInfo, UserBatteryMemberCard userBatteryMemberCard) {
        BigDecimal result = BigDecimal.ZERO;
        
        List<AnotherPayMembercardRecord> anotherPayMembercardRecords = anotherPayMembercardRecordService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(anotherPayMembercardRecords)) {
            return Triple.of(true, null, result);
        }
        
        Map<String, AnotherPayMembercardRecord> payMembercardRecordMap = anotherPayMembercardRecords.stream().collect(Collectors.toMap(AnotherPayMembercardRecord::getOrderId, entity -> entity));
        
        //套餐订单
        Map<String, ElectricityMemberCardOrder> orderMap = new HashMap<>();
        
        //套餐总的云豆数
        BigDecimal totalCloudBean = BigDecimal.ZERO;
        for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(anotherPayMembercardRecord.getOrderId());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                continue;
            }
            
            orderMap.put(electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder);
            
            totalCloudBean = totalCloudBean.add(electricityMemberCardOrder.getPayAmount());
        }
        log.info("RECYCLE BATTERY MEMBERCARD INFO!totalCloudBean={},uid={}", totalCloudBean.doubleValue(), userInfo.getUid());
        
        //租退电记录
        List<EnterpriseRentRecord> enterpriseRentRecords = enterpriseRentRecordService.selectByUid(userInfo.getUid());
        
        //若未租退电
        if (CollectionUtils.isEmpty(enterpriseRentRecords)) {
            //生成退租记录、回收记录
            for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(anotherPayMembercardRecord.getOrderId());
                if (Objects.isNull(electricityMemberCardOrder)) {
                    log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                    continue;
                }
                
                CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                cloudBeanUseRecord.setUid(userInfo.getUid());
                cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
                cloudBeanUseRecord.setBeanAmount(electricityMemberCardOrder.getPayAmount());
                cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount().add(electricityMemberCardOrder.getPayAmount()));
                cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
                cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
                cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                
                enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(electricityMemberCardOrder.getPayAmount()));
            }
            return Triple.of(true, null, totalCloudBean);
        }
        
        //租退电消耗的云豆数
        BigDecimal totalUsedCloudBean = BigDecimal.ZERO;
        
        //同一个套餐内租退电记录ids
        List<Long> ids = Lists.newArrayList();
        
        for (EnterpriseRentRecord enterpriseRentRecord : enterpriseRentRecords) {
            
            //移除同一个套餐内租退电记录
            if (ids.contains(enterpriseRentRecord.getId())) {
                continue;
            }
            
            if (Objects.nonNull(enterpriseRentRecord.getRentTime()) && Objects.nonNull(enterpriseRentRecord.getReturnTime())) {
                //租退电是否在同一个套餐内
                if (ObjectUtil.equal(enterpriseRentRecord.getRentMembercardOrderId(), enterpriseRentRecord.getReturnMembercardOrderId())) {
                    AnotherPayMembercardRecord returnAnotherPayMembercardRecord = payMembercardRecordMap.getOrDefault(enterpriseRentRecord.getReturnMembercardOrderId(), null);
                    
                    //当前套餐租退电总消耗时间
                    long totalUseDay = 0L;
                    
                    //获取当前套餐内的租退电记录
                    List<EnterpriseRentRecord> rentRecordList = enterpriseRentRecords.stream().filter(item -> Objects.equals(item.getRentMembercardOrderId(), enterpriseRentRecord.getRentMembercardOrderId()) && Objects.equals(item.getReturnMembercardOrderId(), item.getRentMembercardOrderId())).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(rentRecordList)) {
                        for (EnterpriseRentRecord rentRecord : rentRecordList) {
                            Long beginTime = rentRecord.getRentTime();
                            Long endTime = Objects.nonNull(returnAnotherPayMembercardRecord) && Objects.nonNull(returnAnotherPayMembercardRecord.getEndTime()) && Objects.nonNull(rentRecord.getReturnTime()) && rentRecord.getReturnTime() > returnAnotherPayMembercardRecord.getEndTime() ? returnAnotherPayMembercardRecord.getEndTime() : rentRecord.getReturnTime();
                            totalUseDay = totalUseDay + DateUtils.diffDayV2(beginTime, endTime);
                            
                            ids.add(rentRecord.getId());
                        }
                    }
                    
                    log.info("RECYCLE BATTERY MEMBERCARD INFO! rent battery totalUseDay={},uid={}", totalUseDay, userInfo.getUid());
                    
                    ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(enterpriseRentRecord.getRentMembercardOrderId());
                    if (Objects.nonNull(electricityMemberCardOrder)) {
                        
                        BigDecimal price = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
                        
                        totalUsedCloudBean = totalUsedCloudBean.add(price.multiply(BigDecimal.valueOf(totalUseDay)));
                        log.info("RECYCLE BATTERY MEMBERCARD INFO!return battery used cloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                        
                        enterpriseInfo.setTotalBeanAmount(
                                enterpriseInfo.getTotalBeanAmount().add(electricityMemberCardOrder.getPayAmount().subtract(price.multiply(BigDecimal.valueOf(totalUseDay)))));
                        
                        //回收记录
                        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                        cloudBeanUseRecord.setUid(userInfo.getUid());
                        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                        cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
                        cloudBeanUseRecord.setBeanAmount(electricityMemberCardOrder.getPayAmount().subtract(price.multiply(BigDecimal.valueOf(totalUseDay))));
                        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
                        cloudBeanUseRecord.setPackageId(userBatteryMemberCard.getMemberCardId());
                        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                        cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
                        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                        
                        orderMap.remove(electricityMemberCardOrder.getOrderId());
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
                        log.info("RECYCLE BATTERY MEMBERCARD INFO!rentUsedCloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                        
                        enterpriseInfo.setTotalBeanAmount(
                                enterpriseInfo.getTotalBeanAmount().add(rentElectricityMemberCardOrder.getPayAmount().subtract(price.multiply(BigDecimal.valueOf(useDays)))));
                        
                        //回收记录
                        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                        cloudBeanUseRecord.setUid(userInfo.getUid());
                        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                        cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
                        cloudBeanUseRecord.setBeanAmount(rentElectricityMemberCardOrder.getPayAmount().subtract(price.multiply(BigDecimal.valueOf(useDays))));
                        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
                        cloudBeanUseRecord.setPackageId(userBatteryMemberCard.getMemberCardId());
                        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                        cloudBeanUseRecord.setRef(rentElectricityMemberCardOrder.getOrderId());
                        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                        
                        orderMap.remove(rentElectricityMemberCardOrder.getOrderId());
                    }
                    
                    List<String> membercardList = anotherPayMembercardRecords.stream().map(AnotherPayMembercardRecord::getOrderId).collect(Collectors.toList());
                    
                    if (!CollectionUtils.isEmpty(membercardList) && membercardList.size() > 1) {
                        //租退电包含的套餐订单
                        List<String> containMembercardList = membercardList.subList(membercardList.indexOf(enterpriseRentRecord.getRentMembercardOrderId()) + 1,
                                membercardList.indexOf(enterpriseRentRecord.getReturnMembercardOrderId()));
                        if (!CollectionUtils.isEmpty(containMembercardList)) {
                            for (String orderId : containMembercardList) {
                                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
                                if (Objects.nonNull(electricityMemberCardOrder)) {
                                    totalUsedCloudBean = totalUsedCloudBean.add(electricityMemberCardOrder.getPayAmount());
                                    log.info("RECYCLE BATTERY MEMBERCARD INFO!containUsedCloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                                    
                                    orderMap.remove(electricityMemberCardOrder.getOrderId());
                                }
                            }
                        }
                    }
                    
                    //退电消耗的云豆
                    AnotherPayMembercardRecord returnAnotherPayMembercardRecord = payMembercardRecordMap.getOrDefault(enterpriseRentRecord.getReturnMembercardOrderId(),null);
                    ElectricityMemberCardOrder returnElectricityMemberCardOrder = electricityMemberCardOrderService
                            .selectByOrderNo(enterpriseRentRecord.getReturnMembercardOrderId());
                    if (Objects.nonNull(returnAnotherPayMembercardRecord) && Objects.nonNull(returnElectricityMemberCardOrder)) {
                        //单价
                        BigDecimal price = returnElectricityMemberCardOrder.getPayAmount()
                                .divide(BigDecimal.valueOf(returnElectricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
                        
                        Long beginTime = returnAnotherPayMembercardRecord.getBeginTime();
                        
                        Long endTime =  Objects.nonNull(returnAnotherPayMembercardRecord.getEndTime()) && Objects.nonNull(enterpriseRentRecord.getReturnTime()) && enterpriseRentRecord.getReturnTime() > returnAnotherPayMembercardRecord.getEndTime() ? returnAnotherPayMembercardRecord.getEndTime() : enterpriseRentRecord.getReturnTime();
                        
                        //使用天数
                        long useDays = DateUtils.diffDayV2( beginTime, endTime);
                        
                        totalUsedCloudBean = totalUsedCloudBean.add(price.multiply(BigDecimal.valueOf(useDays)));
                        log.info("RECYCLE BATTERY MEMBERCARD INFO!returnUsedCloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
                        
                        enterpriseInfo.setTotalBeanAmount(
                                enterpriseInfo.getTotalBeanAmount().add(returnElectricityMemberCardOrder.getPayAmount().subtract(price.multiply(BigDecimal.valueOf(useDays)))));
                        
                        //回收记录
                        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                        cloudBeanUseRecord.setUid(userInfo.getUid());
                        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                        cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
                        cloudBeanUseRecord.setBeanAmount(returnElectricityMemberCardOrder.getPayAmount().subtract(price.multiply(BigDecimal.valueOf(useDays))));
                        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
                        cloudBeanUseRecord.setPackageId(userBatteryMemberCard.getMemberCardId());
                        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                        cloudBeanUseRecord.setRef(returnElectricityMemberCardOrder.getOrderId());
                        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                        
                        orderMap.remove(returnElectricityMemberCardOrder.getOrderId());
                    }
                }
            }
        }
        
        if (!CollectionUtils.isEmpty(orderMap)) {
            orderMap.forEach((k, v) -> {
                enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(v.getPayAmount()));
                
                CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                cloudBeanUseRecord.setUid(userInfo.getUid());
                cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD);
                cloudBeanUseRecord.setBeanAmount(v.getPayAmount());
                cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
                cloudBeanUseRecord.setPackageId(userBatteryMemberCard.getMemberCardId());
                cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                cloudBeanUseRecord.setRef(k);
                cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                
                log.info("RECYCLE BATTERY MEMBERCARD INFO!not used membercard cloudBean={},uid={}", v.getPayAmount().doubleValue(), userInfo.getUid());
            });
        }
        BigDecimal res = totalCloudBean.subtract(totalUsedCloudBean);
        // 如果结果小于零 则默认为零
        if (Objects.equals(res.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
            res = BigDecimal.ZERO;
            log.info("RECYCLE BATTERY MEMBERCARD INFO! cloud count is minus totalCloudBean={},totalUsedCloudBean={},uid={}", totalCloudBean.doubleValue(), totalUsedCloudBean.doubleValue(), userInfo.getUid());
        }
        
        //待回收云豆=总的支付的云豆-总消耗的云豆
        return Triple.of(true, null, result.add(res));
    }
    
    @Override
    public Triple<Boolean, String, Object> recycleBatteryDeposit(UserInfo userInfo, EnterpriseInfo enterpriseInfo) {
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("RECYCLE BATTERY DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if(Objects.isNull(eleDepositOrder)){
            log.warn("RECYCLE BATTERY DEPOSIT WARN!not found eleDepositOrder,uid={}", userInfo.getUid());
            return Triple.of(false, "100221", "未找到订单");
        }
        
        if(!Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())){
            return Triple.of(true, null, BigDecimal.ZERO);
        }
        
        // 免押回收默认为零
        BigDecimal batteryDeposit = userBatteryDeposit.getBatteryDeposit();
        if (Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE)) {
            batteryDeposit = BigDecimal.ZERO;
        }
        
        enterpriseInfo.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(batteryDeposit));
        
        //保存回收记录
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(userInfo.getUid());
        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
        cloudBeanUseRecord.setOrderType(CloudBeanUseRecord.ORDER_TYPE_BATTERY_DEPOSIT);
        cloudBeanUseRecord.setBeanAmount(batteryDeposit);
        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfo.getTotalBeanAmount());
        cloudBeanUseRecord.setPackageId(userBatteryDeposit.getDid());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(userBatteryDeposit.getOrderId());
        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
        
        Integer status = EleRefundOrder.STATUS_SUCCESS;
        // 如果是免押则改为退款中
        if (Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            status = EleRefundOrder.STATUS_REFUND;
        }
        //生成退押订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(userBatteryDeposit.getOrderId())
                .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, userInfo.getUid())).payAmount(userBatteryDeposit.getBatteryDeposit())
                .refundAmount(userBatteryDeposit.getBatteryDeposit()).status(status).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).memberCardOweNumber(0).payType(eleDepositOrder.getPayType())
                .build();
        eleRefundOrderService.insert(eleRefundOrder);
        
        //记录企业用户退押记录
        enterpriseUserCostRecordService.asyncSaveUserCostRecordForRefundDeposit(userInfo.getUid(), UserCostTypeEnum.COST_TYPE_REFUND_DEPOSIT.getCode(), eleRefundOrder);
        
        if(!Objects.equals( eleDepositOrder.getPayType(),EleDepositOrder.FREE_DEPOSIT_PAYMENT)){
            return Triple.of(true, null, userBatteryDeposit.getBatteryDeposit());
        }
        
        threadPool.execute(() -> {
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
            if (Objects.isNull(freeDepositOrder)) {
                log.error("RECYCLE BATTERY DEPOSIT ERROR! not found freeDepositOrder,uid={}", userInfo.getUid());
                return;
            }
            
            //更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);
            
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrder.getTenantId());
            if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
                log.error("REFUND ORDER ERROR! not found pxzConfig,uid={}", userInfo.getUid());
                return;
            }
            
            PxzCommonRequest<PxzFreeDepositUnfreezeRequest> testQuery = new PxzCommonRequest<>();
            testQuery.setAesSecret(pxzConfig.getAesKey());
            testQuery.setDateTime(System.currentTimeMillis());
            testQuery.setSessionId(eleRefundOrder.getOrderId());
            testQuery.setMerchantCode(pxzConfig.getMerchantCode());
            
            PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
            queryRequest.setRemark("电池押金解冻");
            queryRequest.setTransId(freeDepositOrder.getOrderId());
            testQuery.setData(queryRequest);
            
            PxzCommonRsp<PxzDepositUnfreezeRsp> pxzUnfreezeDepositCommonRsp = null;
            
            try {
                pxzUnfreezeDepositCommonRsp = pxzDepositService.unfreezeDeposit(testQuery);
            } catch (Exception e) {
                log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId(), e);
                return;
            }
            
            if (Objects.isNull(pxzUnfreezeDepositCommonRsp)) {
                log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId());
                return;
            }
            
            if (!pxzUnfreezeDepositCommonRsp.isSuccess()) {
                log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={},result={}", userInfo.getUid(), freeDepositOrder.getOrderId(),
                        pxzUnfreezeDepositCommonRsp.getRespDesc());
                return;
            }
            
            if (Objects.equals(pxzUnfreezeDepositCommonRsp.getData().getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
                //更新免押订单状态
                FreeDepositOrder updateFreeDepositOrder = new FreeDepositOrder();
                updateFreeDepositOrder.setId(freeDepositOrder.getId());
                updateFreeDepositOrder.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
                updateFreeDepositOrder.setUpdateTime(System.currentTimeMillis());
                freeDepositOrderService.update(updateFreeDepositOrder);
                
                // 修改退押订单为退款成功
                updateEleRefundOrder(eleRefundOrder.getId(), EleRefundOrder.STATUS_SUCCESS);
            }
        });
        
        return Triple.of(true, null, batteryDeposit);
    }
    
    private void updateEleRefundOrder(Long id, Integer status) {
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(id);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderUpdate.setStatus(status);
        eleRefundOrderService.updateById(eleRefundOrderUpdate);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> cloudBeanGeneralView() {
        EnterpriseInfo enterpriseInfo = this.selectByUid(SecurityUtils.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("ENTERPRISE ERROR! not found enterpriseInfo,uid={} ", SecurityUtils.getUid());
            return Triple.of(true, null, null);
        }
        
        CloudBeanGeneralViewVO cloudBeanGeneralViewVO = new CloudBeanGeneralViewVO();
        cloudBeanGeneralViewVO.setCanAllocationCloudBean(enterpriseInfo.getTotalBeanAmount().doubleValue());
        
        //代付订单
        List<CloudBeanUseRecord> payRecords = cloudBeanUseRecordService.selectByEnterpriseIdAndType(enterpriseInfo.getId(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
        if (CollectionUtils.isEmpty(payRecords)) {
            cloudBeanGeneralViewVO.setAllocationCloudBean(0D);
            cloudBeanGeneralViewVO.setAllocationMembercard(0);
            cloudBeanGeneralViewVO.setAllocationUser(0L);
        } else {
            double allocationCloudBean = payRecords.stream().mapToDouble(item -> item.getBeanAmount().doubleValue()).sum();
            cloudBeanGeneralViewVO.setAllocationCloudBean(BigDecimal.valueOf(allocationCloudBean).setScale(2, RoundingMode.HALF_UP).doubleValue());
            cloudBeanGeneralViewVO.setAllocationMembercard(payRecords.size());
            cloudBeanGeneralViewVO.setAllocationUser(payRecords.stream().map(CloudBeanUseRecord::getUid).distinct().count());
        }
        
        //已回收订单
        List<CloudBeanUseRecord> recycleRecords = cloudBeanUseRecordService.selectByEnterpriseIdAndType(enterpriseInfo.getId(), CloudBeanUseRecord.TYPE_RECYCLE);
        if (CollectionUtils.isEmpty(recycleRecords)) {
            cloudBeanGeneralViewVO.setRecycleCloudBean(0D);
            cloudBeanGeneralViewVO.setRecycleMembercard(0);
            cloudBeanGeneralViewVO.setRecycleUser(0L);
        } else {
            double sum = recycleRecords.stream().mapToDouble(item -> item.getBeanAmount().doubleValue()).sum();
            cloudBeanGeneralViewVO.setRecycleCloudBean(BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());
            cloudBeanGeneralViewVO.setRecycleMembercard(
                    (int) recycleRecords.stream().filter(item -> Objects.equals(CloudBeanUseRecord.ORDER_TYPE_BATTERY_MEMBERCARD, item.getOrderType())).count());
            cloudBeanGeneralViewVO.setRecycleUser(recycleRecords.stream().map(CloudBeanUseRecord::getUid).distinct().count());
        }
        
        //可回收订单
        List<AnotherPayMembercardRecord> canRecycleList = anotherPayMembercardRecordService.selectListByEnterpriseId(enterpriseInfo.getId());
        if (CollectionUtils.isEmpty(canRecycleList)) {
            cloudBeanGeneralViewVO.setCanRecycleCloudBean(0D);
            cloudBeanGeneralViewVO.setCanRecycleMembercard(0);
            cloudBeanGeneralViewVO.setCanRecycleUser(0L);
        } else {
            
            List<AnotherPayMembercardRecord> recycleList = canRecycleList.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingLong(AnotherPayMembercardRecord::getUid))), ArrayList::new));
            
            cloudBeanGeneralViewVO.setCanRecycleUser(recycleList.size());
            cloudBeanGeneralViewVO.setCanRecycleMembercard(canRecycleList.size());
            
            BigDecimal canRecycleCloudBean = BigDecimal.ZERO;
            for (AnotherPayMembercardRecord anotherPayMembercardRecord : recycleList) {
                canRecycleCloudBean = canRecycleCloudBean.add(cloudBeanUseRecordService.acquireUserCanRecycleCloudBean(anotherPayMembercardRecord.getUid()));
            }
            cloudBeanGeneralViewVO.setCanRecycleCloudBean(canRecycleCloudBean.setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
        
        return Triple.of(true, null, cloudBeanGeneralViewVO);
    }
    
    @Slave
    @Override
    public List<EnterprisePurchasedPackageResultVO> queryPurchasedPackageCount(EnterprisePurchaseOrderQuery query) {
        
        List<EnterprisePurchasedPackageResultVO> enterprisePurchasedPackageResultVOList = Lists.newArrayList();
        
        EnterprisePurchasedPackageResultVO expiredPackageResult = new EnterprisePurchasedPackageResultVO();
        Integer expiredPackageCount = enterpriseBatteryPackageMapper.queryExpiredPackageOrderCount(query);
        expiredPackageResult.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
        expiredPackageResult.setRecordSize(expiredPackageCount);
        enterprisePurchasedPackageResultVOList.add(expiredPackageResult);
        
        EnterprisePurchasedPackageResultVO unpaidPackageResult = new EnterprisePurchasedPackageResultVO();
        Integer unpaidPackageCount = enterpriseBatteryPackageMapper.queryUnpaidPackageOrderCount(query);
        unpaidPackageResult.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
        unpaidPackageResult.setRecordSize(unpaidPackageCount);
        enterprisePurchasedPackageResultVOList.add(unpaidPackageResult);
        
        EnterprisePurchasedPackageResultVO paidPackageResult = new EnterprisePurchasedPackageResultVO();
        Integer paidPackageCount = enterpriseBatteryPackageMapper.queryPaidPackageOrderCount(query);
        paidPackageResult.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_SUCCESS.getCode());
        paidPackageResult.setRecordSize(paidPackageCount);
        enterprisePurchasedPackageResultVOList.add(paidPackageResult);
        
        return enterprisePurchasedPackageResultVOList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateAllRenewalStatus(EnterpriseInfoQuery enterpriseInfoQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("update all renewal status error! not found user info, uid = {} ", uid);
            throw new BizException("300075", "当前用户不存在");
        }
        
        EnterpriseInfo enterpriseInfo = this.selectByUid(uid);
        if (Objects.isNull(enterpriseInfo)) {
            log.error("update all renewal status error! not found enterpriseInfo,uid={} ", SecurityUtils.getUid());
            throw new BizException("300076", "用户所属企业不存在");
        }
        
        /*if(!enterpriseInfo.getId().equals(enterpriseInfoQuery.getId())){
            log.error("update all renewal status error! enterprise director not match current user, request params = {}, current user id = {} ", enterpriseInfoQuery.getId(), uid);
            throw new BizException("300077", "企业负责人和当前用户不匹配");
        }*/
        
        Integer renewalStatus = RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode();
        if (RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode().equals(enterpriseInfoQuery.getRenewalStatus())) {
            renewalStatus = RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode();
        } else if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseInfoQuery.getRenewalStatus())) {
            renewalStatus = RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode();
        }
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().enterpriseId(enterpriseInfo.getId()).renewalStatus(renewalStatus)
                .tenantId(tenantId.longValue()).build();
        
        List<EnterpriseChannelUser> enterpriseChannelUserList = enterpriseChannelUserService.queryChannelUserList(enterpriseChannelUserQuery);
        
        List<Long> enterpriseUserIds = enterpriseChannelUserList.stream().map(EnterpriseChannelUser::getId).collect(Collectors.toList());
        
        if (CollectionUtil.isNotEmpty(enterpriseUserIds)) {
            enterpriseChannelUserService.batchUpdateRenewStatus(enterpriseUserIds, enterpriseInfoQuery.getRenewalStatus());
        }
        EnterpriseInfo enterprise = new EnterpriseInfo();
        enterprise.setId(enterpriseInfo.getId());
        enterprise.setRenewalStatus(enterpriseInfoQuery.getRenewalStatus());
        enterprise.setUpdateTime(System.currentTimeMillis());
        
        log.info("Update all renewal status = {}", enterpriseInfoQuery.getRenewalStatus());
        
        Integer result = enterpriseInfoMapper.update(enterprise);
        
        return result;
    }
    
    @Override
    public Boolean checkUserType() {
        User userInfo = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ENTERPRISE ERROR! not found user info,uid={} ", SecurityUtils.getUid());
            return Boolean.FALSE;
        }
        
        EnterpriseInfo enterpriseInfo = this.selectByUid(userInfo.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            return Boolean.FALSE;
        }
        
        if (Objects.equals(EnterpriseInfo.STATUS_OPEN, enterpriseInfo.getStatus())) {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    private Pair<List<Long>, List<String>> getMembercardNames(Long id) {
        
        List<Long> membercardIds = enterprisePackageService.selectByEnterpriseId(id);
        if (CollectionUtils.isEmpty(membercardIds)) {
            return Pair.of(Lists.newArrayList(), Lists.newArrayList());
        }
        
        List<String> nameList = Lists.newArrayList();
        List<Long> idList = Lists.newArrayList();
        membercardIds.forEach(e -> {
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(e);
            if (Objects.nonNull(batteryMemberCard) && Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                nameList.add(batteryMemberCard.getName());
                idList.add(batteryMemberCard.getId());
            }
        });
        
        return Pair.of(idList, nameList);
    }
}
