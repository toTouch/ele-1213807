package com.xiliulou.electricity.service.impl.merchant;

import com.google.api.client.util.Lists;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.enums.merchant.PromotionFeeQueryTypeEnum;
import com.xiliulou.electricity.query.merchant.MerchantAllPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionScanCodeQueryModel;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantPromotionFeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionDataDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionEmployeeDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeIncomeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeRenewalVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeScanCodeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMerchantDetailVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisIncomeVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisMerchantVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisPurchaseVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisRenewalVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisUserScanCodeVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    
    @Resource
    private MerchantEmployeeService merchantEmployeeService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantWithdrawApplicationService merchantWithdrawApplicationService;
    
    @Override
    public R queryMerchantEmployees(Long merchantUid) {
        //校验用户是否是商户
        Merchant merchant = merchantService.queryByUid(merchantUid);
        if (Objects.isNull(merchant)) {
            log.error("find merchant user error, not found merchant user, uid = {}", merchantUid);
            return R.fail("120007", "未找到商户");
        }
        
        MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(merchantUid)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
        
        List<MerchantPromotionFeeEmployeeVO> promotionFeeEmployeeVOList = new ArrayList<>();
        
        if (merchantJoinRecordService.existInviterData(MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF, merchant.getUid(), TenantContextHolder.getTenantId())) {
            MerchantPromotionFeeEmployeeVO merchantVO = new MerchantPromotionFeeEmployeeVO();
            merchantVO.setType(PromotionFeeQueryTypeEnum.MERCHANT.getCode());
            merchantVO.setUserName(merchant.getName());
            merchantVO.setUid(merchant.getUid());
            promotionFeeEmployeeVOList.add(merchantVO);
        }
        
        if (CollectionUtils.isNotEmpty(merchantEmployees)) {
            List<MerchantPromotionFeeEmployeeVO> employeeVOList = merchantEmployees.parallelStream().map(merchantEmployee -> {
                MerchantPromotionFeeEmployeeVO employeeVO = new MerchantPromotionFeeEmployeeVO();
                employeeVO.setType(PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode());
                User user = userService.queryByUidFromCache(merchantEmployee.getUid());
                if (Objects.nonNull(user)) {
                    employeeVO.setUserName(user.getName());
                }
                employeeVO.setUid(merchantEmployee.getUid());
                return employeeVO;
            }).collect(Collectors.toList());
            promotionFeeEmployeeVOList.addAll(employeeVOList);
        }
        return R.ok(promotionFeeEmployeeVOList);
    }
    
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
        
        //审核中
        BigDecimal reviewInProgress = merchantWithdrawApplicationService.sumByStatus(TenantContextHolder.getTenantId(), MerchantWithdrawConstant.REVIEW_IN_PROGRESS, uid);
        
        //审核拒绝
        BigDecimal reviewRefused = merchantWithdrawApplicationService.sumByStatus(TenantContextHolder.getTenantId(), MerchantWithdrawConstant.REVIEW_REFUSED, uid);
        
        //审核成功
        BigDecimal reviewSuccess = merchantWithdrawApplicationService.sumByStatus(TenantContextHolder.getTenantId(), MerchantWithdrawConstant.REVIEW_SUCCESS, uid);
        
        //提现审核中
        BigDecimal withdrawInProgress = merchantWithdrawApplicationService.sumByStatus(TenantContextHolder.getTenantId(), MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS, uid);
        
        //提现成功
        BigDecimal withdrawSuccess = merchantWithdrawApplicationService.sumByStatus(TenantContextHolder.getTenantId(), MerchantWithdrawConstant.WITHDRAW_SUCCESS, uid);
        
        //提现失败
        BigDecimal withdrawFail = merchantWithdrawApplicationService.sumByStatus(TenantContextHolder.getTenantId(), MerchantWithdrawConstant.WITHDRAW_FAIL, uid);
        
        BigDecimal result = new BigDecimal(0);
        return R.ok(result.add(settleIncome).add(reviewRefused).add(withdrawFail).subtract(reviewInProgress).subtract(reviewSuccess).subtract(withdrawInProgress)
                .subtract(withdrawSuccess).subtract(returnIncome));
    }
    
    @Override
    public R queryMerchantPromotionFeeIncome(Integer type, Long uid) {
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        // 如果是默认首页，则type为4  因为员工的收入也包含在商户中 因此如果查全部，也就是查询的商户的
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
        }
        
        MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO = new MerchantPromotionFeeIncomeVO();
        
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        // 获取上月最后一天
        LocalDate lastMonthLastDay = lastMonthFirstDay.with(TemporalAdjusters.lastDayOfMonth());
        
        // 获取上月第一天的时间戳
        long dayOfMonthStartTime = lastMonthFirstDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // 获取上月最后一天的时间戳
        long dayOfMonthEndTime = lastMonthLastDay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        buildPromotionFeeIncomeVO(type, uid, merchantPromotionFeeIncomeVO, dayOfMonthStartTime, dayOfMonthEndTime);
        
        if (Objects.equals(type, PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode())) {
            // 新增商戶數
            buildMerchantNumVCountVO(uid, merchantPromotionFeeIncomeVO);
        }
        
        return R.ok(merchantPromotionFeeIncomeVO);
    }
    
    private void buildMerchantNumVCountVO(Long uid, MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO) {
        //今日新增商户数：渠道与商户绑定时间在今日0点～当前时间内
        merchantPromotionFeeIncomeVO.setTodayMerchantNum(buildMerchantNumCount(uid, DateUtils.getTodayStartTimeStamp(), System.currentTimeMillis()));
        //本月新增商户数：渠道与商户绑定时间在本月1号0点～当前时间内
        merchantPromotionFeeIncomeVO.setCurrentMonthMerchantNum(buildMerchantNumCount(uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis()));
        //累计商户数：渠道与商户绑定时间<=当前时间
        merchantPromotionFeeIncomeVO.setTotalMerchantNum(buildMerchantNumCount(uid, null, System.currentTimeMillis()));
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
        
        // 如果是默认首页，则type为4  因为员工的收入也包含在商户中 因此如果查全部，也就是查询的商户的
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
        }
        
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
    public R statisticMerchantIncome(Integer type, Long uid, Long beginTime, Long endTime) {
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        // 如果是默认首页，则type为4  因为员工的收入也包含在商户中 因此如果查全部，也就是查询的商户的
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
        }
        
        List<PromotionFeeStatisticAnalysisIncomeVO> incomeVOList = new ArrayList<>();
        
        Long startTime = beginTime;
        while (startTime < endTime) {
            PromotionFeeStatisticAnalysisIncomeVO incomeVO = new PromotionFeeStatisticAnalysisIncomeVO();
            BigDecimal totalIncome = buildPromotionFeeTotalIncomeVO(type, uid, startTime);
            incomeVO.setTotalIncome(totalIncome);
            incomeVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            incomeVOList.add(incomeVO);
            startTime = startTime + (60 * 60 * 1000 * 24);
        }
        
        return R.ok(incomeVOList);
    }
    
    @Override
    public R statisticUser(Integer type, Long uid, Long beginTime, Long endTime) {
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        PromotionFeeStatisticAnalysisUserVO userVO = new PromotionFeeStatisticAnalysisUserVO();
        List<PromotionFeeStatisticAnalysisUserScanCodeVO> scanCodeVOList = new ArrayList<>();
        
        List<PromotionFeeStatisticAnalysisPurchaseVO> purchaseVOList = new ArrayList<>();
        
        List<PromotionFeeStatisticAnalysisRenewalVO> renewalVOList = new ArrayList<>();
        
        Long startTime = beginTime;
        while (startTime < endTime) {
            // 扫码人数
            PromotionFeeStatisticAnalysisUserScanCodeVO scanCodeVO = new PromotionFeeStatisticAnalysisUserScanCodeVO();
            Integer scanCodeNum = buildScanCodeCount(type, uid, startTime, DateUtils.getDayEndTimeStampByDate(startTime), null);
            scanCodeVO.setScanCodeNum(scanCodeNum);
            scanCodeVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            scanCodeVOList.add(scanCodeVO);
            
            // 新增人数
            PromotionFeeStatisticAnalysisPurchaseVO purchaseVO = new PromotionFeeStatisticAnalysisPurchaseVO();
            Integer purchaseNum = buildScanCodeCount(type, uid, startTime, DateUtils.getDayEndTimeStampByDate(startTime), MerchantJoinRecordConstant.STATUS_SUCCESS);
            purchaseVO.setPurchaseNum(purchaseNum);
            purchaseVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            purchaseVOList.add(purchaseVO);
            
            // 续费人数
            PromotionFeeStatisticAnalysisRenewalVO renewalVO = new PromotionFeeStatisticAnalysisRenewalVO();
            Integer renewalNum = buildRenewalNum(type, uid, startTime, DateUtils.getDayEndTimeStampByDate(startTime));
            renewalVO.setRenewalNum(renewalNum);
            renewalVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            renewalVOList.add(renewalVO);
            
            startTime = startTime + (60 * 60 * 1000 * 24);
        }
        
        userVO.setPurchaseVOList(purchaseVOList);
        userVO.setScanCodeVOList(scanCodeVOList);
        userVO.setRenewalVOList(renewalVOList);
        return R.ok(userVO);
    }
    
    @Override
    public R statisticChannelEmployeeMerchant(Integer type, Long uid, Long beginTime, Long endTime) {
        
        //判斷是否是渠道员
        if (!Objects.equals(type, PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode())) {
            return R.fail("300851", "不合法的用户类型");
        }
        List<PromotionFeeStatisticAnalysisMerchantVO> incomeVOList = new ArrayList<>();
        
        Long startTime = beginTime;
        while (startTime < endTime) {
            PromotionFeeStatisticAnalysisMerchantVO merchantVO = new PromotionFeeStatisticAnalysisMerchantVO();
            merchantVO.setMerchantNum(buildMerchantNumCount(uid, startTime, endTime));
            merchantVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            incomeVOList.add(merchantVO);
            startTime = startTime + (60 * 60 * 1000 * 24);
        }
        return R.ok(incomeVOList);
    }
    
    @Override
    public R selectPromotionMerchantDetail(MerchantPromotionEmployeeDetailQueryModel queryModel) {
        //校验用户是否是商户
        Merchant merchant = merchantService.queryByUid(queryModel.getUid());
        if (Objects.isNull(merchant)) {
            log.error("find merchant user error, not found merchant user, uid = {}", queryModel.getUid());
            return R.fail("120007", "未找到商户");
        }
        
        MerchantPromotionMerchantDetailVO merchantDetailVO = new MerchantPromotionMerchantDetailVO();
        merchantDetailVO.setMerchantName(merchant.getName());
        merchantDetailVO.setUid(merchant.getUid());
        
        // 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算；
        MerchantPromotionFeeQueryModel incomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)
                .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(merchant.getUid()).tenantId(TenantContextHolder.getTenantId())
                .rebateStartTime(DateUtils.getTodayStartTimeStamp()).rebateEndTime(System.currentTimeMillis()).build();
        BigDecimal todayInCome = rebateRecordService.sumByStatus(incomeQueryModel);
        merchantDetailVO.setTodayIncome(todayInCome);
        
        // 本月预估收入：“结算日期” = 本月，“结算状态” = 未结算；
        incomeQueryModel.setRebateStartTime(DateUtils.getDayOfMonthStartTime(1));
        BigDecimal currentMonthInCome = rebateRecordService.sumByStatus(incomeQueryModel);
        merchantDetailVO.setCurrentMonthIncome(currentMonthInCome);
        
        // 累计收入：“结算日期” = 当前时间，“结算状态” = 未结算；
        incomeQueryModel.setRebateStartTime(null);
        BigDecimal totalInCome = rebateRecordService.sumByStatus(incomeQueryModel);
        merchantDetailVO.setTotalIncome(totalInCome);
        return R.ok(merchantDetailVO);
    }
    
    @Override
    public R selectMerchantEmployeeDetailList(MerchantPromotionEmployeeDetailQueryModel queryModel) {
        List<MerchantPromotionEmployeeDetailVO> result = new ArrayList<>();
        
        // 如果是默认首页，则type为4  因为员工的收入也包含在商户中 因此如果查全部，也就是查询的商户的
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT.getCode(), queryModel.getType()) || Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(),
                queryModel.getType())) {
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(queryModel);
            if (CollectionUtils.isNotEmpty(merchantEmployees)) {
                result = merchantEmployees.stream().map(merchantEmployee -> buildMerchantPromotionEmployeeDetailVO(merchantEmployee.getUid(), merchantEmployee.getPlaceId()))
                        .collect(Collectors.toList());
            }
        }
        
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode(), queryModel.getType())) {
            MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeService.queryMerchantEmployeeByUid(queryModel.getUid());
            if (Objects.nonNull(merchantEmployeeVO)) {
                result.add(buildMerchantPromotionEmployeeDetailVO(merchantEmployeeVO.getUid(), merchantEmployeeVO.getPlaceId()));
            }
        }
        
        return R.ok(result);
    }
    
    private MerchantPromotionEmployeeDetailVO buildMerchantPromotionEmployeeDetailVO(Long uid, Long placeId) {
        MerchantPromotionEmployeeDetailVO employeeDetailVO = new MerchantPromotionEmployeeDetailVO();
        User user = userService.queryByUidFromCache(uid);
        if (Objects.nonNull(user)) {
            employeeDetailVO.setEmployeeName(user.getName());
            employeeDetailVO.setUid(user.getUid());
            
            // 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算；
            MerchantPromotionFeeQueryModel incomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)
                    .type(PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode()).uid(user.getUid()).tenantId(TenantContextHolder.getTenantId())
                    .rebateStartTime(DateUtils.getTodayStartTimeStamp()).rebateEndTime(System.currentTimeMillis()).build();
            BigDecimal todayInCome = rebateRecordService.sumByStatus(incomeQueryModel);
            employeeDetailVO.setTodayIncome(todayInCome);
            
            // 本月预估收入：“结算日期” = 本月，“结算状态” = 未结算；
            incomeQueryModel.setRebateStartTime(DateUtils.getDayOfMonthStartTime(1));
            BigDecimal currentMonthInCome = rebateRecordService.sumByStatus(incomeQueryModel);
            employeeDetailVO.setCurrentMonthIncome(currentMonthInCome);
            
            // 累计收入：“结算日期” = 当前时间，“结算状态” = 未结算；
            incomeQueryModel.setRebateStartTime(null);
            BigDecimal totalInCome = rebateRecordService.sumByStatus(incomeQueryModel);
            employeeDetailVO.setTotalIncome(totalInCome);
            
            if (Objects.nonNull(placeId)) {
                MerchantPlace place = merchantPlaceService.queryByIdFromCache(placeId);
                employeeDetailVO.setPlaceName(place.getName());
            }
        }
        return employeeDetailVO;
    }
    
    
    @Override
    public R selectPromotionDataDetail(MerchantPromotionDataDetailQueryModel queryModel) {
        
        List<MerchantPromotionDataDetailVO> dataDetailVOList;
        List<MerchantJoinRecord> merchantJoinRecords = Lists.newArrayList();
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), queryModel.getType())) {
            Merchant merchant = merchantService.queryByUid(queryModel.getUid());
            if (Objects.nonNull(merchant)) {
                MerchantAllPromotionDataDetailQueryModel allPromotionDataDetailQueryModel = MerchantAllPromotionDataDetailQueryModel.builder().merchantId(merchant.getId())
                        .size(queryModel.getSize()).offset(queryModel.getOffset()).tenantId(TenantContextHolder.getTenantId()).startTime(queryModel.getStartTime())
                        .endTime(queryModel.getEndTime()).status(queryModel.getStatus()).build();
                
                merchantJoinRecords = merchantJoinRecordService.selectListAllPromotionDataDetail(allPromotionDataDetailQueryModel);
            }
        } else {
            merchantJoinRecords = merchantJoinRecordService.selectPromotionDataDetail(queryModel);
        }
        
        if (CollectionUtils.isEmpty(merchantJoinRecords)) {
            return R.ok();
        }
        
        dataDetailVOList = merchantJoinRecords.parallelStream().map(merchantJoinRecord -> {
            MerchantPromotionDataDetailVO vo = new MerchantPromotionDataDetailVO();
            UserInfo userInfo = userInfoService.queryByUidFromCache(merchantJoinRecord.getJoinUid());
            log.info("dataDetailVO user={}", JsonUtil.toJson(userInfo));
            if (Objects.nonNull(userInfo)) {
                vo.setUid(userInfo.getUid());
                // 对手机号中间四位脱敏
                vo.setPhone(PhoneUtils.mobileEncrypt(userInfo.getPhone()));
                vo.setUserName(userInfo.getName());
            }
            vo.setScanCodeTime(merchantJoinRecord.getStartTime());
            vo.setStatus(merchantJoinRecord.getStatus());
            log.info("dataDetailVO dataDetailVO={}", JsonUtil.toJson(vo));
            return vo;
        }).collect(Collectors.toList());
        
        log.info("dataDetailVOList={}", JsonUtil.toJson(dataDetailVOList));
        
        return R.ok(dataDetailVOList);
    }
    
    @Override
    public R selectPromotionData(MerchantPromotionDataDetailQueryModel queryModel) {
        MerchantPromotionDataVO dataVO = new MerchantPromotionDataVO();
        log.info("selectPromotionData queryModel={}", JsonUtil.toJson(queryModel));
        dataVO.setScanCodeCount(buildScanCodeCount(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime(), null));
        dataVO.setPurchaseCount(
                buildScanCodeCount(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime(), MerchantJoinRecordConstant.STATUS_SUCCESS));
        dataVO.setRenewalCount(buildRenewalNum(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime()));
        dataVO.setTotalIncome(buildPromotionFeeTotalIncomeVO(queryModel.getType(), queryModel.getUid(), queryModel.getEndTime()));
        return R.ok(dataVO);
    }
    
    @Override
    public R selectPromotionEmployeeDetailList(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel) {
        return R.ok(rebateRecordService.selectListPromotionDetail(queryModel));
    }
    
    private Integer buildMerchantNumCount(Long uid, Long startTime, Long endTime) {
        MerchantPromotionFeeMerchantNumQueryModel todayQueryModel = MerchantPromotionFeeMerchantNumQueryModel.builder().uid(uid).tenantId(TenantContextHolder.getTenantId())
                .startTime(startTime).endTime(endTime).build();
        //今日新增商户数：渠道与商户绑定时间在今日0点～当前时间内
        return merchantService.countMerchantNumByTime(todayQueryModel);
    }
    
    private BigDecimal buildPromotionFeeTotalIncomeVO(Integer type, Long uid, Long endTime) {
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
        }
        //累计收入：“结算日期” <= 今日，“结算状态” = 已结算 - 已退回；
        MerchantPromotionFeeQueryModel allSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleEndTime(endTime).build();
        BigDecimal allSettleIncome = rebateRecordService.sumByStatus(allSettleIncomeQueryModel);
        
        MerchantPromotionFeeQueryModel allReturnIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleEndTime(endTime).build();
        BigDecimal allReturnIncome = rebateRecordService.sumByStatus(allReturnIncomeQueryModel);
        return allSettleIncome.subtract(allReturnIncome);
    }
    
    
    private Integer buildScanCodeCount(Integer type, Long uid, Long startTime, Long endTime, Integer status) {
        //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            
            // 商户扫码人数
            MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).inviterUid(uid).startTime(startTime).status(status).endTime(endTime).build();
            Integer scanCodeByMerchant = merchantJoinRecordService.countByCondition(scanCodeQueryModel);
            
            MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            
            // 员工扫码人数
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
            if (CollectionUtils.isNotEmpty(merchantEmployees)) {
                List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                Integer scanCodeByEmployee = merchantJoinRecordService.countEmployeeScanCodeNum(employeeIds, startTime, endTime, status, TenantContextHolder.getTenantId());
                return scanCodeByMerchant + scanCodeByEmployee;
            } else {
                return scanCodeByMerchant;
            }
        } else {
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                    .inviterUid(uid).startTime(startTime).status(status).endTime(endTime).build();
            return merchantJoinRecordService.countByCondition(scanCodeQueryModel);
        }
    }
    
    private Integer buildRenewalNum(Integer type, Long uid, Long startTime, Long endTime) {
        //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
        if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
        }
        MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type).uid(uid)
                .startTime(startTime).endTime(endTime).type(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        return rebateRecordService.countByTime(renewalQueryModel);
    }
    
    
    private void buildPromotionFeePromotionRenewal(Integer type, Long uid, MerchantPromotionFeeRenewalVO merchantPromotionFeeRenewalVO, long dayOfMonthStartTime,
            long dayOfMonthEndTime) {
        
        //今日续费次数：购买指定套餐时间=今日0点～当前时间，且套餐购买次数>1的购买成功次数
        merchantPromotionFeeRenewalVO.setTodayRenewalCount(buildRenewalNum(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis()));
        //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
        merchantPromotionFeeRenewalVO.setYesterdayRenewalCount(buildRenewalNum(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1)));
        //本月续费次数：购买指定套餐时间=本月1号0点～当前时间，且套餐购买次数>1的购买成功次数
        merchantPromotionFeeRenewalVO.setCurrentMonthRenewalCount(buildRenewalNum(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis()));
        //上月续费次数：购买指定套餐时间=上月1号0点～本月1号0点，且套餐购买次数>1的购买成功次数
        merchantPromotionFeeRenewalVO.setLastMonthRenewalCount(buildRenewalNum(type, uid, dayOfMonthStartTime, dayOfMonthEndTime));
        
        //累计续费次数：购买指定套餐时间<=当前时间，且套餐购买次数>1的购买成功次数
        MerchantPromotionRenewalQueryModel totalRenewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type).uid(uid)
                .endTime(System.currentTimeMillis()).status(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).build();
        Integer totalRenewalCount = rebateRecordService.countByTime(totalRenewalQueryModel);
        merchantPromotionFeeRenewalVO.setTotalRenewalCount(totalRenewalCount);
    }
    
    private void buildPromotionFeePromotionScanCode(Integer type, Long uid, MerchantPromotionFeeScanCodeVO merchantPromotionFeeScanCodeVO, long dayOfMonthStartTime,
            long dayOfMonthEndTime) {
        //今日扫码人数：扫码绑定时间=今日0点～当前时间；
        merchantPromotionFeeScanCodeVO.setTodayScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), null));
        //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
        merchantPromotionFeeScanCodeVO.setYesterdayScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), null));
        //本月扫码人数：扫码绑定时间=本月1号0点～当前时间
        merchantPromotionFeeScanCodeVO.setCurrentMonthScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), null));
        //上月扫码人数：扫码绑定时间=上月1号0点～本月1号0点
        merchantPromotionFeeScanCodeVO.setLastMonthScanCodeNum(buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, null));
        
        //累计扫码人数：扫码绑定时间<=当前时间
        merchantPromotionFeeScanCodeVO.setTotalScanCodeNum(buildScanCodeCount(type, uid, null, System.currentTimeMillis(), null));
        
        //今日成功人数：首次成功购买指定套餐时间=今日0点～当前时间，邀请状态=邀请成功
        merchantPromotionFeeScanCodeVO.setTodayPurchaseNum(
                buildScanCodeCount(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS));
        //昨日成功人数：首次成功购买指定套餐时间=昨日0点～今日0点，邀请状态=邀请成功
        merchantPromotionFeeScanCodeVO.setYesterdayPurchaseNum(
                buildScanCodeCount(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), MerchantJoinRecordConstant.STATUS_SUCCESS));
        //本月成功人数：首次成功购买指定套餐时间=本月1号0点～当前时间，邀请状态=邀请成功
        merchantPromotionFeeScanCodeVO.setCurrentMonthPurchaseNum(
                buildScanCodeCount(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS));
        //上月成功人数：首次成功购买指定套餐时间=上月1号0点～本月1号0点，邀请状态=邀请成功
        merchantPromotionFeeScanCodeVO.setLastMonthPurchaseNum(buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, MerchantJoinRecordConstant.STATUS_SUCCESS));
        
        //累计成功人数：首次成功购买指定套餐时间<=当前时间，邀请状态=邀请成功
        merchantPromotionFeeScanCodeVO.setTotalPurchaseNum(buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, MerchantJoinRecordConstant.STATUS_SUCCESS));
    
        merchantPromotionFeeScanCodeVO.setLastMonthPurchaseNum(
                buildScanCodeCount(type, uid, dayOfMonthStartTime, DateUtils.getDayOfMonthStartTime(1), MerchantJoinRecordConstant.STATUS_SUCCESS));
        
        //累计成功人数：首次成功购买指定套餐时间<=当前时间，邀请状态=邀请成功
        merchantPromotionFeeScanCodeVO.setTotalPurchaseNum(buildScanCodeCount(type, uid, null, System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS));
        
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
        
  /*      //累计收入：“结算日期” <= 今日，“结算状态” = 已结算 - 已退回；
        MerchantPromotionFeeQueryModel allSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleEndTime(DateUtils.getTodayEndTimeStamp()).build();
        BigDecimal allSettleIncome = rebateRecordService.sumByStatus(allSettleIncomeQueryModel);
        
        MerchantPromotionFeeQueryModel allReturnIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(type)
                .uid(uid).tenantId(TenantContextHolder.getTenantId()).settleEndTime(DateUtils.getTodayEndTimeStamp()).build();
        BigDecimal allReturnIncome = rebateRecordService.sumByStatus(allReturnIncomeQueryModel);
        merchantPromotionFeeIncomeVO.setLastMonthIncome(allSettleIncome.subtract(allReturnIncome));*/
        merchantPromotionFeeIncomeVO.setTotalIncome(buildPromotionFeeTotalIncomeVO(type, uid, dayOfMonthEndTime));
        
    }
}
