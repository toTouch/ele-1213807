package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.api.client.util.Lists;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantChannelEmployeeBindHistoryConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.dto.merchant.MerchantChannelEmployeeBindHistoryDTO;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantChannelEmployeeBindHistory;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
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
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantChannelEmployeeBindHistoryService;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantPromotionFeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionDataDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionEmployeeDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeIncomeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeMerchantVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeRenewalVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeScanCodeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMerchantDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisIncomeVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisMerchantVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisPurchaseVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisRenewalVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisUserScanCodeVO;
import com.xiliulou.electricity.vo.merchant.PromotionFeeStatisticAnalysisUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private MerchantUserAmountService merchantUserAmountService;
    
    @Resource
    private ChannelEmployeeService channelEmployeeService;
    
    @Resource
    private MerchantChannelEmployeeBindHistoryService merchantChannelEmployeeBindHistoryService;
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("MERCHANT-PROMOTION-USER-STATISTICS-THREAD-POOL", 3, "merchantPromotionUserStatisticsThread:");
    
    
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
        
        if (merchantJoinRecordService.existMerchantInviterData(MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF, merchant.getUid(), TenantContextHolder.getTenantId())) {
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
    public R queryMerchantByChannelEmployeeUid(Long channelEmployeeUid) {
        //校验用户是否是渠道员
        ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(channelEmployeeUid);
        if (Objects.isNull(channelEmployeeVO)) {
            log.error("find merchant user error, not found merchant user, uid = {}", channelEmployeeVO);
            return R.fail("120007", "未找到渠道员");
        }
        
        // 获取渠道员绑定的商户id
        List<MerchantChannelEmployeeBindHistory> merchantChannelEmployeeBindHistories = merchantChannelEmployeeBindHistoryService.selectListByChannelEmployeeUid(
                TenantContextHolder.getTenantId(), channelEmployeeVO.getUid());
        
        if (CollectionUtils.isEmpty(merchantChannelEmployeeBindHistories)) {
            return R.ok();
        }
    
        Set<Long> merchantUidList = merchantChannelEmployeeBindHistories.stream().map(MerchantChannelEmployeeBindHistory::getMerchantUid).collect(Collectors.toSet());
    
        List<Merchant> merchantList = merchantService.queryListByUidList(merchantUidList, TenantContextHolder.getTenantId());
        
        Map<Long, Merchant> merchantMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantList)) {
            merchantMap = merchantList.stream().collect(Collectors.toMap(Merchant::getUid, Function.identity(), (key, key1) -> key1));
        }
        
        // 需要显示有邀请数据的商户
        Map<Long, Merchant> finalMerchantMap = merchantMap;
        List<MerchantPromotionFeeMerchantVO> merchantVOList = merchantChannelEmployeeBindHistories.parallelStream().map(bindHistory -> {
            Long merchantUid = bindHistory.getMerchantUid();
            Merchant merchant = finalMerchantMap.get(merchantUid);
            
            if (ObjectUtils.isNotEmpty(merchant)) {
                MerchantPromotionFeeMerchantVO vo = new MerchantPromotionFeeMerchantVO();
                vo.setUserName(merchant.getName());
                vo.setUid(merchant.getUid());
                vo.setType(PromotionFeeQueryTypeEnum.MERCHANT.getCode());
                return vo;
            }
            
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        
        return R.ok(merchantVOList);
    }
    
    @Override
    public R queryMerchantAvailableWithdrawAmount(Long uid) {
        //校验用户是否是商户
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        BigDecimal result = new BigDecimal(0);
        MerchantUserAmount merchantUserAmount = merchantUserAmountService.queryByUid(uid);
        if (Objects.nonNull(merchantUserAmount)) {
            result = merchantUserAmount.getBalance();
        }
        
        return R.ok(result);
    }
    
    @Override
    public R queryMerchantPromotionFeeIncome(Integer type, Long uid, Integer userType) {
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
        
        buildPromotionFeeIncomeVO(type, uid, merchantPromotionFeeIncomeVO, dayOfMonthStartTime, dayOfMonthEndTime, userType);
        
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
    public R queryMerchantPromotionScanCode(Integer type, Long uid, Integer userType) {
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
        
        buildPromotionFeePromotionScanCode(type, uid, merchantPromotionFeeScanCodeVO, dayOfMonthStartTime, dayOfMonthEndTime, userType);
        
        return R.ok(merchantPromotionFeeScanCodeVO);
    }
    
    @Override
    public R queryMerchantPromotionRenewal(Integer type, Long uid, Integer userType) {
        
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
        
        buildPromotionFeePromotionRenewal(type, uid, merchantPromotionFeeRenewalVO, dayOfMonthStartTime, dayOfMonthEndTime, userType);
        return R.ok(merchantPromotionFeeRenewalVO);
    }
    
    @Override
    public R statisticMerchantIncome(Integer type, Long uid, Long beginTime, Long endTime, Integer userType) {
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
            BigDecimal totalIncome = buildPromotionFeeTotalIncomeVO(type, uid, null, DateUtils.getEndOfDayTimestamp(startTime), userType, true);
            incomeVO.setTotalIncome(totalIncome);
            incomeVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            incomeVOList.add(incomeVO);
            startTime = startTime + (60 * 60 * 1000 * 24);
        }
        
        return R.ok(incomeVOList);
    }
    
    @Override
    public R statisticUser(Integer type, Long uid, Long beginTime, Long endTime, Integer userType) {
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        PromotionFeeStatisticAnalysisUserVO userVO = new PromotionFeeStatisticAnalysisUserVO();
        List<PromotionFeeStatisticAnalysisUserScanCodeVO> scanCodeVOList = new ArrayList<>();
        
        List<PromotionFeeStatisticAnalysisPurchaseVO> purchaseVOList = new ArrayList<>();
        
        List<PromotionFeeStatisticAnalysisRenewalVO> renewalVOList = new ArrayList<>();
        
        // 渠道员登录并且查询的是商户数据
        List<MerchantChannelEmployeeBindHistoryDTO> dtoList = null;
        
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            dtoList = new ArrayList<>();
        }
        
        Long startTime = beginTime;
        while (startTime < endTime) {
            int scanCodeNum = 0;
            int purchaseNum = 0;
            int renewalNum = 0;
            long endOfDayTimestamp = DateUtils.getEndOfDayTimestamp(startTime);
            
            if (Objects.nonNull(dtoList)) {
                // 渠道员推广费带商户条件搜索
                scanCodeNum = buildScanCodeCount(type, uid, startTime, endOfDayTimestamp, null, dtoList);
                purchaseNum = buildScanCodeCount(type, uid, startTime, endOfDayTimestamp, MerchantJoinRecordConstant.STATUS_SUCCESS, dtoList);
                renewalNum = buildRenewalNum(type, uid, startTime, endOfDayTimestamp, dtoList);
              
            } else {
                scanCodeNum = buildScanCodeCount(type, uid, startTime, endOfDayTimestamp, null, null);
                purchaseNum = buildScanCodeCount(type, uid, startTime, endOfDayTimestamp, MerchantJoinRecordConstant.STATUS_SUCCESS, null);
                renewalNum = buildRenewalNum(type, uid, startTime, endOfDayTimestamp, null);
            }
            
            // 扫码人数
            PromotionFeeStatisticAnalysisUserScanCodeVO scanCodeVO = new PromotionFeeStatisticAnalysisUserScanCodeVO();
            scanCodeVO.setScanCodeNum(scanCodeNum);
            scanCodeVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            scanCodeVOList.add(scanCodeVO);
            
            // 新增人数
            PromotionFeeStatisticAnalysisPurchaseVO purchaseVO = new PromotionFeeStatisticAnalysisPurchaseVO();
            purchaseVO.setPurchaseNum(purchaseNum);
            purchaseVO.setStatisticTime(DateUtils.getYearAndMonthAndDayByTimeStamps(startTime));
            purchaseVOList.add(purchaseVO);
            
            // 续费人数
            PromotionFeeStatisticAnalysisRenewalVO renewalVO = new PromotionFeeStatisticAnalysisRenewalVO();
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
    
    public R statisticUserV2(Integer type, Long uid, Long beginTime, Long endTime, Integer userType) {
        if (!PromotionFeeQueryTypeEnum.contains(type)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        // 渠道员登录并且查询的是商户数据
        boolean channelEmployeeFlag = false;
        
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            channelEmployeeFlag = true;
        }
        
        boolean finalChannelEmployeeFlag = channelEmployeeFlag;
    
        // 查询扫码人数
        List<MerchantStatisticsUserVO> merchantStatisticsUserVOS = new ArrayList<>();
        CompletableFuture<List<MerchantStatisticsUserVO>> merchantStatisticsUserInfo = CompletableFuture.supplyAsync(
                () -> queryMerchantJoinRecord(type, uid, beginTime, endTime, null, finalChannelEmployeeFlag), threadPool).whenComplete((result, e) -> {
            if (ObjectUtils.isNotEmpty(result)) {
                merchantStatisticsUserVOS.addAll(result);
            }
        
            if (e != null) {
                log.error("MERCHANT PROMOTION FEE QUERY ERROR!, merchant statistics error", e);
            }
        });
    
        // 查询新增人数
        List<MerchantStatisticsUserVO> merchantAddStatisticsUserVOS = new ArrayList<>();
        CompletableFuture<List<MerchantStatisticsUserVO>> merchantAddStatisticsUserInfo = CompletableFuture.supplyAsync(
                () -> queryMerchantJoinRecord(type, uid, beginTime, endTime, MerchantJoinRecordConstant.STATUS_SUCCESS, finalChannelEmployeeFlag), threadPool).whenComplete((result, e) -> {
            if (ObjectUtils.isNotEmpty(result)) {
                merchantAddStatisticsUserVOS.addAll(result);
            }
        
            if (e != null) {
                log.error("MERCHANT PROMOTION FEE QUERY ERROR!, merchant add statistics error", e);
            }
        });
        
        // 查询续费人数
        List<MerchantStatisticsUserVO> merchantRenewalStatisticsUserVOS = new ArrayList<>();
        CompletableFuture<List<MerchantStatisticsUserVO>> merchantRenewalStatisticsUserInfo = CompletableFuture.supplyAsync(
                () -> queryRenewalNum(type, uid, beginTime, endTime, finalChannelEmployeeFlag), threadPool).whenComplete((result, e) -> {
            if (ObjectUtils.isNotEmpty(result)) {
                merchantAddStatisticsUserVOS.addAll(result);
            }
        
            if (e != null) {
                log.error("MERCHANT PROMOTION FEE QUERY ERROR!, merchant renewal statistics error", e);
            }
        });
    
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(merchantStatisticsUserInfo, merchantAddStatisticsUserInfo, merchantRenewalStatisticsUserInfo);
        
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("merchant user statistics query summary browsing error for merchant query", e);
        }
    
        return dealStaticUserResult(beginTime, endTime, merchantRenewalStatisticsUserVOS, merchantAddStatisticsUserVOS, merchantStatisticsUserVOS);
        
    }
    
    private R dealStaticUserResult(Long beginTime, Long endTime, List<MerchantStatisticsUserVO> merchantRenewalStatisticsUserVOS, List<MerchantStatisticsUserVO> merchantAddStatisticsUserVOS, List<MerchantStatisticsUserVO> merchantStatisticsUserVOS) {
        PromotionFeeStatisticAnalysisUserVO userVO = new PromotionFeeStatisticAnalysisUserVO();
        List<PromotionFeeStatisticAnalysisUserScanCodeVO> scanCodeVOList = new ArrayList<>();
    
        List<PromotionFeeStatisticAnalysisPurchaseVO> purchaseVOList = new ArrayList<>();
    
        List<PromotionFeeStatisticAnalysisRenewalVO> renewalVOList = new ArrayList<>();
        
        Map<String, Integer> merchantRenewalUserMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantRenewalStatisticsUserVOS)) {
            merchantRenewalUserMap = merchantRenewalStatisticsUserVOS.stream().filter(item -> Objects.nonNull(item.getScanCodeCount()))
                    .collect(Collectors.groupingBy(MerchantStatisticsUserVO::getMonthDate, Collectors.summingInt(MerchantStatisticsUserVO::getScanCodeCount)));
        }
    
        Map<String, Integer> merchantAddUserMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantAddStatisticsUserVOS)) {
            merchantAddUserMap = merchantAddStatisticsUserVOS.stream().filter(item -> Objects.nonNull(item.getScanCodeCount()))
                    .collect(Collectors.groupingBy(MerchantStatisticsUserVO::getMonthDate, Collectors.summingInt(MerchantStatisticsUserVO::getScanCodeCount)));
        }
    
        Map<String, Integer> merchantScanCodeMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantStatisticsUserVOS)) {
            merchantScanCodeMap = merchantStatisticsUserVOS.stream().filter(item -> Objects.nonNull(item.getScanCodeCount()))
                    .collect(Collectors.groupingBy(MerchantStatisticsUserVO::getMonthDate, Collectors.summingInt(MerchantStatisticsUserVO::getScanCodeCount)));
        }
    
        List<DateTime> dateTimes = DateUtil.rangeToList(new Date(beginTime), new Date(endTime), DateField.DAY_OF_MONTH);
        for (DateTime dateTime : dateTimes) {
            String monthDateStr = DateUtil.format(dateTime, DateFormatConstant.MONTH_DAY_DATE_FORMAT);
            
            // 扫码人数
            Integer scanCodeNum = NumberConstant.ZERO;
            if (merchantScanCodeMap.containsKey(monthDateStr) ) {
                scanCodeNum = merchantScanCodeMap.get(monthDateStr);
            }
            
            PromotionFeeStatisticAnalysisUserScanCodeVO scanCodeVO = new PromotionFeeStatisticAnalysisUserScanCodeVO();
            scanCodeVO.setScanCodeNum(scanCodeNum);
            scanCodeVO.setStatisticTime(monthDateStr);
            scanCodeVOList.add(scanCodeVO);
            
            
            // 新增人数
            Integer purchaseNum = NumberConstant.ZERO;
            if (merchantAddUserMap.containsKey(monthDateStr) ) {
                purchaseNum = merchantAddUserMap.get(monthDateStr);
            }
            
            PromotionFeeStatisticAnalysisPurchaseVO purchaseVO = new PromotionFeeStatisticAnalysisPurchaseVO();
            purchaseVO.setPurchaseNum(purchaseNum);
            purchaseVO.setStatisticTime(monthDateStr);
            purchaseVOList.add(purchaseVO);
            
            // 续费人数
            Integer renewalNum = NumberConstant.ZERO;
            if (merchantRenewalUserMap.containsKey(monthDateStr) ) {
                renewalNum = merchantRenewalUserMap.get(monthDateStr);
            }
            
            PromotionFeeStatisticAnalysisRenewalVO renewalVO = new PromotionFeeStatisticAnalysisRenewalVO();
            renewalVO.setRenewalNum(renewalNum);
            renewalVO.setStatisticTime(monthDateStr);
            renewalVOList.add(renewalVO);
        }
    
        userVO.setPurchaseVOList(purchaseVOList);
        userVO.setScanCodeVOList(scanCodeVOList);
        userVO.setRenewalVOList(renewalVOList);
        
        return R.ok(userVO);
    }
    
    private List<MerchantStatisticsUserVO> queryRenewalNum(Integer type, Long uid, Long startTime, Long endTime, boolean channelEmployeeFlag) {
        if (channelEmployeeFlag) {
            //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
            //统计收入
            MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).uid(uid)
                    .userType(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).startTime(startTime).endTime(endTime)
                    .rebateType(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).channelEmployeeUid(SecurityUtils.getUid()).refundFlag(MerchantConstant.REBATE_IS_NOT_REFUND)
                    .build();
            
            return rebateRecordService.listRenewal(renewalQueryModel);
        } else {
            //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
                type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
            }
            
            if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), type)) {
                MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .channelEmployeeUid(uid).startTime(startTime).endTime(endTime).rebateType(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL)
                        .refundFlag(MerchantConstant.REBATE_IS_NOT_REFUND).build();
                return rebateRecordService.listRenewal(renewalQueryModel);
            }
            
            MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).userType(type).uid(uid)
                    .startTime(startTime).endTime(endTime).rebateType(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).refundFlag(MerchantConstant.REBATE_IS_NOT_REFUND).build();
            return rebateRecordService.listRenewal(renewalQueryModel);
        }
    }
    
    private List<MerchantStatisticsUserVO> queryMerchantJoinRecord(Integer type, Long uid, Long startTime, Long endTime, Integer status, boolean channelEmployeeFlag) {
        // 根据是否成功然后修改查询逻辑
        if (Objects.nonNull(status) && Objects.equals(status, MerchantJoinRecordConstant.STATUS_SUCCESS)) {
            return buildMerchantJoinRecordSuccessCount(type, uid, startTime, endTime, status, channelEmployeeFlag);
        }
        
        return buildMerchantJoinRecordCommonCount(type, uid, startTime, endTime, status, channelEmployeeFlag);
    }
    
    private List<MerchantStatisticsUserVO> buildMerchantJoinRecordCommonCount(Integer type, Long uid, Long startTime, Long endTime, Integer status, boolean channelEmployeeFlag) {
        if (channelEmployeeFlag) {
            int result = 0;
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .channelEmployeeUid(SecurityUtils.getUid()).status(status).type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).build();
        
            scanCodeQueryModel.setStartTime(startTime);
            scanCodeQueryModel.setEndTime(endTime);
            List<MerchantStatisticsUserVO> merchantStatisticsUserVOS = merchantJoinRecordService.listJoinNumByCondition(scanCodeQueryModel);
    
    
            MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            // 员工扫码人数
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
            if (ObjectUtils.isEmpty(merchantEmployees)) {
                return merchantStatisticsUserVOS;
            }
    
            List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
            List<MerchantStatisticsUserVO> scanCodeByEmployee = merchantJoinRecordService.listEmployeeJoinNum(employeeIds, startTime, endTime, status,
                    TenantContextHolder.getTenantId(), SecurityUtils.getUid());
            if (ObjectUtils.isEmpty(scanCodeByEmployee)) {
                return merchantStatisticsUserVOS;
            }
    
            merchantStatisticsUserVOS.addAll(scanCodeByEmployee);
            
            return merchantStatisticsUserVOS;
        } else {
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
            
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                List<MerchantStatisticsUserVO> merchantStatisticsUserVOS = merchantJoinRecordService.listJoinNumByCondition(scanCodeQueryModel);
            
                MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                        .tenantId(TenantContextHolder.getTenantId()).build();
            
                // 员工扫码人数
                List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
                if (ObjectUtils.isEmpty(merchantEmployees)) {
                    return merchantStatisticsUserVOS;
                }
    
                List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                List<MerchantStatisticsUserVO> scanCodeByEmployee = merchantJoinRecordService.listEmployeeJoinNum(employeeIds, startTime, endTime, status, TenantContextHolder.getTenantId(),
                        null);
    
                if (ObjectUtils.isEmpty(scanCodeByEmployee)) {
                    return merchantStatisticsUserVOS;
                }
    
                merchantStatisticsUserVOS.addAll(scanCodeByEmployee);
                
                return merchantStatisticsUserVOS;
            } else if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), type)) {
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .channelEmployeeUid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.listJoinNumByCondition(scanCodeQueryModel);
            } else {
                //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                        .uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.listJoinNumByCondition(scanCodeQueryModel);
            }
        }
    }
    
    private List<MerchantStatisticsUserVO> buildMerchantJoinRecordSuccessCount(Integer type, Long uid, Long startTime, Long endTime, Integer status, boolean channelEmployeeFlag) {
        if (channelEmployeeFlag) {
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .channelEmployeeUid(SecurityUtils.getUid()).status(status).type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).build();
            
            scanCodeQueryModel.setStartTime(startTime);
            scanCodeQueryModel.setEndTime(endTime);
            List<MerchantStatisticsUserVO> merchantStatisticsUserVOS = merchantJoinRecordService.listSuccessJoinNumByCondition(scanCodeQueryModel);
            
            MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            
            // 员工扫码人数
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
            if (ObjectUtils.isEmpty(merchantEmployees)) {
                return merchantStatisticsUserVOS;
            }
            
            List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
            List<MerchantStatisticsUserVO> employeeSuccessJoinNum = merchantJoinRecordService.listEmployeeSuccessJoinNum(employeeIds, startTime, endTime, status,
                    TenantContextHolder.getTenantId(), SecurityUtils.getUid());
            
            if (ObjectUtils.isEmpty(employeeSuccessJoinNum)) {
                return merchantStatisticsUserVOS;
            }
            
            merchantStatisticsUserVOS.addAll(employeeSuccessJoinNum);
            
            return merchantStatisticsUserVOS;
        } else {
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
                
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                List<MerchantStatisticsUserVO> merchantStatisticsUserVOS = merchantJoinRecordService.listSuccessJoinNumByCondition(scanCodeQueryModel);
                
                MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                        .tenantId(TenantContextHolder.getTenantId()).build();
                
                // 员工扫码人数
                List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
                if (ObjectUtils.isEmpty(merchantEmployees)) {
                    return merchantStatisticsUserVOS;
                }
    
                List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                List<MerchantStatisticsUserVO> employeeSuccessJoinNum = merchantJoinRecordService.listEmployeeSuccessJoinNum(employeeIds, startTime, endTime, status, TenantContextHolder.getTenantId(),
                        null);
    
                if (ObjectUtils.isEmpty(employeeSuccessJoinNum)) {
                    return merchantStatisticsUserVOS;
                }
    
                merchantStatisticsUserVOS.addAll(employeeSuccessJoinNum);
    
                return merchantStatisticsUserVOS;
            } else if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), type)) {
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .channelEmployeeUid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.listSuccessJoinNumByCondition(scanCodeQueryModel);
            } else {
                //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                        .uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.listSuccessJoinNumByCondition(scanCodeQueryModel);
            }
        }
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
            merchantVO.setMerchantNum(buildMerchantNumCount(uid, startTime, DateUtils.getEndOfDayTimestamp(startTime)));
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
        
        if (!merchantJoinRecordService.existMerchantInviterData(MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF, merchant.getUid(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        MerchantPromotionMerchantDetailVO merchantDetailVO = new MerchantPromotionMerchantDetailVO();
        merchantDetailVO.setMerchantName(merchant.getName());
        merchantDetailVO.setUid(merchant.getUid());
        
        // 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算-已退回（今日发生的退款）；
        MerchantPromotionFeeQueryModel todayIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)
                .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(merchant.getUid()).tenantId(TenantContextHolder.getTenantId())
                .rebateStartTime(DateUtils.getTodayStartTimeStamp()).rebateEndTime(System.currentTimeMillis()).build();
        BigDecimal todayInCome = rebateRecordService.sumByStatus(todayIncomeQueryModel);
        
        todayIncomeQueryModel.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
        BigDecimal todayReturnInCome = rebateRecordService.sumByStatus(todayIncomeQueryModel);
        merchantDetailVO.setTodayIncome(todayInCome.subtract(todayReturnInCome));
        
        // 本月预估收入：本月1号0点～当前时间，“结算状态” = 未结算+已结算-已退回；
        merchantDetailVO.setCurrentMonthIncome(getCurrentMonthIncome(queryModel.getUid(), PromotionFeeQueryTypeEnum.MERCHANT.getCode(), null));
        
        // 累计收入：“结算日期” = 当前时间，“结算状态” = 未结算；
        merchantDetailVO.setTotalIncome(
                buildPromotionFeeTotalIncomeVO(PromotionFeeQueryTypeEnum.MERCHANT.getCode(), queryModel.getUid(), null, System.currentTimeMillis(), null, false));
        
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
                    .type(PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode()).uid(uid).tenantId(TenantContextHolder.getTenantId())
                    .rebateStartTime(DateUtils.getTodayStartTimeStamp()).rebateEndTime(System.currentTimeMillis()).build();
            BigDecimal todayInCome = rebateRecordService.sumByStatus(incomeQueryModel);
            employeeDetailVO.setTodayIncome(todayInCome);
            
            // 本月预估收入：本月1号0点～当前时间，“结算状态” = 未结算+已结算-已退回；
            employeeDetailVO.setCurrentMonthIncome(getCurrentMonthIncome(uid, PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode(), null));
            
            // 累计收入：“结算日期” = 当前时间，“结算状态” = 未结算；
            employeeDetailVO.setTotalIncome(
                    buildPromotionFeeTotalIncomeVO(PromotionFeeQueryTypeEnum.MERCHANT_EMPLOYEE.getCode(), uid, null, System.currentTimeMillis(), null, false));
            
            if (Objects.nonNull(placeId)) {
                MerchantPlace place = merchantPlaceService.queryByIdFromCache(placeId);
                if (Objects.nonNull(place)) {
                    employeeDetailVO.setPlaceName(place.getName());
                }
            }
        }
        return employeeDetailVO;
    }
    
    private BigDecimal getCurrentMonthIncome(Long uid, Integer type, Integer userType) {
        // 渠道员登录并且查询的是商户数据
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            long dayOfMonthStartTime = DateUtils.getDayOfMonthStartTime(1);
            long currentTimeMillis = System.currentTimeMillis();
            
            //统计收入未结算 已退回
            BigDecimal resultNoSettleAmount = new BigDecimal(0);
            //统计已结算收入
            BigDecimal resultSettleAmount = new BigDecimal(0);
            
            MerchantPromotionFeeQueryModel monthIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)
                    .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                    .rebateStartTime(dayOfMonthStartTime).rebateEndTime(currentTimeMillis).build();
            BigDecimal currentMonthNoSettleInCome = rebateRecordService.sumByStatus(monthIncomeQueryModel);
            
            monthIncomeQueryModel.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
            // 本月未结算的数据
            BigDecimal currentMonthReturnInCome = rebateRecordService.sumByStatus(monthIncomeQueryModel);
    
            resultNoSettleAmount = currentMonthNoSettleInCome.subtract(currentMonthReturnInCome);
            
            MerchantPromotionFeeQueryModel monthSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                    .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                    .rebateStartTime(dayOfMonthStartTime).rebateEndTime(currentTimeMillis).build();
            // 本月已结算的数据
            resultSettleAmount = rebateRecordService.sumByStatus(monthSettleIncomeQueryModel);
    
            BigDecimal resultAmount = resultNoSettleAmount.add(resultSettleAmount);
            
            return resultAmount;
            
        } else {
            MerchantPromotionFeeQueryModel monthIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getDayOfMonthStartTime(1)).rebateEndTime(System.currentTimeMillis()).build();
            BigDecimal currentMonthNoSettleInCome = rebateRecordService.sumByStatus(monthIncomeQueryModel);
            
            monthIncomeQueryModel.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
            BigDecimal currentMonthReturnInCome = rebateRecordService.sumByStatus(monthIncomeQueryModel);
            
            MerchantPromotionFeeQueryModel monthSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getDayOfMonthStartTime(1)).rebateEndTime(System.currentTimeMillis()).build();
            BigDecimal currentMonthSettleInCome = rebateRecordService.sumByStatus(monthSettleIncomeQueryModel);
            return currentMonthNoSettleInCome.add(currentMonthSettleInCome).subtract(currentMonthReturnInCome);
        }
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
        
        dataDetailVOList = merchantJoinRecords.stream().map(merchantJoinRecord -> {
            MerchantPromotionDataDetailVO vo = new MerchantPromotionDataDetailVO();
            UserInfo userInfo = userInfoService.queryByUidFromCache(merchantJoinRecord.getJoinUid());
            if (Objects.nonNull(userInfo)) {
                vo.setUid(userInfo.getUid());
                // 对手机号中间四位脱敏
                vo.setPhone(PhoneUtils.mobileEncrypt(userInfo.getPhone()));
                vo.setUserName(userInfo.getName());
            }
            vo.setScanCodeTime(merchantJoinRecord.getStartTime());
            vo.setStatus(merchantJoinRecord.getStatus());
            return vo;
        }).collect(Collectors.toList());
        
        return R.ok(dataDetailVOList);
    }
    
    @Override
    public R selectPromotionData(MerchantPromotionDataDetailQueryModel queryModel) {
        // 如果没有传时间 则为查询截止当前的记录
        if (Objects.isNull(queryModel.getStartTime()) && Objects.isNull(queryModel.getEndTime())) {
            queryModel.setEndTime(System.currentTimeMillis());
        }
        
        MerchantPromotionDataVO dataVO = new MerchantPromotionDataVO();
        dataVO.setScanCodeCount(buildScanCodeCount(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime(), null, null));
        dataVO.setPurchaseCount(
                buildScanCodeCount(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime(), MerchantJoinRecordConstant.STATUS_SUCCESS, null));
        dataVO.setRenewalCount(buildRenewalNum(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime(), null));
        dataVO.setTotalIncome(buildPromotionFeeTotalIncomeVO(queryModel.getType(), queryModel.getUid(), queryModel.getStartTime(), queryModel.getEndTime(), null, false));
        return R.ok(dataVO);
    }
    
    @Override
    public R selectPromotionEmployeeDetailList(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel) {
        return R.ok(rebateRecordService.selectListPromotionDetail(queryModel));
    }
    
    private Integer buildMerchantNumCount(Long uid, Long startTime, Long endTime) {
        MerchantPromotionFeeMerchantNumQueryModel todayQueryModel = MerchantPromotionFeeMerchantNumQueryModel.builder().uid(uid).tenantId(TenantContextHolder.getTenantId())
                .startTime(startTime).endTime(endTime).bindStatus(MerchantChannelEmployeeBindHistoryConstant.BIND).build();
        //今日新增商户数：渠道与商户绑定时间在今日0点～当前时间内
        return merchantChannelEmployeeBindHistoryService.countMerchantNumByTime(todayQueryModel);
    }
    
    private BigDecimal buildPromotionFeeTotalIncomeVO(Integer type, Long uid, Long startTime, Long endTime, Integer userType, boolean isAnalysis) {
        // 渠道员登录并且查询的是商户数据
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            BigDecimal resultAmount = new BigDecimal(0);
            
            // 如果是统计分析，则需要根据每天的时间去查询
            //统计累计收入
            MerchantPromotionFeeQueryModel totalIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                    .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                    .rebateStartTime(startTime)
                    .rebateEndTime(endTime).build();
            BigDecimal totalSettleInCome = rebateRecordService.sumByStatus(totalIncomeQueryModel);
    
            // 已退回需要用返现日期计算
            MerchantPromotionFeeQueryModel totalReturnIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED)
                    .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                    .rebateStartTime(startTime)
                    .rebateEndTime(endTime).build();
    
            BigDecimal totalReturnInCome = rebateRecordService.sumByStatus(totalReturnIncomeQueryModel);
    
            resultAmount = totalSettleInCome.subtract(totalReturnInCome);
            
            return resultAmount;
        } else {
            //累计收入：“结算日期” <= 今日，“结算状态” = 已结算 - 已退回；
            MerchantPromotionFeeQueryModel allSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(startTime).rebateEndTime(endTime).build();
            BigDecimal allSettleIncome = rebateRecordService.sumByStatus(allSettleIncomeQueryModel);
            
            // 已退回需要用返现日期计算
            MerchantPromotionFeeQueryModel allReturnIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(startTime).rebateEndTime(endTime).build();
            BigDecimal allReturnIncome = rebateRecordService.sumByStatus(allReturnIncomeQueryModel);
            return allSettleIncome.subtract(allReturnIncome);
        }
    }
    
    
    private Integer buildScanCodeCount(Integer type, Long uid, Long startTime, Long endTime, Integer status, List<MerchantChannelEmployeeBindHistoryDTO> dtoList) {
        // 根据是否成功然后修改查询逻辑
        if (Objects.nonNull(status) && Objects.equals(status, MerchantJoinRecordConstant.STATUS_SUCCESS)) {
            return buildScanCodeSuccessCount(type, uid, startTime, endTime, status, dtoList);
        }
    
        return buildScanCodeCommonCount(type, uid, startTime, endTime, status, dtoList);
    }
    
    /**
     * 查询扫码人数成功的数量
     * @param type
     * @param uid
     * @param startTime
     * @param endTime
     * @param status
     * @param dtoList
     * @return
     */
    private Integer buildScanCodeSuccessCount(Integer type, Long uid, Long startTime, Long endTime, Integer status, List<MerchantChannelEmployeeBindHistoryDTO> dtoList) {
        if (Objects.nonNull(dtoList)) {
            int result = 0;
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .channelEmployeeUid(SecurityUtils.getUid()).status(status).type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).build();
            
            scanCodeQueryModel.setStartTime(startTime);
            scanCodeQueryModel.setEndTime(endTime);
            Integer scanCodeByMerchant = merchantJoinRecordService.countSuccessByCondition(scanCodeQueryModel);
            
            MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            
            // 员工扫码人数
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
            if (CollectionUtils.isNotEmpty(merchantEmployees)) {
                List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                Integer scanCodeByEmployee = merchantJoinRecordService.countEmployeeScanCodeSuccessNum(employeeIds, startTime, endTime, status,
                        TenantContextHolder.getTenantId(), SecurityUtils.getUid());
                
                result = scanCodeByMerchant + scanCodeByEmployee;
            } else {
                result = scanCodeByMerchant;
            }
            
            return result;
        } else {
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
                
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                Integer scanCodeByMerchant = merchantJoinRecordService.countSuccessByCondition(scanCodeQueryModel);
                
                MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                        .tenantId(TenantContextHolder.getTenantId()).build();
                
                // 员工扫码人数
                List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
                if (CollectionUtils.isNotEmpty(merchantEmployees)) {
                    List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                    Integer scanCodeByEmployee = merchantJoinRecordService.countEmployeeScanCodeSuccessNum(employeeIds, startTime, endTime, status, TenantContextHolder.getTenantId(),
                            null);
                    return scanCodeByMerchant + scanCodeByEmployee;
                } else {
                    return scanCodeByMerchant;
                }
            } else if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), type)) {
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .channelEmployeeUid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.countSuccessByCondition(scanCodeQueryModel);
            } else {
                //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                        .uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.countSuccessByCondition(scanCodeQueryModel);
            }
        }
    }
    
    /**
     * 查询扫码人数通用方法
     * @param type
     * @param uid
     * @param startTime
     * @param endTime
     * @param status
     * @param dtoList
     * @return
     */
    private Integer buildScanCodeCommonCount(Integer type, Long uid, Long startTime, Long endTime, Integer status, List<MerchantChannelEmployeeBindHistoryDTO> dtoList) {
        if (Objects.nonNull(dtoList)) {
            int result = 0;
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .channelEmployeeUid(SecurityUtils.getUid()).status(status).type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).build();
            
            scanCodeQueryModel.setStartTime(startTime);
            scanCodeQueryModel.setEndTime(endTime);
            Integer scanCodeByMerchant = merchantJoinRecordService.countByCondition(scanCodeQueryModel);
            
            MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            
            // 员工扫码人数
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
            if (CollectionUtils.isNotEmpty(merchantEmployees)) {
                List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                Integer scanCodeByEmployee = merchantJoinRecordService.countEmployeeScanCodeNum(employeeIds, startTime, endTime, status,
                        TenantContextHolder.getTenantId(), SecurityUtils.getUid());
                
                result = scanCodeByMerchant + scanCodeByEmployee;
            } else {
                result = scanCodeByMerchant;
            }
            
            return result;
        } else {
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
                
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .type(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                Integer scanCodeByMerchant = merchantJoinRecordService.countByCondition(scanCodeQueryModel);
                
                MerchantPromotionEmployeeDetailQueryModel employeeDetailQueryModel = MerchantPromotionEmployeeDetailQueryModel.builder().uid(uid)
                        .tenantId(TenantContextHolder.getTenantId()).build();
                
                // 员工扫码人数
                List<MerchantEmployee> merchantEmployees = merchantEmployeeService.selectByMerchantUid(employeeDetailQueryModel);
                if (CollectionUtils.isNotEmpty(merchantEmployees)) {
                    List<Long> employeeIds = merchantEmployees.parallelStream().map(MerchantEmployee::getUid).collect(Collectors.toList());
                    Integer scanCodeByEmployee = merchantJoinRecordService.countEmployeeScanCodeNum(employeeIds, startTime, endTime, status, TenantContextHolder.getTenantId(),
                            null);
                    return scanCodeByMerchant + scanCodeByEmployee;
                } else {
                    return scanCodeByMerchant;
                }
            } else if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), type)) {
                // 商户扫码人数
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .channelEmployeeUid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.countByCondition(scanCodeQueryModel);
            } else {
                //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
                MerchantPromotionScanCodeQueryModel scanCodeQueryModel = MerchantPromotionScanCodeQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).type(type)
                        .uid(uid).startTime(startTime).status(status).endTime(endTime).build();
                return merchantJoinRecordService.countByCondition(scanCodeQueryModel);
            }
        }
    }
    
    private Integer buildRenewalNum(Integer type, Long uid, Long startTime, Long endTime, List<MerchantChannelEmployeeBindHistoryDTO> dtoList) {
        if (Objects.nonNull(dtoList)) {
            //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
            //统计收入
            MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).uid(uid)
                    .userType(PromotionFeeQueryTypeEnum.MERCHANT.getCode()).startTime(startTime).endTime(endTime)
                    .rebateType(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).channelEmployeeUid(SecurityUtils.getUid()).refundFlag(MerchantConstant.REBATE_IS_NOT_REFUND)
                    .build();
            
            int result = rebateRecordService.countByTime(renewalQueryModel);
            
            return result;
        } else {
            //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), type)) {
                type = PromotionFeeQueryTypeEnum.MERCHANT.getCode();
            }
            
            if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), type)) {
                MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .channelEmployeeUid(uid).startTime(startTime).endTime(endTime).rebateType(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL)
                        .refundFlag(MerchantConstant.REBATE_IS_NOT_REFUND).build();
                return rebateRecordService.countByTime(renewalQueryModel);
            }
            
            MerchantPromotionRenewalQueryModel renewalQueryModel = MerchantPromotionRenewalQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).userType(type).uid(uid)
                    .startTime(startTime).endTime(endTime).rebateType(MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL).refundFlag(MerchantConstant.REBATE_IS_NOT_REFUND).build();
            return rebateRecordService.countByTime(renewalQueryModel);
        }
    }
    
    
    private void buildPromotionFeePromotionRenewal(Integer type, Long uid, MerchantPromotionFeeRenewalVO merchantPromotionFeeRenewalVO, long dayOfMonthStartTime,
            long dayOfMonthEndTime, Integer userType) {
        // 渠道员登录并且查询的是商户数据
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            List<MerchantChannelEmployeeBindHistoryDTO> list = new ArrayList<>();
            
            //今日续费次数：购买指定套餐时间=今日0点～当前时间，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setTodayRenewalCount(buildRenewalNum(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), list));
            //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setYesterdayRenewalCount(buildRenewalNum(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), list));
            //本月续费次数：购买指定套餐时间=本月1号0点～当前时间，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setCurrentMonthRenewalCount(
                    buildRenewalNum(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), list));
            //上月续费次数：购买指定套餐时间=上月1号0点～本月1号0点，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setLastMonthRenewalCount(buildRenewalNum(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, list));
            
            //累计续费次数：购买指定套餐时间<=当前时间，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setTotalRenewalCount(buildRenewalNum(type, uid, null, System.currentTimeMillis(), list));
        } else {
            //今日续费次数：购买指定套餐时间=今日0点～当前时间，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setTodayRenewalCount(buildRenewalNum(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), null));
            //昨日续费次数：购买指定套餐时间=昨日0点～今日0点，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setYesterdayRenewalCount(buildRenewalNum(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), null));
            //本月续费次数：购买指定套餐时间=本月1号0点～当前时间，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setCurrentMonthRenewalCount(buildRenewalNum(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), null));
            //上月续费次数：购买指定套餐时间=上月1号0点～本月1号0点，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setLastMonthRenewalCount(buildRenewalNum(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, null));
            
            //累计续费次数：购买指定套餐时间<=当前时间，且套餐购买次数>1的购买成功次数
            merchantPromotionFeeRenewalVO.setTotalRenewalCount(buildRenewalNum(type, uid, null, System.currentTimeMillis(), null));
        }
    }
    
    private static List<MerchantChannelEmployeeBindHistoryDTO> buildMerchantChannelEmployeeBindHistoryDTO(List<MerchantChannelEmployeeBindHistory> bindHistoryList, Long startTime,
            Long endTime) {
        if (CollectionUtils.isEmpty(bindHistoryList)) {
            return Lists.newArrayList();
        }
        return bindHistoryList.stream().map(bindHistory -> {
            MerchantChannelEmployeeBindHistoryDTO bindHistoryDTO = new MerchantChannelEmployeeBindHistoryDTO();
            BeanUtils.copyProperties(bindHistory, bindHistoryDTO);
            
            if (Objects.equals(bindHistory.getBindStatus(), MerchantChannelEmployeeBindHistoryConstant.BIND) && bindHistory.getBindTime() >= endTime) {
                return null;
            }
            
            if (Objects.equals(bindHistory.getBindStatus(), MerchantChannelEmployeeBindHistoryConstant.UN_BIND)) {
                if (Objects.nonNull(endTime) && bindHistory.getBindTime() > endTime) {
                    return null;
                }
                if (Objects.nonNull(startTime) && bindHistory.getUnBindTime() < startTime) {
                    return null;
                }
            }
            
            if (bindHistory.getBindTime() >= startTime) {
                bindHistoryDTO.setQueryStartTime(bindHistory.getBindTime());
            } else {
                bindHistoryDTO.setQueryStartTime(startTime);
            }
            if (Objects.nonNull(bindHistory.getUnBindTime()) && bindHistory.getUnBindTime() <= endTime) {
                bindHistoryDTO.setQueryEndTime(bindHistory.getUnBindTime());
            } else {
                bindHistoryDTO.setQueryEndTime(endTime);
            }
            return bindHistoryDTO;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    private static List<MerchantChannelEmployeeBindHistoryDTO> buildStatisticMerchantChannelEmployeeBindHistoryDTO(List<MerchantChannelEmployeeBindHistory> bindHistoryList, Long endTime) {
        if (CollectionUtils.isEmpty(bindHistoryList)) {
            return Lists.newArrayList();
        }
        return bindHistoryList.stream().map(bindHistory -> {
            MerchantChannelEmployeeBindHistoryDTO bindHistoryDTO = new MerchantChannelEmployeeBindHistoryDTO();
            BeanUtils.copyProperties(bindHistory, bindHistoryDTO);
            bindHistoryDTO.setQueryStartTime(bindHistory.getBindTime());
            if (Objects.nonNull(bindHistory.getBindTime()) && bindHistory.getBindTime() >= endTime) {
                return null;
            }
            
            if (Objects.nonNull(bindHistory.getUnBindTime()) && bindHistory.getUnBindTime() <= endTime) {
                bindHistoryDTO.setQueryEndTime(bindHistory.getUnBindTime());
            } else {
                bindHistoryDTO.setQueryEndTime(endTime);
            }
            return bindHistoryDTO;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    private void buildPromotionFeePromotionScanCode(Integer type, Long uid, MerchantPromotionFeeScanCodeVO merchantPromotionFeeScanCodeVO, long dayOfMonthStartTime,
            long dayOfMonthEndTime, Integer userType) {
        
        // 渠道员登录并且查询的是商户数据
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            List<MerchantChannelEmployeeBindHistoryDTO> list = new ArrayList<>();
            
            //今日扫码人数：扫码绑定时间=今日0点～当前时间；
            merchantPromotionFeeScanCodeVO.setTodayScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), null, list));
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            merchantPromotionFeeScanCodeVO.setYesterdayScanCodeNum(
                    buildScanCodeCount(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), null, list));
            //本月扫码人数：扫码绑定时间=本月1号0点～当前时间
            merchantPromotionFeeScanCodeVO.setCurrentMonthScanCodeNum(
                    buildScanCodeCount(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), null, list));
            //上月扫码人数：扫码绑定时间=上月1号0点～本月1号0点
            merchantPromotionFeeScanCodeVO.setLastMonthScanCodeNum(buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, null, list));
            
            //累计扫码人数：扫码绑定时间<=当前时间
            merchantPromotionFeeScanCodeVO.setTotalScanCodeNum(buildScanCodeCount(type, uid, null, System.currentTimeMillis(), null, list));
            
            //今日成功人数：首次成功购买指定套餐时间=今日0点～当前时间，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setTodayPurchaseNum(
                    buildScanCodeCount(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS, list));
            //昨日成功人数：首次成功购买指定套餐时间=昨日0点～今日0点，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setYesterdayPurchaseNum(
                    buildScanCodeCount(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), MerchantJoinRecordConstant.STATUS_SUCCESS, list));
            //本月成功人数：首次成功购买指定套餐时间=本月1号0点～当前时间，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setCurrentMonthPurchaseNum(
                    buildScanCodeCount(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS, list));
            //上月成功人数：首次成功购买指定套餐时间=上月1号0点～本月1号0点，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setLastMonthPurchaseNum(
                    buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, MerchantJoinRecordConstant.STATUS_SUCCESS, list));
            //累计成功人数：首次成功购买指定套餐时间<=当前时间，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setTotalPurchaseNum(
                    buildScanCodeCount(type, uid, null, System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS, list));
            
        } else {
            //今日扫码人数：扫码绑定时间=今日0点～当前时间；
            merchantPromotionFeeScanCodeVO.setTodayScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), null, null));
            //昨日扫码人数：扫码绑定时间=昨日0点～今日0点；
            merchantPromotionFeeScanCodeVO.setYesterdayScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), null, null));
            //本月扫码人数：扫码绑定时间=本月1号0点～当前时间
            merchantPromotionFeeScanCodeVO.setCurrentMonthScanCodeNum(buildScanCodeCount(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), null, null));
            //上月扫码人数：扫码绑定时间=上月1号0点～本月1号0点
            merchantPromotionFeeScanCodeVO.setLastMonthScanCodeNum(buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, null, null));
            
            //累计扫码人数：扫码绑定时间<=当前时间
            merchantPromotionFeeScanCodeVO.setTotalScanCodeNum(buildScanCodeCount(type, uid, null, System.currentTimeMillis(), null, null));
            
            //今日成功人数：首次成功购买指定套餐时间=今日0点～当前时间，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setTodayPurchaseNum(
                    buildScanCodeCount(type, uid, DateUtils.getTodayStartTime(), System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS, null));
            //昨日成功人数：首次成功购买指定套餐时间=昨日0点～今日0点，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setYesterdayPurchaseNum(
                    buildScanCodeCount(type, uid, DateUtils.getTimeAgoStartTime(1), DateUtils.getTimeAgoEndTime(1), MerchantJoinRecordConstant.STATUS_SUCCESS, null));
            //本月成功人数：首次成功购买指定套餐时间=本月1号0点～当前时间，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setCurrentMonthPurchaseNum(
                    buildScanCodeCount(type, uid, DateUtils.getDayOfMonthStartTime(1), System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS, null));
            //上月成功人数：首次成功购买指定套餐时间=上月1号0点～本月1号0点，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setLastMonthPurchaseNum(
                    buildScanCodeCount(type, uid, dayOfMonthStartTime, dayOfMonthEndTime, MerchantJoinRecordConstant.STATUS_SUCCESS, null));
            //累计成功人数：首次成功购买指定套餐时间<=当前时间，邀请状态=邀请成功
            merchantPromotionFeeScanCodeVO.setTotalPurchaseNum(buildScanCodeCount(type, uid, null, System.currentTimeMillis(), MerchantJoinRecordConstant.STATUS_SUCCESS, null));
        }
    }
    
    private void buildPromotionFeeIncomeVO(Integer type, Long uid, MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO, long dayOfMonthStartTime, long dayOfMonthEndTime,
            Integer userType) {
        if (Objects.equals(userType, User.TYPE_USER_CHANNEL) && Objects.equals(type, PromotionFeeQueryTypeEnum.MERCHANT.getCode())) {
            
            handleMerchantTodayIncomeByChannelEmployee(type, uid, merchantPromotionFeeIncomeVO, null, null, null);
        } else {
            // 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算-已退回（今日发生的退款）；
            MerchantPromotionFeeQueryModel todayInComeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getTodayStartTimeStamp()).rebateEndTime(DateUtils.getTodayEndTimeStamp())
                    .build();
            BigDecimal todayInCome = rebateRecordService.sumByStatus(todayInComeQueryModel);
            
            // 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算-已退回（今日发生的退款）；
            todayInComeQueryModel.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
            BigDecimal todayReturnInCome = rebateRecordService.sumByStatus(todayInComeQueryModel);
            merchantPromotionFeeIncomeVO.setTodayIncome(todayInCome.subtract(todayReturnInCome));
            
            // 昨日收入：“结算日期” = 今日，“结算状态” = 已结算 - 已退回（昨日发生的退款）；
            MerchantPromotionFeeQueryModel yesterdaySettleQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getTimeAgoStartTime(1)).rebateEndTime(DateUtils.getTimeAgoEndTime(1))
                    .build();
            BigDecimal todaySettleInCome = rebateRecordService.sumByStatus(yesterdaySettleQueryModel);
            
            MerchantPromotionFeeQueryModel yesterdayReturnQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(type)
                    .uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(DateUtils.getTimeAgoStartTime(1)).rebateEndTime(DateUtils.getTimeAgoEndTime(1)).build();
            BigDecimal yesterdayReturnInCome = rebateRecordService.sumByStatus(yesterdayReturnQueryModel);
            merchantPromotionFeeIncomeVO.setYesterdayIncome(todaySettleInCome.subtract(yesterdayReturnInCome));
            
            //上月收入：上月1号0点～本月1号0点，“结算状态”= 已结算-已退回（上月发生的退款）；
            MerchantPromotionFeeQueryModel lastMonthNOSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                    .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(dayOfMonthStartTime).rebateEndTime(dayOfMonthEndTime)
                    .build();
            BigDecimal lastMonthSettleIncome = rebateRecordService.sumByStatus(lastMonthNOSettleIncomeQueryModel);
            
            MerchantPromotionFeeQueryModel lastMonthReturnSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED)
                    .type(type).uid(uid).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(dayOfMonthStartTime).rebateEndTime(dayOfMonthEndTime).build();
            BigDecimal lastMonthReturnSettleIncome = rebateRecordService.sumByStatus(lastMonthReturnSettleIncomeQueryModel);
            merchantPromotionFeeIncomeVO.setLastMonthIncome(lastMonthSettleIncome.subtract(lastMonthReturnSettleIncome));
        }
        
        //本月预估收入：本月1号0点～当前时间，“结算状态” = 未结算+已结算-已退回（本月发生的退款）
        merchantPromotionFeeIncomeVO.setCurrentMonthIncome(getCurrentMonthIncome(uid, type, userType));
        merchantPromotionFeeIncomeVO.setTotalIncome(buildPromotionFeeTotalIncomeVO(type, uid, null, System.currentTimeMillis(), userType, false));
    }
    
    
    private void handleMerchantTodayIncomeByChannelEmployee(Integer type, Long uid, MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO,
            List<MerchantChannelEmployeeBindHistoryDTO> todayList, List<MerchantChannelEmployeeBindHistoryDTO> yesterdayList,
            List<MerchantChannelEmployeeBindHistoryDTO> lastMonthList) {
        BigDecimal todayIncomeResult = new BigDecimal(0);
        
        long todayStartTimeStamp = DateUtils.getTodayStartTimeStamp();
        long todayEndTimeStamp = DateUtils.getTodayEndTimeStamp();

        // 计算渠道员的收入 今日预估收入：“返现日期” = 今日，“结算状态” = 未结算-已退回（今日发生的退款）；
        MerchantPromotionFeeQueryModel todayInComeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)
                .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                .rebateStartTime(todayStartTimeStamp).rebateEndTime(todayEndTimeStamp).build();
        
        BigDecimal todayInCome = rebateRecordService.sumByStatus(todayInComeQueryModel);
        // 查询今天退回的
        todayInComeQueryModel.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
        BigDecimal todayReturnInCome = rebateRecordService.sumByStatus(todayInComeQueryModel);
        // 计算今天预估
        todayIncomeResult = todayInCome.subtract(todayReturnInCome);
        
        long yesterdayStartTime = DateUtils.getTimeAgoStartTime(1);
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(1);
        
        // 计算渠道员的收入  昨日收入：“结算日期” = 今日，“结算状态” = 已结算 - 已退回（昨日发生的退款）；
        MerchantPromotionFeeQueryModel yesterdayReturnQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED)
                .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                .rebateStartTime(yesterdayStartTime).rebateEndTime(yesterdayEndTime).build();
        // 查询昨天退回的数据
        BigDecimal yesterdayReturnIncomeResult = rebateRecordService.sumByStatus(yesterdayReturnQueryModel);
    
        // 计算渠道员的收入  昨日收入：“结算日期” = 今日，“结算状态” = 已结算 - 已退回（昨日发生的退款）；
        MerchantPromotionFeeQueryModel yesterdaySettleQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                .rebateStartTime(yesterdayStartTime).rebateEndTime(yesterdayEndTime).build();
        // 查询昨天结算的
        BigDecimal yesterdayIncomeResult = rebateRecordService.sumByStatus(yesterdaySettleQueryModel);
        
        // 昨天收入
        merchantPromotionFeeIncomeVO.setYesterdayIncome(yesterdayIncomeResult.subtract(yesterdayReturnIncomeResult));
        
        // 上个月的第一天
        long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(DateFormatConstant.LAST_MONTH);
        // 上个月的最后一天
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(DateFormatConstant.LAST_MONTH);
        
        MerchantPromotionFeeQueryModel lastMonthReturnSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder()
                .status(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED).type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid)
                .uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId()).rebateStartTime(lastMonthStartTime)
                .rebateEndTime(lastMonthEndTime).build();
        // 查询上月退回
        BigDecimal  lastReturnMonthIncomeResult = rebateRecordService.sumByStatus(lastMonthReturnSettleIncomeQueryModel);
    
        // 计算渠道员的收入  上月收入：上月1号0点～本月1号0点，“结算状态”= 已结算
        MerchantPromotionFeeQueryModel lastMonthNOSettleIncomeQueryModel = MerchantPromotionFeeQueryModel.builder().status(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED)
                .type(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode()).merchantUid(uid).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                .rebateStartTime(lastMonthStartTime).rebateEndTime(lastMonthEndTime).build();
        // 查询上月结算
        BigDecimal lastMonthSettleIncomeResult = rebateRecordService.sumByStatus(lastMonthNOSettleIncomeQueryModel);
    
        
        merchantPromotionFeeIncomeVO.setTodayIncome(todayIncomeResult);
        merchantPromotionFeeIncomeVO.setLastMonthIncome(lastMonthSettleIncomeResult.subtract(lastReturnMonthIncomeResult));
    }
}
