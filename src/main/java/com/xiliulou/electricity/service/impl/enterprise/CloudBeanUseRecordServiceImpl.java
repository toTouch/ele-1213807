package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.enterprise.CloudBeanUseRecordConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.enterprise.*;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.enterprise.CloudBeanUseRecordTypeEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.mapper.enterprise.CloudBeanUseRecordMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserExitMapper;
import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanUseRecordQueryModel;
import com.xiliulou.electricity.request.enterprise.EnterpriseCloudBeanUseRecordPageRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.enterprise.*;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserBatteryMemberCardChannelExitVo;
import com.xiliulou.electricity.vo.enterprise.CloudBeanOrderExcelVO;
import com.xiliulou.electricity.vo.enterprise.CloudBeanSumVO;
import com.xiliulou.electricity.vo.enterprise.CloudBeanUseRecordExcelVO;
import com.xiliulou.electricity.vo.enterprise.CloudBeanUseRecordVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseCloudBeanOrderVO;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import com.xiliulou.storage.service.impl.AliyunOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
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
    
    @Resource
    private EnterpriseChannelUserExitMapper userExitMapper;
    
    @Resource
    private EnterpriseRentRecordDetailService enterpriseRentRecordDetailService;
    
    @Resource
    private EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private MerchantService merchantService;
    
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
    public BigDecimal acquireUserRecycledCloudBean(Long uid, Long enterpriseId) {
        List<CloudBeanUseRecord> cloudBeanUseRecords = this.cloudBeanUseRecordMapper
                .selectList(new LambdaQueryWrapper<CloudBeanUseRecord>().eq(CloudBeanUseRecord::getUid, uid).eq(CloudBeanUseRecord::getType, CloudBeanUseRecord.TYPE_RECYCLE).eq(CloudBeanUseRecord::getEnterpriseId, enterpriseId));
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
                log.warn("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                continue;
            }
            totalCloudBean = totalCloudBean.add(electricityMemberCardOrder.getPayAmount());
        }
        
        log.info("ACQUIRE CAN RECYCLE BATTERY MEMBERCARD INFO!totalCloudBean={},uid={}", totalCloudBean.doubleValue(), userInfo.getUid());
    
        // 租退电记录详情
        List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList = enterpriseRentRecordDetailService.queryListByUid(userInfo.getUid());
        // 没有租电记录
        if (ObjectUtils.isEmpty(enterpriseRentRecordDetailList)) {
            return result.add(totalCloudBean);
        }
        
        // 过滤非法数据
        enterpriseRentRecordDetailList = enterpriseRentRecordDetailList.stream().filter(item -> Objects.nonNull(item.getRentTime()) && Objects.nonNull(item.getReturnTime())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(enterpriseRentRecordDetailList)) {
            return result.add(totalCloudBean);
        }
        
        List<String> orderIdList = enterpriseRentRecordDetailList.stream().map(EnterpriseRentRecordDetail::getOrderId).distinct().collect(Collectors.toList());
        // 将租电记录根据订单id进行分组
        Map<String, List<EnterpriseRentRecordDetail>> recordDetailMap = enterpriseRentRecordDetailList.stream()
                .collect(Collectors.groupingBy(EnterpriseRentRecordDetail::getOrderId));
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
        
            // 套餐回收的总的消耗天数大于订单的有效天数则按照有效天数进行云豆回收
            if (totalUseDay > electricityMemberCardOrder.getValidDays()) {
                totalUseDay = electricityMemberCardOrder.getValidDays();
                log.info("RECYCLE BATTERY MEMBERCARD INFO!cloud bean use day is error, uid={}, orderId={}", userInfo.getUid(), orderId);
            }
        
            // 订单每天的单价
            BigDecimal price = electricityMemberCardOrder.getPayAmount().divide(BigDecimal.valueOf(electricityMemberCardOrder.getValidDays()), 2, RoundingMode.HALF_UP);
            // 花费金额
            BigDecimal usedAmount = price.multiply(BigDecimal.valueOf(totalUseDay)).setScale(2, RoundingMode.HALF_UP);
            // 总的使用的云豆数量
            totalUsedCloudBean = totalUsedCloudBean.add(usedAmount);
        
            log.info("RECYCLE BATTERY MEMBERCARD INFO!return battery used cloudBean={},uid={}", totalUsedCloudBean.doubleValue(), userInfo.getUid());
        }
        
        BigDecimal res = totalCloudBean.subtract(totalUsedCloudBean);
        // 如果结果小于零 则默认为零
        if (Objects.equals(res.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
            res = BigDecimal.ZERO;
            log.info("RECYCLE BATTERY MEMBERCARD INFO! cloud count is minus totalCloudBean={},totalUsedCloudBean={},uid={}", totalCloudBean.doubleValue(), totalUsedCloudBean.doubleValue(), userInfo.getUid());
        }
    
        //待回收云豆=总的支付的云豆-总消耗的云豆
        return result.add(res);
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
    
    @Override
    public void recycleCloudBeanExitTask() {
        int offset = 0;
        int size = 200;
        
        while (true) {
            List<UserBatteryMemberCardChannelExitVo> userBatteryMemberCardList = userBatteryMemberCardService.selectExpireExitList(offset, size);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                log.info("RECYCLE TASK WARN! userBatteryMemberCardList is empty");
                return;
            }
            
            userBatteryMemberCardList.forEach(memberCardChannelExitVo -> {
                UserBatteryMemberCard item = new UserBatteryMemberCard();
                BeanUtils.copyProperties(memberCardChannelExitVo, item);
                
                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                String errorMsg = "";
                
                if (Objects.isNull(userInfo)) {
                    log.error("RECYCLE TASK WARN! user Info is null,uid={}", item.getUid());
                    errorMsg = "RECYCLE TASK WARN! user Info is null";
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(userInfo.getUid());
                if (Objects.isNull(enterpriseChannelUser)) {
                    log.error("RECYCLE TASK WARN! channel user Info is null,uid={}", item.getUid());
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                // 如果已经回收了则直接修改状态
                if (Objects.equals(enterpriseChannelUser.getCloudBeanStatus(), EnterpriseChannelUser.CLOUD_BEAN_STATUS_RECYCLE)) {
                    // 修改历史退出为成功
                    userExitMapper.updateById(null, EnterpriseChannelUserExit.TYPE_SUCCESS, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    
                    // 修改企业用户为自主续费为退出
                    EnterpriseChannelUserQuery query = EnterpriseChannelUserQuery.builder().uid(userInfo.getUid()).renewalStatus(EnterpriseChannelUser.RENEWAL_OPEN).build();
                    enterpriseChannelUserService.updateRenewStatus(query);
                    return;
                }
                
                
                if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                    log.warn("RECYCLE TASK WARN! ser battery is not return,uid={}", item.getUid());
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
                    errorMsg = "RECYCLE TASK WARN! not found batteryMemberCard";
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                        .acquireUserBatteryServiceFee(userInfo, item, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                    log.warn("RECYCLE TASK WARN! user exist battery service fee,uid={}", userInfo.getUid());
                    errorMsg = "RECYCLE TASK WARN! user exist battery service fee";
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseChannelUser.getEnterpriseId());
                if (Objects.isNull(enterpriseInfo)) {
                    log.error("RECYCLE CLOUD BEAN TASK ERROR! not found enterpriseInfo,enterpriseId={}", enterpriseChannelUser.getEnterpriseId());
                    errorMsg = "RECYCLE CLOUD BEAN TASK ERROR! not found enterpriseInfo";
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                //回收押金
                Triple<Boolean, String, Object> batteryDepositTriple = enterpriseInfoService.recycleBatteryDeposit(userInfo, enterpriseInfo);
                if (!batteryDepositTriple.getLeft()) {
                    errorMsg = (String) batteryDepositTriple.getRight();
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                //回收套餐
                Triple<Boolean, String, Object> recycleBatteryMembercard = enterpriseInfoService.recycleBatteryMemberCardV2(userInfo, enterpriseInfo, item);
                if (Boolean.FALSE.equals(recycleBatteryMembercard.getLeft())) {
                    errorMsg = (String) recycleBatteryMembercard.getRight();
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                    return;
                }
                
                try {
                    //解绑用户数据
                    enterpriseInfoService.unbindUserData(userInfo, enterpriseChannelUser);
                    
                    // 修改企业用户为自主续费为退出
                    EnterpriseChannelUserQuery query = EnterpriseChannelUserQuery.builder().uid(userInfo.getUid()).renewalStatus(EnterpriseChannelUser.RENEWAL_OPEN).build();
                    enterpriseChannelUserService.updateRenewStatus(query);
                    
                    BigDecimal membercardTotalCloudBean = (BigDecimal) recycleBatteryMembercard.getRight();
                    BigDecimal batteryDepositTotalCloudBean = (BigDecimal) batteryDepositTriple.getRight();
                    
                    enterpriseInfoService.addCloudBean(enterpriseInfo.getId(), membercardTotalCloudBean.add(batteryDepositTotalCloudBean));
                    
                    // 修改历史退出为成功
                    userExitMapper.updateById(null, EnterpriseChannelUserExit.TYPE_SUCCESS, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                } catch (Exception e) {
                    log.error("recycle cloud bean exit error msg={}", e);
                    errorMsg = "recycle cloud bean exit error" + e.getMessage();
                    
                    userExitMapper.updateById(errorMsg, EnterpriseChannelUserExit.TYPE_FAIL, memberCardChannelExitVo.getChannelUserExitId(), System.currentTimeMillis());
                }
                
            });
            
            offset += size;
        }
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
                log.error("check cloud bean task calculate error enterpriseId={}, rechargeSum={}, totalBeanAmount={}", enterpriseId, rechargeSum, totalBeanAmount);
            }
        });
    }
    
    @Override
    public int batchInsert(List<CloudBeanUseRecord> cloudBeanUseRecordList) {
        return cloudBeanUseRecordMapper.batchInsert(cloudBeanUseRecordList);
    }
    
    @Override
    @Slave
    public List<EnterpriseCloudBeanOrderVO> listByPage(EnterpriseCloudBeanUseRecordPageRequest request) {
        EnterpriseCloudBeanUseRecordQueryModel queryModel = new EnterpriseCloudBeanUseRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        List<CloudBeanUseRecord> cloudBeanUseRecordList = this.cloudBeanUseRecordMapper.selectListByPage(queryModel);
    
        if (ObjectUtils.isEmpty(cloudBeanUseRecordList)) {
            return Collections.emptyList();
        }
    
        Set<String> orderIdList = new HashSet<>();
        Set<Long> enterpriseIdList = new HashSet<>();
        cloudBeanUseRecordList.stream().forEach(item -> {
            orderIdList.add(item.getRef());
            enterpriseIdList.add(item.getEnterpriseId());
        });
    
        List<EnterpriseCloudBeanOrder> orderList = enterpriseCloudBeanOrderService.listByOrderIdList(new ArrayList<>(orderIdList));
        Map<String, Long> operateUidMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(orderList)) {
            operateUidMap = orderList.stream()
                    .collect(Collectors.toMap(EnterpriseCloudBeanOrder::getOrderId, EnterpriseCloudBeanOrder::getOperateUid, (item1, item2) -> item1));
        }
    
        List<Merchant> merchantList = merchantService.listByEnterpriseList(new ArrayList<>(enterpriseIdList));
        Map<Long, String> merchantNameMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantList)) {
            merchantNameMap = merchantList.stream()
                    .collect(Collectors.toMap(Merchant::getEnterpriseId, Merchant::getName, (item1, item2) -> item1));
        }
    
        Map<String, Long> finalOperateUidMap = operateUidMap;
    
        Map<Long, String> finalMerchantNameMap = merchantNameMap;
        List<EnterpriseCloudBeanOrderVO> list = cloudBeanUseRecordList.parallelStream().map(item -> {
            EnterpriseCloudBeanOrderVO enterpriseCloudBeanOrderVO = new EnterpriseCloudBeanOrderVO();
            BeanUtils.copyProperties(item, enterpriseCloudBeanOrderVO);
        
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(item.getEnterpriseId());
            if (Objects.nonNull(enterpriseInfo)) {
                enterpriseCloudBeanOrderVO.setEnterpriseName(enterpriseInfo.getName());
                enterpriseCloudBeanOrderVO.setBusinessId(enterpriseInfo.getBusinessId());
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                enterpriseCloudBeanOrderVO.setFranchiseeName(franchisee.getName());
            }
    
            Long operateUid = finalOperateUidMap.get(item.getRef());
            if (ObjectUtils.isNotEmpty(operateUid)) {
                User user = userService.queryByUidFromCache(operateUid);
                if (Objects.nonNull(user)) {
                    enterpriseCloudBeanOrderVO.setOperateName(user.getName());
                }
            } else if (ObjectUtils.isNotEmpty(finalMerchantNameMap.get(item.getEnterpriseId()))) {
                enterpriseCloudBeanOrderVO.setOperateName(finalMerchantNameMap.get(item.getEnterpriseId()));
            }
        
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            enterpriseCloudBeanOrderVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
        
            return enterpriseCloudBeanOrderVO;
        }).collect(Collectors.toList());
    
        return list;
    }
    
    @Override
    @Slave
    public Integer countTotal(EnterpriseCloudBeanUseRecordPageRequest request) {
        EnterpriseCloudBeanUseRecordQueryModel queryModel = new EnterpriseCloudBeanUseRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        return cloudBeanUseRecordMapper.countTotal(queryModel);
    
    }
    
    @Override
    @Slave
    public void export(EnterpriseCloudBeanUseRecordPageRequest request, HttpServletResponse response) {
        EnterpriseCloudBeanUseRecordQueryModel queryModel = new EnterpriseCloudBeanUseRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        List<CloudBeanUseRecord> cloudBeanUseRecordList = this.cloudBeanUseRecordMapper.selectListByPage(queryModel);
    
        Set<String> enterpriseNameList = new HashSet<>();
        Map<String, List<CloudBeanUseRecordExcelVO>> enterpriseNameMap = new HashMap<>();
        
        cloudBeanUseRecordList.stream().forEach(item -> {
            CloudBeanUseRecordExcelVO enterpriseCloudBeanOrderVO = new CloudBeanUseRecordExcelVO();
            BeanUtils.copyProperties(item, enterpriseCloudBeanOrderVO);
        
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(item.getEnterpriseId());
            if (Objects.isNull(enterpriseInfo)) {
                return;
            }
            
            enterpriseCloudBeanOrderVO.setEnterpriseName(enterpriseInfo.getName());
    
            if (Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_ADMIN_RECHARGE) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_RECYCLE) || Objects.equals(
                    item.getType(), CloudBeanUseRecord.TYPE_PRESENT) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_USER_RECHARGE)) {
                enterpriseCloudBeanOrderVO.setIncomeAndExpend("收入");
            } else {
                enterpriseCloudBeanOrderVO.setIncomeAndExpend("支出");
            }
        
            CloudBeanUseRecordTypeEnum typeEnum = BasicEnum.getEnum(item.getType(), CloudBeanUseRecordTypeEnum.class);
            if (Objects.nonNull(typeEnum)) {
                enterpriseCloudBeanOrderVO.setType(typeEnum.getDesc());
            }
        
            if (Objects.nonNull(item.getCreateTime())) {
                enterpriseCloudBeanOrderVO.setCreateTime(DateUtil.format(new Date(item.getCreateTime()), DateFormatConstant.MONTH_DAY_DATE_TIME_FORMAT));
            }
    
            enterpriseNameList.add(enterpriseCloudBeanOrderVO.getEnterpriseName());
            
            if (ObjectUtils.isNotEmpty(enterpriseNameMap.get(enterpriseCloudBeanOrderVO.getEnterpriseName()))) {
                enterpriseNameMap.get(enterpriseCloudBeanOrderVO.getEnterpriseName()).add(enterpriseCloudBeanOrderVO);
            } else {
                List<CloudBeanUseRecordExcelVO> cloudBeanUseRecordExcelVOList = new ArrayList<>();
                cloudBeanUseRecordExcelVOList.add(enterpriseCloudBeanOrderVO);
                enterpriseNameMap.put(enterpriseCloudBeanOrderVO.getEnterpriseName(), cloudBeanUseRecordExcelVOList);
            }
        });
    
        
        List<String> sheetNameList = new ArrayList<>(enterpriseNameList);
        if (ObjectUtils.isEmpty(sheetNameList)) {
            sheetNameList.add(CloudBeanUseRecordConstant.default_sheet_name);
        }
        
        if (ObjectUtils.isEmpty(enterpriseNameMap)) {
            enterpriseNameMap.put(sheetNameList.get(0), new ArrayList<>());
        }
    
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application /vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(CloudBeanUseRecordConstant.export_file_name, StandardCharsets.UTF_8));
    
            ClassPathResource classPathResource = new ClassPathResource("excelTemplate" + File.separator + CloudBeanUseRecordConstant.export_template_name);
            InputStream stream = classPathResource.getInputStream();
            // 把excel流给这个对象，后续可以操作
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            // 设置模板的第一个sheet的名称，名称我们使用合同号
            workbook.setSheetName(0, sheetNameList.get(0));
            for (int i = 1; i < sheetNameList.size(); i++) {
                // 剩余的全部复制模板sheet0即可
                workbook.cloneSheet(0, sheetNameList.get(i));
            }
            
            // 把workbook写到流里
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            byte[] bytes = baos.toByteArray();
            stream = new ByteArrayInputStream(bytes);
            ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(stream).build();
    
            for (int i = 0; i < sheetNameList.size(); i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetNameList.get(i)).build();
                excelWriter.fill(enterpriseNameMap.get(sheetNameList.get(i)), writeSheet);
            }
    
            excelWriter.finish();
            baos.close();
            stream.close();
        } catch (Exception e) {
            log.error("cloud bean use record export error！", e);
        }
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
                Triple<Boolean, String, Object> recycleBatteryMembercard = enterpriseInfoService.recycleBatteryMemberCardV2(userInfo, enterpriseInfo, item);
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
    
    public static void main(String[] args) {
        CloudBeanUseRecordTypeEnum typeEnum = BasicEnum.getEnum(2, CloudBeanUseRecordTypeEnum.class);
        System.out.printf("");
    }
}
