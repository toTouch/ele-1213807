package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.entity.InvitationActivityRecord;
import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.mapper.InvitationActivityRecordMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.request.activity.InvitationActivityAnalysisRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.InvitationActivityMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityCodeVO;
import com.xiliulou.electricity.vo.InvitationActivityJoinHistoryVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordInfoListVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordInfoVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityAnalysisVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityDetailVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityIncomeAnalysisVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityLineDataVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityStaticsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (InvitationActivityRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-05 20:17:53
 */
@Service("invitationActivityRecordService")
@Slf4j
public class InvitationActivityRecordServiceImpl implements InvitationActivityRecordService {
    
    @Resource
    private InvitationActivityRecordMapper invitationActivityRecordMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private InvitationActivityUserService invitationActivityUserService;
    
    @Autowired
    private InvitationActivityService invitationActivityService;
    
    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;
    
    @Autowired
    private InvitationActivityMemberCardService invitationActivityMemberCardService;
    
    @Autowired
    private UserAmountService userAmountService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private CarRentalPackageOrderService carRentalPackageOrderService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private CarRentalPackageService carRentalPackageService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private UserDelRecordService userDelRecordService;
    
    @Override
    @Slave
    public List<InvitationActivityRecordVO> selectByPage(InvitationActivityRecordQuery query) {
        List<InvitationActivityRecordVO> list = invitationActivityRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.parallelStream().peek(item -> {
            Long franchiseeId = item.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                item.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
        }).collect(Collectors.toList());
    }
    
    @Override
    @Slave
    public Integer selectByPageCount(InvitationActivityRecordQuery query) {
        return invitationActivityRecordMapper.selectByPageCount(query);
    }
    
    @Override
    public InvitationActivityRecord queryByIdFromDB(Long id) {
        return this.invitationActivityRecordMapper.queryById(id);
    }
    
    @Override
    public List<InvitationActivityRecord> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityRecordMapper.queryAllByLimit(offset, limit);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityRecord insert(InvitationActivityRecord invitationActivityRecord) {
        this.invitationActivityRecordMapper.insertOne(invitationActivityRecord);
        return invitationActivityRecord;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityRecord invitationActivityRecord) {
        return this.invitationActivityRecordMapper.update(invitationActivityRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.invitationActivityRecordMapper.deleteById(id) > 0;
    }
    
    @Override
    public Integer addCountAndMoneyByUid(BigDecimal rewardAmount, Long recordId) {
        return this.invitationActivityRecordMapper.addCountAndMoneyByUid(rewardAmount, recordId);
    }
    
    @Override
    public Integer addMoneyByRecordId(BigDecimal rewardAmount, Long recordId) {
        return this.invitationActivityRecordMapper.addMoneyByRecordId(rewardAmount, recordId);
    }
    
    @Override
    public List<InvitationActivityRecord> selectByActivityIdAndUid(List<Long> activityIds, Long uid) {
        return this.invitationActivityRecordMapper.selectByActivityIdAndUid(activityIds, uid);
    }
    
    @Override
    @Slave
    public List<InvitationActivityRecord> selectByUid(Long uid) {
        return this.invitationActivityRecordMapper.selectByUid(uid);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> countByStatics() {
        // 封装结果集
        InvitationActivityStaticsVO invitationActivityStaticsVO = new InvitationActivityStaticsVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        List<InvitationActivityRecord> recordList = this.selectByUid(userInfo.getUid());
        
        BigDecimal totalMoney = BigDecimal.ZERO;
        Integer totalInvitationCount = NumberConstant.ZERO;
        if (CollectionUtils.isNotEmpty(recordList)) {
            totalMoney = recordList.stream().map(InvitationActivityRecord::getMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            totalInvitationCount = recordList.stream().mapToInt(InvitationActivityRecord::getInvitationCount).sum();
        }
        
        invitationActivityStaticsVO.setTotalMoney(totalMoney);
        invitationActivityStaticsVO.setTotalInvitationCount(totalInvitationCount);
        
        return Triple.of(true, null, invitationActivityStaticsVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> listInvitationLineData() {
        List<InvitationActivityLineDataVO> lineDataVOList = new ArrayList<>();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 查询近7天的
        Long startTime = DateUtils.getLastDayStartTime(NumberConstant.SEVEN);
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder().uid(userInfo.getUid()).beginTime(startTime).build();
        List<InvitationActivityJoinHistoryVO> historyVOList = invitationActivityJoinHistoryService.listByInviterUid(query);
        
        if (CollectionUtils.isNotEmpty(historyVOList)) {
            // 将每天的数据进行分组，并按createTime升序排序
            Map<LocalDate, List<InvitationActivityJoinHistoryVO>> dateListMap = historyVOList.stream().collect(
                    Collectors.groupingBy(item -> LocalDate.ofEpochDay(item.getCreateTime() / TimeConstant.DAY_MILLISECOND), Collectors.collectingAndThen(Collectors.toList(),
                            list -> list.stream().sorted(Comparator.comparingLong(InvitationActivityJoinHistoryVO::getCreateTime)).collect(Collectors.toList()))));
            
            dateListMap.forEach((k, v) -> {
                InvitationActivityLineDataVO lineDataVO = new InvitationActivityLineDataVO();
                Integer totalShareCount = NumberConstant.ZERO;
                Integer totalInvitationCount = NumberConstant.ZERO;
                
                if (CollectionUtils.isNotEmpty(v)) {
                    List<InvitationActivityJoinHistoryVO> uniqueHistoryVOList = v.stream().collect(
                            Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                                    map -> new ArrayList<>(map.values())));
                    
                    if (CollectionUtils.isNotEmpty(uniqueHistoryVOList)) {
                        totalShareCount = uniqueHistoryVOList.size();
                    }
                    
                    List<InvitationActivityJoinHistoryVO> uniqueInvitationHistoryVOList = v.stream().filter(item -> Objects.equals(item.getStatus(), NumberConstant.TWO)).collect(
                            Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                                    map -> new ArrayList<>(map.values())));
                    
                    if (CollectionUtils.isNotEmpty(uniqueInvitationHistoryVOList)) {
                        totalInvitationCount = uniqueInvitationHistoryVOList.size();
                    }
                }
                
                lineDataVO.setCreateTime(DateUtils.getDayStartTimeByLocalDate(k));
                lineDataVO.setTotalShareCount(totalShareCount);
                lineDataVO.setTotalInvitationCount(totalInvitationCount);
                
                lineDataVOList.add(lineDataVO);
            });
        }
        
        if (CollectionUtils.isEmpty(lineDataVOList)) {
            return Triple.of(true, null, Collections.emptyList());
        }
        
        return Triple.of(true, null, lineDataVOList);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryInvitationAnalysis(Integer timeType) {
        // 封装结果集
        InvitationActivityAnalysisVO activityAnalysisVO = new InvitationActivityAnalysisVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 根据时间范围查询：昨日/本月/累计
        Long startTime = null;
        Long endTime = null;
        if (Objects.equals(timeType, NumberConstant.ONE)) {
            // 查询昨日
            startTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
            endTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        } else if (Objects.equals(timeType, NumberConstant.TWO)) {
            // 查询本月
            startTime = DateUtils.getDayOfMonthStartTime(NumberConstant.ONE);
        }
        // startTime=null、endTime = null 查询累计
        
        InvitationActivityJoinHistoryQuery historyQuery = InvitationActivityJoinHistoryQuery.builder().uid(userInfo.getUid()).beginTime(startTime).endTime(endTime).build();
        List<InvitationActivityJoinHistoryVO> historyVOList = invitationActivityJoinHistoryService.listByInviterUidOfAdmin(historyQuery);
        
        // 邀请总人数、邀请成功总人数
        Integer totalShareCount = NumberConstant.ZERO;
        Integer totalInvitationCount = NumberConstant.ZERO;
        if (CollectionUtils.isNotEmpty(historyVOList)) {
            // 根据joinUid去重
            List<InvitationActivityJoinHistoryVO> uniqueHistoryVOList = historyVOList.stream().collect(
                    Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                            map -> new ArrayList<>(map.values())));
            
            if (CollectionUtils.isNotEmpty(uniqueHistoryVOList)) {
                totalShareCount = uniqueHistoryVOList.size();
            }
            
            List<InvitationActivityJoinHistoryVO> uniqueInvitationHistoryVOList = historyVOList.stream().filter(item -> Objects.equals(item.getStatus(), NumberConstant.TWO))
                    .collect(Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                            map -> new ArrayList<>(map.values())));
            
            if (CollectionUtils.isNotEmpty(uniqueInvitationHistoryVOList)) {
                totalInvitationCount = uniqueInvitationHistoryVOList.size();
            }
        }
        
        activityAnalysisVO.setTotalShareCount(totalShareCount);
        activityAnalysisVO.setTotalInvitationCount(totalInvitationCount);
        
        return Triple.of(true, null, activityAnalysisVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryInvitationDetail(InvitationActivityAnalysisRequest request) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 根据时间范围查询：昨日/本月/累计
        Integer timeType = request.getTimeType();
        Long startTime = null;
        Long endTime = null;
        if (Objects.equals(timeType, NumberConstant.ONE)) {
            // 查询昨日
            startTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
            endTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        } else if (Objects.equals(timeType, NumberConstant.TWO)) {
            // 查询本月
            startTime = DateUtils.getDayOfMonthStartTime(NumberConstant.ONE);
        }
        // startTime=null、endTime = null 查询累计
        
        // 邀请明细
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder().uid(userInfo.getUid()).beginTime(startTime).endTime(endTime)
                .offset(request.getOffset()).size(request.getSize()).build();
        
        Set<Integer> statusSet = Set.of(NumberConstant.ZERO, NumberConstant.ONE, NumberConstant.TWO, NumberConstant.THREE, NumberConstant.FOUR, NumberConstant.FIVE);
        Integer status = request.getStatus();
        if (Objects.nonNull(status) && statusSet.contains(status)) {
            // status=0 查全部
            status = Objects.equals(status, NumberConstant.ZERO) ? null : status;
            query.setStatus(status);
        }
        
        List<InvitationActivityJoinHistoryVO> historyVOList = invitationActivityJoinHistoryService.listByInviterUidDistinctJoin(query);
        
        // 根据joinUid进行去重
        List<InvitationActivityDetailVO> rspList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(historyVOList)) {
            rspList = historyVOList.stream().map(item -> {
                Long joinUid = item.getJoinUid();
                
                InvitationActivityDetailVO invitationActivityDetailVO = InvitationActivityDetailVO.builder().joinUid(joinUid).joinTime(item.getStartTime())
                        .activityId(item.getActivityId()).activityName(item.getActivityName()).payCount(ObjectUtils.defaultIfNull(item.getPayCount(), NumberConstant.ZERO))
                        .money(ObjectUtils.defaultIfNull(item.getMoney(), BigDecimal.ZERO)).status(item.getStatus()).build();
                
                UserInfo joinUser = userInfoService.queryByUidFromCache(joinUid);
                if (Objects.isNull(joinUser)) {
                    joinUser = userInfoService.queryByUidFromDbIncludeDelUser(joinUid);
                }
                
                Optional.ofNullable(joinUser).ifPresent(user -> {
                    invitationActivityDetailVO.setJoinName(user.getName());
                    invitationActivityDetailVO.setJoinPhone(user.getPhone());
                });
                
                return invitationActivityDetailVO;
                
            }).collect(Collectors.toList());
            
            if (CollectionUtils.isEmpty(rspList)) {
                rspList = Collections.emptyList();
            }
        }
        
        return Triple.of(true, null, rspList);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryInvitationIncomeAnalysis(Integer timeType) {
        // 封装结果集
        InvitationActivityIncomeAnalysisVO analysisVO = new InvitationActivityIncomeAnalysisVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 根据时间范围查询：昨日/本月/累计
        Long startTime = null;
        Long endTime = null;
        if (Objects.equals(timeType, NumberConstant.ONE)) {
            // 查询昨日
            startTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
            endTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        } else if (Objects.equals(timeType, NumberConstant.TWO)) {
            // 查询本月
            startTime = DateUtils.getDayOfMonthStartTime(NumberConstant.ONE);
        }
        // startTime=null、endTime = null 查询累计
        
        // 首返奖励及人数、续返奖励及人数
        InvitationActivityJoinHistoryQuery historyQuery = InvitationActivityJoinHistoryQuery.builder().uid(userInfo.getUid()).beginTime(startTime).endTime(endTime).build();
        
        List<InvitationActivityJoinHistoryVO> historyVOList = invitationActivityJoinHistoryService.listByInviterUid(historyQuery);
        
        InvitationActivityIncomeAnalysisVO incomeAndMemCountVO = this.getIncomeAndMemCount(historyVOList);
        
        analysisVO.setTotalIncome(Objects.isNull(incomeAndMemCountVO.getTotalIncome()) ? BigDecimal.ZERO : incomeAndMemCountVO.getTotalIncome());
        analysisVO.setFirstTotalIncome(Objects.isNull(incomeAndMemCountVO.getFirstTotalIncome()) ? BigDecimal.ZERO : incomeAndMemCountVO.getFirstTotalIncome());
        analysisVO.setFirstTotalMemCount(Objects.isNull(incomeAndMemCountVO.getFirstTotalMemCount()) ? NumberConstant.ZERO : incomeAndMemCountVO.getFirstTotalMemCount());
        analysisVO.setRenewTotalIncome(Objects.isNull(incomeAndMemCountVO.getRenewTotalIncome()) ? BigDecimal.ZERO : incomeAndMemCountVO.getRenewTotalIncome());
        analysisVO.setRenewTotalMemCount(Objects.isNull(incomeAndMemCountVO.getRenewTotalMemCount()) ? NumberConstant.ZERO : incomeAndMemCountVO.getRenewTotalMemCount());
        
        return Triple.of(true, null, analysisVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryInvitationIncomeDetail(InvitationActivityAnalysisRequest request) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 根据时间范围查询：昨日/本月/累计
        Integer timeType = request.getTimeType();
        Long startTime = null;
        Long endTime = null;
        if (Objects.equals(timeType, NumberConstant.ONE)) {
            // 查询昨日
            startTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
            endTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        } else if (Objects.equals(timeType, NumberConstant.TWO)) {
            // 查询本月
            startTime = DateUtils.getDayOfMonthStartTime(NumberConstant.ONE);
        }
        // startTime=null、endTime = null 查询累计
        
        // 收入明细
        InvitationActivityJoinHistoryQuery historyQuery = InvitationActivityJoinHistoryQuery.builder().uid(userInfo.getUid()).beginTime(startTime).endTime(endTime)
                .offset(request.getOffset()).size(request.getSize()).build();
        
        Set<Integer> statusSet = Set.of(NumberConstant.ZERO, NumberConstant.ONE, NumberConstant.TWO, NumberConstant.THREE, NumberConstant.FOUR, NumberConstant.FIVE);
        Integer status = request.getStatus();
        if (Objects.nonNull(status) && statusSet.contains(status)) {
            // status=0 查全部
            status = Objects.equals(status, NumberConstant.ZERO) ? null : status;
            historyQuery.setStatus(status);
        }
        
        List<InvitationActivityJoinHistoryVO> historyVOList = invitationActivityJoinHistoryService.listByInviterUid(historyQuery);
        List<InvitationActivityDetailVO> rspList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(historyVOList)) {
            rspList = historyVOList.stream().map(item -> {
                InvitationActivityDetailVO vo = InvitationActivityDetailVO.builder().activityId(item.getActivityId()).activityName(item.getActivityName())
                        .joinUid(item.getJoinUid()).joinTime(item.getCreateTime()).money(Objects.isNull(item.getMoney()) ? BigDecimal.ZERO : item.getMoney())
                        .payCount(Objects.isNull(item.getPayCount()) ? NumberConstant.ZERO : item.getPayCount()).status(item.getStatus()).build();
                
                UserInfo joinUser = userInfoService.queryByUidFromCache(item.getJoinUid());
                if (Objects.isNull(joinUser)) {
                    joinUser = userInfoService.queryByUidFromDbIncludeDelUser(item.getJoinUid());
                }
                
                Optional.ofNullable(joinUser).ifPresent(u -> {
                    vo.setJoinPhone(u.getPhone());
                    vo.setJoinName(u.getName());
                });
                
                Long packageId = item.getPackageId();
                Integer packageType = item.getPackageType();
                if (Objects.nonNull(packageId) && Objects.nonNull(packageType)) {
                    if (Objects.equals(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode(), packageType)) {
                        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
                        Optional.ofNullable(batteryMemberCard).ifPresent(b -> {
                            vo.setPackageId(packageId);
                            vo.setPackageName(b.getName());
                            vo.setPackageType(packageType);
                        });
                    } else {
                        CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(packageId);
                        Optional.ofNullable(carRentalPackagePo).ifPresent(b -> {
                            vo.setPackageId(packageId);
                            vo.setPackageName(b.getName());
                            vo.setPackageType(packageType);
                        });
                    }
                }
                return vo;
            }).collect(Collectors.toList());
            
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        return Triple.of(true, null, rspList);
    }
    
    private InvitationActivityIncomeAnalysisVO getIncomeAndMemCount(List<InvitationActivityJoinHistoryVO> historyVOList) {
        InvitationActivityIncomeAnalysisVO incomeDetailVO = new InvitationActivityIncomeAnalysisVO();
        
        if (CollectionUtils.isNotEmpty(historyVOList)) {
            // 根据 payCount=1为1组，不等于1为另一组
            Map<Boolean, List<InvitationActivityJoinHistoryVO>> groupedByPayCount = historyVOList.stream()
                    .collect(Collectors.partitioningBy(history -> Objects.equals(Optional.ofNullable(history.getPayCount()).orElse(NumberConstant.ZERO), NumberConstant.ONE)));
            
            List<InvitationActivityJoinHistoryVO> firstHistoryList = groupedByPayCount.get(Boolean.TRUE);
            List<InvitationActivityJoinHistoryVO> renewHistoryList = groupedByPayCount.get(Boolean.FALSE);
            
            //首返奖励及人数
            BigDecimal firstTotalIncome = BigDecimal.ZERO;
            Integer firstTotalMemCount = NumberConstant.ZERO;
            if (CollectionUtils.isNotEmpty(firstHistoryList)) {
                firstTotalIncome = firstHistoryList.stream().map(history -> Optional.ofNullable(history.getMoney()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 根据joinUid进行去重
                List<InvitationActivityJoinHistoryVO> uniqueFirstHistoryVOList = firstHistoryList.stream().collect(
                        Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                                map -> new ArrayList<>(map.values())));
                
                if (CollectionUtils.isNotEmpty(uniqueFirstHistoryVOList)) {
                    firstTotalMemCount = uniqueFirstHistoryVOList.size();
                }
            }
            
            //续返奖励及人数
            BigDecimal renewTotalIncome = BigDecimal.ZERO;
            Integer renewTotalMemCount = NumberConstant.ZERO;
            if (CollectionUtils.isNotEmpty(renewHistoryList)) {
                renewTotalIncome = renewHistoryList.stream().map(history -> Optional.ofNullable(history.getMoney()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                List<InvitationActivityJoinHistoryVO> uniqueRenewHistoryVOList = renewHistoryList.stream().filter(history -> Objects.nonNull(history.getPayCount())).collect(
                        Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                                map -> new ArrayList<>(map.values())));
                
                if (CollectionUtils.isNotEmpty(uniqueRenewHistoryVOList)) {
                    renewTotalMemCount = uniqueRenewHistoryVOList.size();
                }
            }
            
            BigDecimal totalIncome = firstTotalIncome.add(renewTotalIncome);
            
            // 收入为0时，人数也为0
            firstTotalMemCount = Objects.equals(firstTotalIncome, BigDecimal.ZERO) ? NumberConstant.ZERO : firstTotalMemCount;
            renewTotalMemCount = Objects.equals(renewTotalIncome, BigDecimal.ZERO) ? NumberConstant.ZERO : renewTotalMemCount;
            
            incomeDetailVO.setFirstTotalIncome(firstTotalIncome);
            incomeDetailVO.setFirstTotalMemCount(firstTotalMemCount);
            incomeDetailVO.setRenewTotalIncome(renewTotalIncome);
            incomeDetailVO.setRenewTotalMemCount(renewTotalMemCount);
            incomeDetailVO.setTotalIncome(totalIncome);
        }
        
        return incomeDetailVO;
    }
    
    @Override
    public Triple<Boolean, String, Object> selectUserInvitationDetail() {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        //获取当前用户所绑定的套餐返现活动
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            log.warn("INVITATION ACTIVITY WARN! not found invitationActivityUserList,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        // 为了兼容旧版本（单个活动），获取第一个结果
        InvitationActivityUser invitationActivityUser = invitationActivityUserList.get(0);
        List<Long> activityIds = List.of(invitationActivityUser.getActivityId());
        
        List<InvitationActivityRecord> activityRecordList = this.selectByActivityIdAndUid(activityIds, userInfo.getUid());
        if (CollectionUtils.isEmpty(activityRecordList)) {
            log.warn("INVITATION ACTIVITY WARN! not found activityRecordList,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        //        InvitationActivityRecord activityRecord = this.selectByUid(userInfo.getUid());
        //        if (Objects.isNull(activityRecord)) {
        //            log.warn("INVITATION ACTIVITY WARN! not found activityRecord,uid={}", userInfo.getUid());
        //            return Triple.of(true, null, null);
        //        }
        
        InvitationActivityRecordInfoListVO invitationActivityRecordInfoListVO = new InvitationActivityRecordInfoListVO();
        BeanUtils.copyProperties(activityRecordList.get(0), invitationActivityRecordInfoListVO);
        
        return Triple.of(true, null, invitationActivityRecordInfoListVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> selectUserInvitationDetailV2() {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("INVITATION ACTIVITY WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        List<InvitationActivityRecord> recordList = selectByUid(userInfo.getUid());
        List<Long> activityIds = recordList.stream().map(InvitationActivityRecord::getActivityId).collect(Collectors.toList());
        
        List<InvitationActivityRecord> activityRecords = this.selectByActivityIdAndUid(activityIds, userInfo.getUid());
        if (CollectionUtils.isEmpty(activityRecords)) {
            log.warn("INVITATION ACTIVITY WARN! not found activityRecords,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        BigDecimal totalMoney = BigDecimal.ZERO;
        Integer totalInvitationCount = NumberConstant.ZERO;
        List<InvitationActivityRecordInfoListVO> list = new ArrayList<>();
        for (InvitationActivityRecord record : activityRecords) {
            totalMoney = totalMoney.add(record.getMoney());
            totalInvitationCount += record.getInvitationCount();
            
            InvitationActivityRecordInfoListVO invitationActivityRecordInfoListVO = new InvitationActivityRecordInfoListVO();
            BeanUtils.copyProperties(record, invitationActivityRecordInfoListVO);
            list.add(invitationActivityRecordInfoListVO);
        }
        InvitationActivityRecordInfoVO invitationActivityRecordInfoVO = InvitationActivityRecordInfoVO.builder().totalMoney(totalMoney).totalInvitationCount(totalInvitationCount)
                .invitationActivityRecordInfoList(list).build();
        
        return Triple.of(true, null, invitationActivityRecordInfoVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> generateCode() {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "100001", "用户不存在");
        }
        
        List<InvitationActivity> invitationActivities = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivities)) {
            log.warn("INVITATION ACTIVITY WARN! invitationActivities is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "100391", "暂无上架的活动");
        }
        
        List<Long> activityIds = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toList());
        
        //        InvitationActivity invitationActivity = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        //        if (Objects.isNull(invitationActivity)) {
        //            log.error("INVITATION ACTIVITY ERROR! not found InvitationActivity,uid={}", userInfo.getUid());
        //            return Triple.of(false, "100391", "暂无上架的活动");
        //        }
        
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList) || !activityIds.contains(invitationActivityUserList.get(0).getActivityId())) {
            log.warn("INVITATION ACTIVITY WARN! invitationActivityUser is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100392", "无权限参加此活动");
        }
        
        InvitationActivityUser invitationActivityUser = invitationActivityUserList.get(0);
        
        if (StringUtils.isBlank(userInfo.getPhone())) {
            log.warn("INVITATION ACTIVITY WARN! phone is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant) || StringUtils.isBlank(tenant.getCode())) {
            log.warn("INVITATION ACTIVITY WARN! tenant is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        InvitationActivityCodeVO invitationActivityCodeVO = new InvitationActivityCodeVO();
        invitationActivityCodeVO.setCode(codeEnCoder(invitationActivityUser.getActivityId().toString(), userInfo.getUid()));
        invitationActivityCodeVO.setTenantCode(tenant.getCode());
        invitationActivityCodeVO.setPhone(userInfo.getPhone());
        
        InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(
                new LambdaQueryWrapper<InvitationActivityRecord>().eq(InvitationActivityRecord::getUid, userInfo.getUid())
                        .eq(InvitationActivityRecord::getActivityId, invitationActivityUser.getActivityId()));
        if (Objects.isNull(invitationActivityRecord)) {
            //第一次分享  生成分享记录
            InvitationActivityRecord invitationActivityRecordInsert = new InvitationActivityRecord();
            invitationActivityRecordInsert.setActivityId(invitationActivityUser.getActivityId());
            invitationActivityRecordInsert.setUid(userInfo.getUid());
            invitationActivityRecordInsert.setCode(RandomUtil.randomNumbers(6));
            invitationActivityRecordInsert.setShareCount(0);
            invitationActivityRecordInsert.setInvitationCount(0);
            invitationActivityRecordInsert.setMoney(BigDecimal.ZERO);
            invitationActivityRecordInsert.setTenantId(TenantContextHolder.getTenantId());
            invitationActivityRecordInsert.setStatus(InvitationActivityRecord.STATUS_SUCCESS);
            invitationActivityRecordInsert.setCreateTime(System.currentTimeMillis());
            invitationActivityRecordInsert.setUpdateTime(System.currentTimeMillis());
            
            InvitationActivity activity = invitationActivityService.queryByIdFromCache(invitationActivityUser.getActivityId());
            if (Objects.nonNull(activity) && Objects.nonNull(activity.getFranchiseeId())) {
                invitationActivityRecordInsert.setFranchiseeId(activity.getFranchiseeId());
            }
            
            invitationActivityRecordMapper.insertOne(invitationActivityRecordInsert);
        }
        
        return Triple.of(true, null, invitationActivityCodeVO);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> generateCodeV2() {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        if (StringUtils.isBlank(userInfo.getPhone())) {
            log.warn("INVITATION ACTIVITY WARN! phone is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant) || StringUtils.isBlank(tenant.getCode())) {
            log.warn("INVITATION ACTIVITY WARN! tenant is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        List<InvitationActivity> invitationActivities = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivities)) {
            log.warn("INVITATION ACTIVITY WARN! invitationActivities is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            log.warn("INVITATION ACTIVITY WARN! invitationActivityUserList is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        Set<Long> activityIdList = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toSet());
        
        //过滤后的
        List<InvitationActivityUser> newInvitationActivityUserList = invitationActivityUserList.stream()
                .filter(activityUser -> activityIdList.contains(activityUser.getActivityId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newInvitationActivityUserList)) {
            log.warn("INVITATION ACTIVITY WARN! all activities bound user is invalid,uid={}", userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        List<InvitationActivityRecord> invitationActivityRecordList = new ArrayList<>();
        InvitationActivityRecord invitationActivityRecordIn = InvitationActivityRecord.builder().uid(userInfo.getUid()).code(RandomUtil.randomNumbers(NumberConstant.SIX))
                .shareCount(NumberConstant.ZERO).invitationCount(NumberConstant.ZERO).money(BigDecimal.ZERO).tenantId(TenantContextHolder.getTenantId())
                .status(InvitationActivityRecord.STATUS_SUCCESS).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
        StringJoiner activityIdsStr = new StringJoiner(StrUtil.COMMA);
        newInvitationActivityUserList.forEach(item -> {
            Long activityId = item.getActivityId();
            activityIdsStr.add(activityId.toString());
            
            InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(
                    new LambdaQueryWrapper<InvitationActivityRecord>().eq(InvitationActivityRecord::getUid, userInfo.getUid())
                            .eq(InvitationActivityRecord::getActivityId, activityId));
            
            if (Objects.isNull(invitationActivityRecord)) {
                //第一次分享  生成分享记录
                InvitationActivityRecord invitationActivityRecordInsert = new InvitationActivityRecord();
                
                BeanUtils.copyProperties(invitationActivityRecordIn, invitationActivityRecordInsert);
                invitationActivityRecordInsert.setActivityId(activityId);
                
                InvitationActivity activity = invitationActivityService.queryByIdFromCache(activityId);
                if (Objects.nonNull(activity) && Objects.nonNull(activity.getFranchiseeId())) {
                    invitationActivityRecordInsert.setFranchiseeId(activity.getFranchiseeId());
                }
                
                invitationActivityRecordList.add(invitationActivityRecordInsert);
            }
        });
        
        if (CollectionUtils.isNotEmpty(invitationActivityRecordList)) {
            invitationActivityRecordMapper.batchInsert(invitationActivityRecordList);
        }
        
        InvitationActivityCodeVO invitationActivityCodeVO = new InvitationActivityCodeVO();
        invitationActivityCodeVO.setCode(codeEnCoder(activityIdsStr.toString(), userInfo.getUid()));
        invitationActivityCodeVO.setTenantCode(tenant.getCode());
        invitationActivityCodeVO.setPhone(userInfo.getPhone());
        
        return Triple.of(true, null, invitationActivityCodeVO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> joinActivity(InvitationActivityQuery query) {
        if (!redisService.setNx(CacheConstant.CACHE_SCAN_INTO_ACTIVITY_LOCK + SecurityUtils.getUid(), "1", 2000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfoExtra)) {
            log.warn("INVITATION ACTIVITY WARN! Not found userInfoExtra, joinUid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        // 530活动互斥判断
        R canJoinActivity = merchantJoinRecordService.canJoinActivity(userInfo, userInfoExtra, null, null);
        if (!canJoinActivity.isSuccess()) {
            return Triple.of(false, canJoinActivity.getErrCode(), canJoinActivity.getErrMsg());
        }
    
        // 是否被删除过的老用户
        if (userDelRecordService.existsByDelPhoneAndDelIdNumber(userInfo.getPhone(), userInfo.getIdNumber(), userInfo.getTenantId())) {
            log.warn("INVITATION ACTIVITY WARN! The user ever deleted, joinUid={}, phone={}", SecurityUtils.getUid(), userInfo.getPhone());
            return Triple.of(false, "120122", "此活动仅限新用户参加，您已是平台用户无法参与，感谢您的支持");
        }
        
        String decrypt = null;
        try {
            decrypt = codeDeCoder(query.getCode());
        } catch (Exception e) {
            log.error("INVITATION ACTIVITY ERROR! decode fail,uid={},code={}", SecurityUtils.getUid(), query.getCode());
        }
        
        if (StringUtils.isBlank(decrypt)) {
            log.warn("INVITATION ACTIVITY WARN! invitation activity code decrypt error,code={}, uid={}", query.getCode(), userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        String[] split = decrypt.split(String.valueOf(StrUtil.C_COLON));
        if (ArrayUtils.isEmpty(split) || split.length != NumberConstant.TWO) {
            log.warn("INVITATION ACTIVITY WARN! illegal code! code={}, uid={}", query.getCode(), userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        String activityIdStr = split[NumberConstant.ZERO];
        if (StringUtils.isEmpty(activityIdStr)) {
            log.warn("INVITATION ACTIVITY WARN! not found invitationActivity, uid={}", userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        Long invitationUid = Long.parseLong(split[NumberConstant.ONE]);
        // 是否自己扫自己的码
        if (Objects.equals(userInfo.getUid(), invitationUid)) {
            log.info("INVITATION ACTIVITY INFO! illegal operate! invitationUid={}, uid={}", invitationUid, userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        UserInfo invitationUserInfo = userInfoService.queryByUidFromCache(invitationUid);
        if (Objects.isNull(invitationUserInfo)) {
            log.warn("INVITATION ACTIVITY WARN! not found invitationUserInfo,invitationUid={},uid={}", invitationUid, userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        List<Long> activityIdList = Arrays.stream(activityIdStr.split(String.valueOf(StrUtil.C_COMMA))).map(Long::valueOf).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(activityIdList)) {
            log.warn("INVITATION ACTIVITY WARN! joinActivity activityIdList is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "100463", "二维码已失效");
        }
        
        List<InvitationActivity> invitationActivities = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivities)) {
            log.warn("INVITATION ACTIVITY WARN! joinActivity invitationActivities is empty, invitationUid={}, uid={}", invitationUid, userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(invitationUid);
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            log.warn("INVITATION ACTIVITY WARN! joinActivity invitationActivityUserList is empty, invitationUid={}, uid={}", invitationUid, userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        Set<Long> invitationActivitiesSet = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toSet());
        Set<Long> invitationActivityUserSet = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toSet());
        
        //过滤掉未上线的活动
        Set<Long> newActivityIdSet1 = activityIdList.stream().filter(invitationActivitiesSet::contains).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(newActivityIdSet1)) {
            log.warn("INVITATION ACTIVITY WARN! joinActivity activities in newActivityIdSet1 are all down,uid={}", userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        //过滤掉邀请人解绑的活动
        Set<Long> newActivityIdSet2 = newActivityIdSet1.stream().filter(invitationActivityUserSet::contains).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(newActivityIdSet2)) {
            log.warn("INVITATION ACTIVITY WARN! joinActivity activities in newActivityIdSet2 are all down,uid={}", userInfo.getUid());
            return Triple.of(false, "100399", "该活动已下架，二维码失效");
        }
        
        // 会员扩展表是否需要更新最新参与活动类型
        boolean needUpdateUserInfoExtra = true;
        
        for (Long activityId : newActivityIdSet2) {
            //用户是否已参与过此活动
            Integer exist = invitationActivityJoinHistoryService.existsByJoinUidAndActivityId(userInfo.getUid(), activityId);
            if (Objects.nonNull(exist)) {
                log.warn("INVITATION ACTIVITY WARN! user already join invitation activity,activityId={},uid={}", activityId, userInfo.getUid());
                return Triple.of(true, "100398", "您已参与过该活动，无法重复参加");
            }
            
            // 获取活动记录
            InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(
                    new LambdaQueryWrapper<InvitationActivityRecord>().eq(InvitationActivityRecord::getUid, invitationUid).eq(InvitationActivityRecord::getActivityId, activityId));
            
            // 获取活动
            InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(activityId);
            
            //更新活动邀请次数
            invitationActivityRecordMapper.addShareCount(invitationActivityRecord.getId());
            
            // 计算活动有效期
            long expiredTime;
            if (Objects.nonNull(invitationActivity.getHours()) && !Objects.equals(invitationActivity.getHours(), NumberConstant.ZERO)) {
                expiredTime = System.currentTimeMillis() + invitationActivity.getHours() * TimeConstant.HOURS_MILLISECOND;
            } else {
                Integer minutes = Objects.isNull(invitationActivity.getMinutes()) ? NumberConstant.ZERO : invitationActivity.getMinutes();
                expiredTime = System.currentTimeMillis() + minutes * TimeConstant.MINUTE_MILLISECOND;
            }
            
            //保存活动参与记录
            InvitationActivityJoinHistory invitationActivityJoinHistoryInsert = new InvitationActivityJoinHistory();
            invitationActivityJoinHistoryInsert.setUid(invitationUid);
            invitationActivityJoinHistoryInsert.setJoinUid(userInfo.getUid());
            invitationActivityJoinHistoryInsert.setActivityId(activityId);
            invitationActivityJoinHistoryInsert.setRecordId(invitationActivityRecord.getId());
            invitationActivityJoinHistoryInsert.setStatus(InvitationActivityJoinHistory.STATUS_INIT);
            invitationActivityJoinHistoryInsert.setStartTime(System.currentTimeMillis());
            invitationActivityJoinHistoryInsert.setExpiredTime(expiredTime);
            invitationActivityJoinHistoryInsert.setTenantId(TenantContextHolder.getTenantId());
            invitationActivityJoinHistoryInsert.setCreateTime(System.currentTimeMillis());
            invitationActivityJoinHistoryInsert.setUpdateTime(System.currentTimeMillis());
            
            Long activityFranchiseeId = invitationActivity.getFranchiseeId();
            if (Objects.nonNull(activityFranchiseeId)) {
                invitationActivityJoinHistoryInsert.setFranchiseeId(activityFranchiseeId);
            }
            
            invitationActivityJoinHistoryService.insert(invitationActivityJoinHistoryInsert);
            
            if (needUpdateUserInfoExtra) {
                // 530会员扩展表更新最新参与活动类型
                userInfoExtraService.updateByUid(
                        UserInfoExtra.builder().uid(userInfo.getUid()).latestActivitySource(UserInfoActivitySourceEnum.SUCCESS_INVITATION_ACTIVITY.getCode()).build());
                needUpdateUserInfoExtra = false;
            }
            
        }
        return Triple.of(true, null, null);
    }
    
    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleInvitationActivity(UserInfo userInfo, String orderId) {
        //是否参与过套餐返现活动
        InvitationActivityJoinHistory activityJoinHistory = invitationActivityJoinHistoryService.selectByJoinUid(userInfo.getUid());
        if (Objects.isNull(activityJoinHistory)) {
            log.info("INVITATION ACTIVITY INFO!not found activityJoinHistory,uid={}", userInfo.getUid());
            return;
        }
        
        InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(activityJoinHistory.getActivityId());
        if (Objects.isNull(invitationActivity)) {
            log.warn("INVITATION ACTIVITY ERROR!not found invitationActivity,uid={},activityId={}", userInfo.getUid(), activityJoinHistory.getActivityId());
            return;
        }
        
        //是否有上架的套餐返现活动
        List<InvitationActivity> invitationActivitys = invitationActivityService.selectUsableActivity(userInfo.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivitys)) {
            log.info("INVITATION ACTIVITY INFO!invitationActivitys is empty,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return;
        }
        
        List<Long> activityIds = invitationActivitys.stream().map(InvitationActivity::getId).collect(Collectors.toList());
        if (!activityIds.contains(invitationActivity.getId())) {
            log.info("INVITATION ACTIVITY INFO!enable invitationActivitys not contains user join activity,activityId={},uid={}", invitationActivity.getId(), userInfo.getUid());
            return;
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.info("INVITATION ACTIVITY INFO!not found electricityMemberCardOrder,orderId={},uid={}", orderId, userInfo.getUid());
            return;
        }
        
        //是否购买的是活动指定的套餐
        List<Long> memberCardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activityJoinHistory.getActivityId());
        if (CollectionUtils.isEmpty(memberCardIds) || !memberCardIds.contains(electricityMemberCardOrder.getMemberCardId())) {
            log.info("INVITATION ACTIVITY INFO!invite fail,activityId={},membercardId={},uid={}", activityJoinHistory.getActivityId(), electricityMemberCardOrder.getMemberCardId(),
                    userInfo.getUid());
            return;
        }
        
        //返现金额
        BigDecimal rewardAmount = null;
        
        //首次购买套餐
        if (electricityMemberCardOrder.getPayCount() == 1) {
            //首次购买需要判断活动是否过期
            if (activityJoinHistory.getExpiredTime() < System.currentTimeMillis()) {
                log.warn("INVITATION ACTIVITY INFO!activity already sold out,activityId={},uid={}", activityJoinHistory.getActivityId(), userInfo.getUid());
                return;
            }
            
            rewardAmount = invitationActivity.getFirstReward();
            //修改参与状态
            InvitationActivityJoinHistory activityJoinHistoryUpdate = new InvitationActivityJoinHistory();
            activityJoinHistoryUpdate.setId(activityJoinHistory.getId());
            activityJoinHistoryUpdate.setStatus(InvitationActivityJoinHistory.STATUS_SUCCESS);
            activityJoinHistoryUpdate.setMoney(rewardAmount);
            activityJoinHistoryUpdate.setPayCount(electricityMemberCardOrder.getPayCount());
            activityJoinHistoryUpdate.setUpdateTime(System.currentTimeMillis());
            invitationActivityJoinHistoryService.update(activityJoinHistoryUpdate);
            
            //给邀请人增加邀请成功人数及返现金额
            this.addCountAndMoneyByUid(rewardAmount, activityJoinHistory.getRecordId());
        } else {
            //非首次购买需要判断 首次购买是否成功
            if (!Objects.equals(activityJoinHistory.getStatus(), InvitationActivityJoinHistory.STATUS_SUCCESS)) {
                log.warn("INVITATION ACTIVITY INFO!activity join fail,activityHistoryId={},uid={}", activityJoinHistory.getId(), userInfo.getUid());
                return;
            }
            
            rewardAmount = invitationActivity.getOtherReward();
            //保存参与记录
            InvitationActivityJoinHistory activityJoinHistoryInsert = new InvitationActivityJoinHistory();
            activityJoinHistoryInsert.setUid(activityJoinHistory.getUid());
            activityJoinHistoryInsert.setRecordId(activityJoinHistory.getRecordId());
            activityJoinHistoryInsert.setJoinUid(activityJoinHistory.getJoinUid());
            activityJoinHistoryInsert.setStartTime(activityJoinHistory.getStartTime());
            activityJoinHistoryInsert.setExpiredTime(activityJoinHistory.getExpiredTime());
            activityJoinHistoryInsert.setActivityId(activityJoinHistory.getActivityId());
            activityJoinHistoryInsert.setStatus(activityJoinHistory.getStatus());
            activityJoinHistoryInsert.setPayCount(electricityMemberCardOrder.getPayCount());
            activityJoinHistoryInsert.setMoney(rewardAmount);
            activityJoinHistoryInsert.setTenantId(userInfo.getTenantId());
            activityJoinHistoryInsert.setCreateTime(System.currentTimeMillis());
            activityJoinHistoryInsert.setUpdateTime(System.currentTimeMillis());
            if (Objects.nonNull(activityJoinHistory.getFranchiseeId())) {
                activityJoinHistoryInsert.setFranchiseeId(activityJoinHistory.getFranchiseeId());
            }
            invitationActivityJoinHistoryService.insert(activityJoinHistoryInsert);
            
            //给邀请人增加返现金额
            this.addMoneyByRecordId(rewardAmount, activityJoinHistory.getRecordId());
        }
        
        //处理返现
        userAmountService.handleInvitationActivityAmount(userInfo, activityJoinHistory.getUid(), rewardAmount);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleInvitationActivityByPackage(UserInfo userInfo, String orderNo, Integer packageType) {
        //是否参与过套餐返现活动
        List<InvitationActivityJoinHistory> activityJoinHistoryList = invitationActivityJoinHistoryService.listByJoinUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(activityJoinHistoryList)) {
            log.info("Invitation activity info! not found activityJoinHistoryList,uid={}", userInfo.getUid());
            return;
        }
        
        // 解析状态 用于后面判断非首次购买是否成功
        Set<Integer> activityJoinHistoryStatusSet = activityJoinHistoryList.stream().map(InvitationActivityJoinHistory::getStatus).collect(Collectors.toSet());
        
        // 根据activityId去重
        activityJoinHistoryList = new ArrayList<>(
                activityJoinHistoryList.stream().collect(Collectors.toMap(InvitationActivityJoinHistory::getActivityId, history -> history, (existing, replacement) -> existing))
                        .values());
        
        // 获取租户下所有上架的套餐返现活动
        Integer tenantId = userInfo.getTenantId();
        List<InvitationActivity> invitationActivities = invitationActivityService.selectUsableActivity(tenantId);
        if (CollectionUtils.isEmpty(invitationActivities)) {
            log.info("Invitation activity info! invitationActivities is empty,tenantId={},uid={}", tenantId, userInfo.getUid());
            return;
        }
        
        // 对参与过的活动进行过滤，过滤掉未上架的活动
        Set<Long> activityIdsSet = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toSet());
        activityJoinHistoryList = activityJoinHistoryList.stream().filter(history -> activityIdsSet.contains(history.getActivityId())).collect(Collectors.toList());
        
        //增加换电套餐和租车及车电一体套餐的判断逻辑
        Long packageId;
        Integer payCount = userInfo.getPayCount();
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)) {
            // UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromDB(userInfo.getUid());
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderNo);
            
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.info("Invitation activity info, not found battery package order,orderId={},uid={}", orderNo, userInfo.getUid());
                return;
            }
            packageId = electricityMemberCardOrder.getMemberCardId();
        } else {
            //获取租车或者车电一体订单信息
            CarRentalPackageOrderPo carRentalPackageOrderPO = carRentalPackageOrderService.selectByOrderNo(orderNo);
            if (Objects.isNull(carRentalPackageOrderPO)) {
                log.info("Invitation activity info, Not found for car rental package, order number = {}", orderNo);
                return;
            }
            
            //根据当前用户uid, 订单号, 购买成功等条件查询，当前套餐的购买记录
            CarRentalPackageOrderQryModel queryModel = new CarRentalPackageOrderQryModel();
            queryModel.setUid(userInfo.getUid());
            queryModel.setPayState(PayStateEnum.SUCCESS.getCode());
            
            packageId = carRentalPackageOrderPO.getRentalPackageId();
        }
        
        // 获取购买的套餐绑定的所有活动
        List<Long> activityIdsByPackage = invitationActivityMemberCardService.selectActivityIdByPackageIdAndPackageType(packageId, packageType);
        if (CollectionUtils.isEmpty(activityIdsByPackage)) {
            log.info("Invitation activity info! package not bound to any activity, package type = {}, package Id={},uid={}", packageType, packageId, userInfo.getUid());
            return;
        }
        
        // 获取历史记录和本次参与活动的交集
        Map<Long, InvitationActivityJoinHistory> intersectionActivityMap = activityJoinHistoryList.stream().filter(item -> activityIdsByPackage.contains(item.getActivityId()))
                .collect(Collectors.toMap(InvitationActivityJoinHistory::getActivityId, item -> item));
        
        // 由于规则:参与的所有活动下的套餐不会重复，所以上述交集元素唯一
        if (NumberUtil.equals(intersectionActivityMap.size(), NumberConstant.ZERO)) {
            log.info("Invitation activity info! package not bound to activity, package type = {}, package Id={},uid={}", packageType, packageId, userInfo.getUid());
            return;
        }
        
        intersectionActivityMap.entrySet().stream().findFirst().ifPresent(entry -> {
            //本次购买套餐的活动
            Long activityId = entry.getKey();
            InvitationActivityJoinHistory activityJoinHistory = entry.getValue();
            
            //获取邀请人绑定的所有活动，判断当前活动是否已解绑，如果解绑，直接返回
            List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(activityJoinHistory.getUid());
            Set<Long> activityIdUserSet = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toSet());
            if (!activityIdUserSet.contains(activityId)) {
                log.info("Invitation activity info! the activity is bound from inviter, inviter uid={}", activityJoinHistory.getUid());
                return;
            }
            
            // 获取购买套餐的活动
            InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(activityId);
            
            //返现金额
            BigDecimal rewardAmount;
            
            //首次购买套餐
            if (NumberUtil.equals(payCount, NumberConstant.ONE)) {
                //首次购买需要判断活动是否过期
                if (activityJoinHistory.getExpiredTime() < System.currentTimeMillis()) {
                    log.warn("Invitation activity error! activity already sold out,activityId={},uid={}", activityJoinHistory.getActivityId(), userInfo.getUid());
                    return;
                }
                
                log.info("handle invitation activity for first purchase package. join record id = {}, join uid = {}, invitor uid = {}", activityJoinHistory.getRecordId(),
                        activityJoinHistory.getJoinUid(), activityJoinHistory.getUid());
                rewardAmount = invitationActivity.getFirstReward();
                //修改参与状态
                InvitationActivityJoinHistory activityJoinHistoryUpdate = new InvitationActivityJoinHistory();
                activityJoinHistoryUpdate.setId(activityJoinHistory.getId());
                activityJoinHistoryUpdate.setStatus(InvitationActivityJoinHistory.STATUS_SUCCESS);
                activityJoinHistoryUpdate.setMoney(rewardAmount);
                activityJoinHistoryUpdate.setPayCount(payCount);
                activityJoinHistoryUpdate.setPackageId(packageId);
                activityJoinHistoryUpdate.setPackageType(packageType);
                activityJoinHistoryUpdate.setUpdateTime(System.currentTimeMillis());
                invitationActivityJoinHistoryService.update(activityJoinHistoryUpdate);
                
                //给邀请人增加邀请成功人数及返现金额
                this.addCountAndMoneyByUid(rewardAmount, activityJoinHistory.getRecordId());
                
                //修改会员扩展表活动类型
                userInfoExtraService.updateByUid(
                        UserInfoExtra.builder().uid(activityJoinHistory.getJoinUid()).activitySource(UserInfoActivitySourceEnum.SUCCESS_INVITATION_ACTIVITY.getCode())
                                .inviterUid(activityJoinHistory.getUid()).build());
            } else {
                //非首次购买需要判断 首次购买是否成功（同一个邀请人下 所有活动的首次）
                if (!activityJoinHistoryStatusSet.contains(InvitationActivityJoinHistory.STATUS_SUCCESS)) {
                    log.warn("Invitation activity error! Unsuccessful join renewal activity, activity join fail,activityHistoryId={},uid={}", activityJoinHistory.getId(),
                            userInfo.getUid());
                    return;
                }
                
                // 如果该参与人有修改邀请人的记录（status=2 and del_flag=1），则其参与的套餐返现的所有活动都不再对原邀请人进行返利
                InvitationActivityJoinHistory modifyInviterHistory = invitationActivityJoinHistoryService.queryModifiedInviterHistory(activityJoinHistory.getJoinUid(), tenantId);
                if (Objects.nonNull(modifyInviterHistory)) {
                    log.warn("Invitation activity error! inviter has been modified,activityHistoryId={},uid={}, inviter uid = {}", activityJoinHistory.getId(), userInfo.getUid(),
                            activityJoinHistory.getUid());
                    return;
                }
                
                log.info("handle invitation activity for renewal package. join record id = {}, join uid = {}, inviter uid = {}", activityJoinHistory.getRecordId(),
                        activityJoinHistory.getJoinUid(), activityJoinHistory.getUid());
                
                rewardAmount = invitationActivity.getOtherReward();
                
                // 保存参与记录，判断非首次购买有没有已参与状态的历史记录，有-更新，没有-新增
                InvitationActivityJoinHistory existHistory = invitationActivityJoinHistoryService.queryByJoinUidAndActivityId(activityJoinHistory.getJoinUid(),
                        activityJoinHistory.getActivityId());
                InvitationActivityJoinHistory insertOrUpdateHistory = new InvitationActivityJoinHistory();
                insertOrUpdateHistory.setStatus(InvitationActivityJoinHistory.STATUS_SUCCESS);
                insertOrUpdateHistory.setMoney(rewardAmount);
                insertOrUpdateHistory.setPayCount(payCount);
                insertOrUpdateHistory.setPackageId(packageId);
                insertOrUpdateHistory.setPackageType(packageType);
                
                if (Objects.nonNull(existHistory)) {
                    insertOrUpdateHistory.setId(existHistory.getId());
                    insertOrUpdateHistory.setUpdateTime(System.currentTimeMillis());
                    invitationActivityJoinHistoryService.update(insertOrUpdateHistory);
                } else {
                    insertOrUpdateHistory.setUid(activityJoinHistory.getUid());
                    insertOrUpdateHistory.setRecordId(activityJoinHistory.getRecordId());
                    insertOrUpdateHistory.setJoinUid(activityJoinHistory.getJoinUid());
                    insertOrUpdateHistory.setStartTime(activityJoinHistory.getStartTime());
                    insertOrUpdateHistory.setExpiredTime(activityJoinHistory.getExpiredTime());
                    insertOrUpdateHistory.setActivityId(activityJoinHistory.getActivityId());
                    insertOrUpdateHistory.setTenantId(userInfo.getTenantId());
                    insertOrUpdateHistory.setCreateTime(System.currentTimeMillis());
                    insertOrUpdateHistory.setUpdateTime(System.currentTimeMillis());
                    
                    Long activityFranchiseeId = invitationActivity.getFranchiseeId();
                    if (Objects.nonNull(activityFranchiseeId)) {
                        insertOrUpdateHistory.setFranchiseeId(activityFranchiseeId);
                    }
                    
                    invitationActivityJoinHistoryService.insert(insertOrUpdateHistory);
                }
                
                //给邀请人增加返现金额
                this.addMoneyByRecordId(rewardAmount, activityJoinHistory.getRecordId());
            }
            //处理返现
            if (!BigDecimal.ZERO.equals(rewardAmount)) {
                userAmountService.handleInvitationActivityAmount(userInfo, activityJoinHistory.getUid(), rewardAmount);
            }
            log.info("handle invitation activity for package end. join record id = {}, join uid = {}, invitor uid = {}", activityJoinHistory.getRecordId(),
                    activityJoinHistory.getJoinUid(), activityJoinHistory.getUid());
        });
        
    }
    
    private static String codeEnCoder(String activityIds, Long uid) {
        String encrypt = AESUtils.encrypt(activityIds + ":" + uid);
        
        if (StringUtils.isNotBlank(encrypt)) {
            Base64.Encoder encoder = Base64.getUrlEncoder();
            byte[] base64Result = encoder.encode(encrypt.getBytes());
            return new String(base64Result);
        }
        return null;
    }
    
    private static String codeDeCoder(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        
        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] decode = decoder.decode(code.getBytes());
        String base64Result = new String(decode);
        
        if (StringUtils.isNotBlank(base64Result)) {
            return AESUtils.decrypt(base64Result);
        }
        return null;
    }
}
