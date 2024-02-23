package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.MerchantConstant;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.enums.merchant.PromotionFeeQueryTypeEnum;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionScanCodeQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPromotionFeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeIncomeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeRenewalVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeScanCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * @ClassName : MerchantPromotionFeeServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Service
@Slf4j
public class MerchantPromotionFeeServiceImpl implements MerchantPromotionFeeService {
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private RebateRecordService rebateRecordService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Override
    public R queryMerchantAvailableWithdrawAmount(Long uid) {
        //校验用户是否是商户
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        // 设置查询条件
        MerchantPromotionFeeQueryModel settleQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).tenantId(TenantContextHolder.getTenantId()).build();
        
        // 获取已结算的收益（数据来源：返利记录）
        BigDecimal settleIncome = rebateRecordService.sumByStatus(settleQueryModel);
        settleQueryModel.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
        // 获取已退回的收益（数据来源：返利记录）
        BigDecimal returnIncome = rebateRecordService.sumByStatus(settleQueryModel);
        
        return null;
    }
    
    @Override
    public R queryMerchantPromotionFeeIncome(Integer type, Long uid) {
        MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO = new MerchantPromotionFeeIncomeVO();
        
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        // 获取上月最后一天
        LocalDate lastMonthLastDay = lastMonthFirstDay.with(TemporalAdjusters.lastDayOfMonth());
        
        // 获取上月第一天的时间戳
        long dayOfMonthStartTime = lastMonthFirstDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // 获取上月最后一天的时间戳
        long dayOfMonthEndTime = lastMonthLastDay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        //商户
        if (Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            buildPromotionFeeIncomeVO(PromotionFeeQueryTypeEnum.MERCHANT.getCode(), uid, merchantPromotionFeeIncomeVO, dayOfMonthStartTime, dayOfMonthEndTime);
        }
        
        //场地员工
        if (Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode())) {
            buildPromotionFeeIncomeVO(PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode(), uid, merchantPromotionFeeIncomeVO, dayOfMonthStartTime, dayOfMonthEndTime);
        }
        return R.ok(merchantPromotionFeeIncomeVO);
    }
    
    @Override
    public R queryMerchantPromotionScanCode(Integer type, Long uid) {
        MerchantPromotionFeeScanCodeVO merchantPromotionFeeScanCodeVO = new MerchantPromotionFeeScanCodeVO();
        
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        // 获取上月最后一天
        LocalDate lastMonthLastDay = lastMonthFirstDay.with(TemporalAdjusters.lastDayOfMonth());
        
        // 获取上月第一天的时间戳
        long dayOfMonthStartTime = lastMonthFirstDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // 获取上月最后一天的时间戳
        long dayOfMonthEndTime = lastMonthLastDay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        buildPromotionFeePromotionScanCode(type, uid, merchantPromotionFeeScanCodeVO, dayOfMonthStartTime, dayOfMonthEndTime);
        
        return R.ok(merchantPromotionFeeScanCodeVO);
    }
    
    @Override
    public R queryMerchantPromotionRenewal(Integer type, Long uid) {
        MerchantPromotionFeeRenewalVO merchantPromotionFeeRenewalVO = new MerchantPromotionFeeRenewalVO();
        
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        // 获取上月最后一天
        LocalDate lastMonthLastDay = lastMonthFirstDay.with(TemporalAdjusters.lastDayOfMonth());
        
        // 获取上月第一天的时间戳
        long dayOfMonthStartTime = lastMonthFirstDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // 获取上月最后一天的时间戳
        long dayOfMonthEndTime = lastMonthLastDay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        buildPromotionFeePromotionRenewal(type, uid, merchantPromotionFeeRenewalVO, dayOfMonthStartTime, dayOfMonthEndTime);
        return R.ok(merchantPromotionFeeRenewalVO);
    }
    
    @Override
    public R statisticMerchantIncome(Integer type,Long uid,Long beginTime,Long endTime) {
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
    
        for (int i = 1; i < 8 ; i++) {
            long timeAgoStartTime = DateUtils.getTimeAgoStartTime(i);
            long timeAgoEndTime = DateUtils.getTimeAgoEndTime(i);
            
        }
        
        return null;
    }
    
    private void buildPromotionFeePromotionRenewal(Integer type, Long uid, MerchantPromotionFeeRenewalVO merchantPromotionFeeRenewalVO, long dayOfMonthStartTime,
            long dayOfMonthEndTime) {
        
        //今日续费次数：购买指定套餐时间=今日0点～当前时间，且套餐购买次数>1的购买成功次数
        MerchantPromotionRenewalQueryModel todayRenewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type).uid(uid)
                .startTime(DateUtils.getTodayStartTime()).endTime(DateUtils.getTodayEndTimeStamp()).status(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        Integer todayRenewalCount = rebateRecordService.countByTime(todayRenewalQueryModel);
        merchantPromotionFeeRenewalVO.setTodayRenewalCount(todayRenewalCount);
        
        //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
        MerchantPromotionRenewalQueryModel yesterdayRenewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type).uid(uid)
                .startTime(DateUtils.getTimeAgoStartTime(1)).endTime(DateUtils.getTimeAgoEndTime(1)).status(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        Integer yesterdayRenewalCount = rebateRecordService.countByTime(yesterdayRenewalQueryModel);
        merchantPromotionFeeRenewalVO.setYesterdayRenewalCount(yesterdayRenewalCount);
        
        //本月续费次数：购买指定套餐时间=本月1号0点～当前时间，且套餐购买次数>1的购买成功次数
        MerchantPromotionRenewalQueryModel currentMonthRenewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .uid(uid).startTime(DateUtils.getDayOfMonthStartTime(1)).endTime(System.currentTimeMillis()).status(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        Integer currentMonthRenewalCount = rebateRecordService.countByTime(currentMonthRenewalQueryModel);
        merchantPromotionFeeRenewalVO.setCurrentMonthRenewalCount(currentMonthRenewalCount);
        
        //上月续费次数：购买指定套餐时间=上月1号0点～本月1号0点，且套餐购买次数>1的购买成功次数
        MerchantPromotionRenewalQueryModel lastMonthRenewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type).uid(uid)
                .startTime(dayOfMonthStartTime).endTime(dayOfMonthEndTime).status(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        Integer lastMonthRenewalCount = rebateRecordService.countByTime(lastMonthRenewalQueryModel);
        merchantPromotionFeeRenewalVO.setLastMonthRenewalCount(lastMonthRenewalCount);
        
        //累计续费次数：购买指定套餐时间<=当前时间，且套餐购买次数>1的购买成功次数
        MerchantPromotionRenewalQueryModel totalRenewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type).uid(uid)
                .endTime(System.currentTimeMillis()).status(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        Integer totalRenewalCount = rebateRecordService.countByTime(totalRenewalQueryModel);
        merchantPromotionFeeRenewalVO.setTotalRenewalCount(totalRenewalCount);
    }
    
    private void buildPromotionFeePromotionScanCode(Integer type, Long uid, MerchantPromotionFeeScanCodeVO merchantPromotionFeeScanCodeVO, long dayOfMonthStartTime,
            long dayOfMonthEndTime) {
        //今日扫码人数：扫码绑定时间=今日0点～当前时间；
        MerchantPromotionScanCodeQueryModel todayScanCodeCountQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(DateUtils.getTodayStartTime()).endTime(System.currentTimeMillis()).build();
        
        Integer todayScanCodeCount = merchantJoinRecordService.countByCondition(todayScanCodeCountQueryModel);
        merchantPromotionFeeScanCodeVO.setTodayScanCodeNum(todayScanCodeCount);
        
        //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
        MerchantPromotionScanCodeQueryModel yesterdayScanCodeCountQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(DateUtils.getTimeAgoStartTime(1)).endTime(DateUtils.getTimeAgoEndTime(1)).build();
        Integer yesterdayScanCodeCount = merchantJoinRecordService.countByCondition(yesterdayScanCodeCountQueryModel);
        merchantPromotionFeeScanCodeVO.setYesterdayScanCodeNum(yesterdayScanCodeCount);
        
        //本月扫码人数：扫码绑定时间=本月1号0点～当前时间
        MerchantPromotionScanCodeQueryModel currentMonthScanCodeCountQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                .type(type).inviterUid(uid).startTime(DateUtils.getDayOfMonthStartTime(1)).endTime(System.currentTimeMillis()).build();
        Integer currentMonthScanCodeCount = merchantJoinRecordService.countByCondition(currentMonthScanCodeCountQueryModel);
        merchantPromotionFeeScanCodeVO.setCurrentMonthScanCodeNum(currentMonthScanCodeCount);
        
        //上月扫码人数：扫码绑定时间=上月1号0点～本月1号0点
        MerchantPromotionScanCodeQueryModel lastMonthScanCodeCountQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(dayOfMonthStartTime).endTime(dayOfMonthEndTime).build();
        Integer lastMonthScanCodeCount = merchantJoinRecordService.countByCondition(lastMonthScanCodeCountQueryModel);
        merchantPromotionFeeScanCodeVO.setLastMonthScanCodeNum(lastMonthScanCodeCount);
        
        //累计扫码人数：扫码绑定时间<=当前时间
        MerchantPromotionScanCodeQueryModel totalScanCodeCountQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).endTime(System.currentTimeMillis()).build();
        Integer totalScanCodeCount = merchantJoinRecordService.countByCondition(totalScanCodeCountQueryModel);
        merchantPromotionFeeScanCodeVO.setTotalScanCodeNum(totalScanCodeCount);
        
        //今日成功人数：首次成功购买指定套餐时间=今日0点～当前时间，邀请状态=邀请成功
        MerchantPromotionScanCodeQueryModel todayPurchaseQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(DateUtils.getTodayStartTime()).endTime(System.currentTimeMillis()).status(MerchantJoinRecord.STATUS_SUCCESS).build();
        Integer todayPurchase = merchantJoinRecordService.countByCondition(todayPurchaseQueryModel);
        merchantPromotionFeeScanCodeVO.setTodayPurchaseNum(todayPurchase);
        
        //昨日成功人数：首次成功购买指定套餐时间=昨日0点～今日0点，邀请状态=邀请成功
        MerchantPromotionScanCodeQueryModel yesterdayPurchaseQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(DateUtils.getTimeAgoStartTime(1)).endTime(DateUtils.getTimeAgoEndTime(1)).status(MerchantJoinRecord.STATUS_SUCCESS).build();
        Integer yesterdayPurchase = merchantJoinRecordService.countByCondition(yesterdayPurchaseQueryModel);
        merchantPromotionFeeScanCodeVO.setYesterdayPurchaseNum(yesterdayPurchase);
        
        //本月成功人数：首次成功购买指定套餐时间=本月1号0点～当前时间，邀请状态=邀请成功
        MerchantPromotionScanCodeQueryModel currentMonthPurchaseQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(DateUtils.getDayOfMonthStartTime(1)).endTime(System.currentTimeMillis()).status(MerchantJoinRecord.STATUS_SUCCESS).build();
        Integer currentMonthPurchase = merchantJoinRecordService.countByCondition(currentMonthPurchaseQueryModel);
        merchantPromotionFeeScanCodeVO.setCurrentMonthPurchaseNum(currentMonthPurchase);
        
        //上月成功人数：首次成功购买指定套餐时间=上月1号0点～本月1号0点，邀请状态=邀请成功
        MerchantPromotionScanCodeQueryModel lastMonthPurchaseQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).startTime(dayOfMonthStartTime).endTime(dayOfMonthEndTime).status(MerchantJoinRecord.STATUS_SUCCESS).build();
        Integer lastMonthPurchase = merchantJoinRecordService.countByCondition(lastMonthPurchaseQueryModel);
        merchantPromotionFeeScanCodeVO.setLastMonthPurchaseNum(lastMonthPurchase);
        
        //累计成功人数：首次成功购买指定套餐时间<=当前时间，邀请状态=邀请成功
        MerchantPromotionScanCodeQueryModel totalPurchaseQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                .inviterUid(uid).endTime(System.currentTimeMillis()).status(MerchantJoinRecord.STATUS_SUCCESS).build();
        Integer totalPurchase = merchantJoinRecordService.countByCondition(totalPurchaseQueryModel);
        merchantPromotionFeeScanCodeVO.setTotalPurchaseNum(totalPurchase);
    }
    
    
    private void buildPromotionFeeIncomeVO(Integer type, Long uid, MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO, long dayOfMonthStartTime, long dayOfMonthEndTime) {
        // 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算；
        MerchantPromotionFeeQueryModel todayInComeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getTodayStartTimeStamp()).rebateEndTime(DateUtils.getTodayEndTimeStamp()).build();
        BigDecimal todayInCome = rebateRecordService.sumByStatus(todayInComeQueryModel);
        merchantPromotionFeeIncomeVO.setTodayIncome(todayInCome);
        
        // 昨日收入：“结算日期” = 今日，“结算状态” = 已结算 - 已退回；
        MerchantPromotionFeeQueryModel yesterdaySettleQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleStartTime(DateUtils.getTodayStartTimeStamp()).settleEndTime(DateUtils.getTodayEndTimeStamp()).build();
        BigDecimal todaySettleInCome = rebateRecordService.sumByStatus(yesterdaySettleQueryModel);
        
        MerchantPromotionFeeQueryModel yesterdayReturnQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getTimeAgoStartTime(1)).rebateEndTime(DateUtils.getTimeAgoEndTime(1)).build();
        BigDecimal yesterdayReturnInCome = rebateRecordService.sumByStatus(yesterdayReturnQueryModel);
        merchantPromotionFeeIncomeVO.setYesterdayIncome(todaySettleInCome.subtract(yesterdayReturnInCome));
        
        //本月预估收入：本月1号0点～当前时间，“结算状态” = 未结算+已结算-已退回；
        MerchantPromotionFeeQueryModel currentMonthNOSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)
                .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getDayOfMonthStartTime(1)).rebateEndTime(System.currentTimeMillis())
                .build();
        BigDecimal currentMonthNOSettleIncome = rebateRecordService.sumByStatus(currentMonthNOSettleIncomeQueryModel);
        
        MerchantPromotionFeeQueryModel currentMonthSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).settleStartTime(DateUtils.getDayOfMonthStartTime(1)).settleEndTime(System.currentTimeMillis())
                .build();
        BigDecimal currentMonthSettleIncome = rebateRecordService.sumByStatus(currentMonthSettleIncomeQueryModel);
        
        MerchantPromotionFeeQueryModel currentMonthReturnSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED)
                .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getDayOfMonthStartTime(1)).rebateEndTime(System.currentTimeMillis())
                .build();
        BigDecimal currentMonthReturnSettleIncome = rebateRecordService.sumByStatus(currentMonthReturnSettleIncomeQueryModel);
        merchantPromotionFeeIncomeVO.setCurrentMonthIncome(currentMonthNOSettleIncome.add(currentMonthSettleIncome).subtract(currentMonthReturnSettleIncome));
        
        //上月收入：上月1号0点～本月1号0点，“结算状态”= 已结算-已退回；
        MerchantPromotionFeeQueryModel lastMonthNOSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).settleStartTime(dayOfMonthStartTime).settleEndTime(dayOfMonthEndTime).build();
        BigDecimal lastMonthSettleIncome = rebateRecordService.sumByStatus(lastMonthNOSettleIncomeQueryModel);
        
        MerchantPromotionFeeQueryModel lastMonthReturnSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED)
                .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(dayOfMonthStartTime).rebateEndTime(dayOfMonthEndTime).build();
        BigDecimal lastMonthReturnSettleIncome = rebateRecordService.sumByStatus(lastMonthReturnSettleIncomeQueryModel);
        merchantPromotionFeeIncomeVO.setLastMonthIncome(lastMonthSettleIncome.subtract(lastMonthReturnSettleIncome));
        
        //累计收入：“结算日期” <= 今日，“结算状态” = 已结算 - 已退回；
        MerchantPromotionFeeQueryModel allSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleEndTime(DateUtils.getTodayEndTimeStamp()).build();
        BigDecimal allSettleIncome = rebateRecordService.sumByStatus(allSettleIncomeQueryModel);
        
        MerchantPromotionFeeQueryModel allReturnIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleEndTime(DateUtils.getTodayEndTimeStamp()).build();
        BigDecimal allReturnIncome = rebateRecordService.sumByStatus(allReturnIncomeQueryModel);
        merchantPromotionFeeIncomeVO.setLastMonthIncome(allSettleIncome.subtract(allReturnIncome));
    }
}
