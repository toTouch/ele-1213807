package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.mapper.InvitationActivityRecordMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityCodeVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordInfoVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private UserBatteryMemberCardService userBatteryMemberCardService;

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

    @Override
    public List<InvitationActivityRecordVO> selectByPage(InvitationActivityRecordQuery query) {
        List<InvitationActivityRecordVO> list = invitationActivityRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.parallelStream().peek(item -> {

            InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(item.getActivityId());
            item.setActivityName(Objects.isNull(invitationActivity) ? StringUtils.EMPTY : invitationActivity.getName());

        }).collect(Collectors.toList());
    }

    @Override
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
    public InvitationActivityRecord selectByUid(Long uid) {
        return this.invitationActivityRecordMapper.selectByUid(uid);
    }

    @Override
    public Triple<Boolean, String, Object> selectUserInvitationDetail() {
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("INVITATION ACTIVITY ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
    
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("INVITATION ACTIVITY ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
    
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("INVITATION ACTIVITY ERROR! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
    
        //获取当前用户所绑定的套餐返现活动
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            log.warn("INVITATION ACTIVITY WARN! not found invitationActivityUserList,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
    
        List<Long> activityIds = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toList());
    
        List<InvitationActivityRecord> activityRecords = this.selectByActivityIdAndUid(activityIds, userInfo.getUid());
        if (CollectionUtils.isEmpty(activityRecords)) {
            log.warn("INVITATION ACTIVITY WARN! not found activityRecords,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
    
        List<InvitationActivityRecordInfoVO> collect = activityRecords.stream().map(item -> {
            InvitationActivityRecordInfoVO invitationActivityRecordInfoVO = new InvitationActivityRecordInfoVO();
            BeanUtils.copyProperties(item, invitationActivityRecordInfoVO);
            return invitationActivityRecordInfoVO;
        }).collect(Collectors.toList());
    
        return Triple.of(true, null, collect);
    }

    @Override
    public Triple<Boolean, String, Object> generateCode() {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "100001", "用户不存在");
        }

        List<InvitationActivity> invitationActivities = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivities)) {
            log.error("INVITATION ACTIVITY ERROR! invitationActivities is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "100391", "暂无上架的活动");
        }

        List<Long> activityIds = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toList());

        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            log.error("INVITATION ACTIVITY ERROR! invitationActivityUserList is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "100392", "无权限参加此活动");
        }
    
        // todo
        List<Long> activityUserIds = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(activityUserIds) || !new HashSet<>(activityIds).containsAll(activityUserIds)) {
            log.error("INVITATION ACTIVITY ERROR! activityUserIds is empty or activityUserIds is invalid,uid={}", userInfo.getUid());
            return Triple.of(false, "100392", "无权限参加此活动");
        }

        // todo 提前
        if (StringUtils.isBlank(userInfo.getPhone())) {
            log.error("INVITATION ACTIVITY ERROR! phone is null,uid={}", userInfo.getUid());
            return Triple.of(false, "000001", "系统异常");
        }

        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant) || StringUtils.isBlank(tenant.getCode())) {
            log.error("INVITATION ACTIVITY ERROR! tenant is null,uid={}", userInfo.getUid());
            return Triple.of(false, "000001", "系统异常");
        }
    
        // todo
        StringBuilder activityIdsSb = new StringBuilder();
        invitationActivityUserList.forEach(item -> {
            activityIdsSb.append(item.getActivityId()).append(StrUtil.COMMA);
        
            InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(
                    new LambdaQueryWrapper<InvitationActivityRecord>().eq(InvitationActivityRecord::getUid, userInfo.getUid())
                            .eq(InvitationActivityRecord::getActivityId, item.getActivityId()));
            // todo 批量
            if (Objects.isNull(invitationActivityRecord)) {
                //第一次分享  生成分享记录
                InvitationActivityRecord invitationActivityRecordInsert = new InvitationActivityRecord();
                invitationActivityRecordInsert.setActivityId(item.getActivityId());
                invitationActivityRecordInsert.setUid(userInfo.getUid());
                invitationActivityRecordInsert.setCode(RandomUtil.randomNumbers(NumberConstant.SIX));
                invitationActivityRecordInsert.setShareCount(NumberConstant.ZERO);
                invitationActivityRecordInsert.setInvitationCount(NumberConstant.ZERO);
                invitationActivityRecordInsert.setMoney(BigDecimal.ZERO);
                invitationActivityRecordInsert.setTenantId(TenantContextHolder.getTenantId());
                invitationActivityRecordInsert.setStatus(InvitationActivityRecord.STATUS_SUCCESS);
                invitationActivityRecordInsert.setCreateTime(System.currentTimeMillis());
                invitationActivityRecordInsert.setUpdateTime(System.currentTimeMillis());
            
                invitationActivityRecordMapper.insertOne(invitationActivityRecordInsert);
            }
        });
    
        // 多个activityId用逗号分割
        String str = activityIdsSb.toString();
        String activityIdsStr = StringUtils.EMPTY;
        if(str.contains(StrUtil.COMMA)) {
            activityIdsStr = str.substring(NumberConstant.ZERO, str.lastIndexOf(StrUtil.COMMA));
        }
        
        InvitationActivityCodeVO invitationActivityCodeVO = new InvitationActivityCodeVO();
        invitationActivityCodeVO.setCode(codeEnCoder(activityIdsStr, userInfo.getUid()));
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
            log.error("INVITATION ACTIVITY ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
    
        //3.0 判断用户是否购买套餐(包含换电, 租车, 车电一体套餐)
        if (userInfo.getPayCount() > NumberConstant.ZERO) {
            log.info("Exist package pay count for current user, uid = {}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
    
        String decrypt = null;
        try {
            decrypt = codeDeCoder(query.getCode());
        } catch (Exception e) {
            log.error("INVITATION ACTIVITY ERROR! decode fail,uid={},code={}", SecurityUtils.getUid(), query.getCode());
        }
    
        if (StringUtils.isBlank(decrypt)) {
            log.error("INVITATION ACTIVITY ERROR! invitation activity code decrypt error,code={}, uid={}", query.getCode(), userInfo.getUid());
            return Triple.of(false, "100457", "活动二维码解析失败");
        }
    
        String[] split = decrypt.split(String.valueOf(StrUtil.C_COLON));
        if (ArrayUtils.isEmpty(split) || split.length != NumberConstant.TWO) {
            log.error("INVITATION ACTIVITY ERROR! illegal code! code={}, uid={}", query.getCode(), userInfo.getUid());
            return Triple.of(false, "100459", "活动二维码内容不合法");
        }
    
        String activityIdStr = split[NumberConstant.ZERO];
        if (StringUtils.isEmpty(activityIdStr)) {
            log.error("INVITATION ACTIVITY ERROR! not found invitationActivity, uid={}", userInfo.getUid());
            return Triple.of(false, "100450", "渠道活动不存在");
        }
    
        Long invitationUid = Long.parseLong(split[NumberConstant.ONE]);
        if (Objects.equals(userInfo.getUid(), invitationUid)) {
            log.info("INVITATION ACTIVITY INFO! illegal operate! invitationUid={}, uid={}", invitationUid, userInfo.getUid());
            return Triple.of(true, null, null);
        }
    
        UserInfo invitationUserInfo = userInfoService.queryByUidFromCache(invitationUid);
        if (Objects.isNull(invitationUserInfo)) {
            log.error("INVITATION ACTIVITY ERROR! not found invitationUserInfo,invitationUid={},uid={}", invitationUid, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
    
        // 活动id集合
        List<Long> activityIdList = Arrays.stream(activityIdStr.split(String.valueOf(StrUtil.C_COMMA))).map(Long::valueOf).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(activityIdList)) {
            log.error("INVITATION ACTIVITY ERROR!  not found valid activity, invitationUid={}, uid={}", invitationUid, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00106", "活动已下架");
        }
    
        for (Long activityId : activityIdList) {
            InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(activityId);
            if (Objects.isNull(invitationActivity) || !Objects.equals(invitationActivity.getStatus(), InvitationActivity.STATUS_UP)) {
                log.error("INVITATION ACTIVITY ERROR! invitationActivity disable,activityId={}, uid={}", activityId, userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00106", "活动已下架");
            }
        
            // 获取活动记录
            InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(
                    new LambdaQueryWrapper<InvitationActivityRecord>().eq(InvitationActivityRecord::getUid, invitationUid).eq(InvitationActivityRecord::getActivityId, activityId));
            if (Objects.isNull(invitationActivityRecord) || Objects.isNull(invitationActivityRecord.getActivityId())) {
                log.error("INVITATION ACTIVITY ERROR!  invitationActivityRecord is null, invitationUid={}, uid={}", invitationUid, userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00106", "活动已下架");
            }
        
            //用户是否已参与过此活动
            Integer exist = invitationActivityJoinHistoryService.existsByJoinUidAndActivityId(userInfo.getUid(), activityId);
            if (Objects.nonNull(exist)) {
                log.error("INVITATION ACTIVITY ERROR! user already join invitation activity,activityId={},uid={}", invitationActivity.getId(), userInfo.getUid());
                return Triple.of(true, "ELECTRICITY.00107", "已参加过活动");
            }
        
            //保存活动参与记录
            InvitationActivityJoinHistory invitationActivityJoinHistoryInsert = new InvitationActivityJoinHistory();
            invitationActivityJoinHistoryInsert.setUid(invitationUid);
            invitationActivityJoinHistoryInsert.setJoinUid(userInfo.getUid());
            invitationActivityJoinHistoryInsert.setActivityId(activityId);
            invitationActivityJoinHistoryInsert.setRecordId(invitationActivityRecord.getId());
            invitationActivityJoinHistoryInsert.setStatus(InvitationActivityJoinHistory.STATUS_INIT);
            invitationActivityJoinHistoryInsert.setStartTime(System.currentTimeMillis());
            invitationActivityJoinHistoryInsert.setExpiredTime(System.currentTimeMillis() + invitationActivity.getHours() * TimeConstant.HOURS_MILLISECOND);
            invitationActivityJoinHistoryInsert.setTenantId(TenantContextHolder.getTenantId());
            invitationActivityJoinHistoryInsert.setCreateTime(System.currentTimeMillis());
            invitationActivityJoinHistoryInsert.setUpdateTime(System.currentTimeMillis());
        
            invitationActivityJoinHistoryService.insert(invitationActivityJoinHistoryInsert);
        
            //更新活动邀请总人数
            invitationActivityRecordMapper.addShareCount(invitationActivityRecord.getId());
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
            log.error("INVITATION ACTIVITY ERROR!not found invitationActivity,uid={},activityId={}", userInfo.getUid(), invitationActivity.getId());
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
        if (CollectionUtils.isEmpty(memberCardIds) || !memberCardIds.contains(electricityMemberCardOrder.getMemberCardId().longValue())) {
            log.info("INVITATION ACTIVITY INFO!invite fail,activityId={},membercardId={},uid={}", activityJoinHistory.getActivityId(), electricityMemberCardOrder.getMemberCardId(), userInfo.getUid());
            return;
        }

        //返现金额
        BigDecimal rewardAmount = null;

        //首次购买套餐
        if (electricityMemberCardOrder.getPayCount() == 1) {
            //首次购买需要判断活动是否过期
            if (activityJoinHistory.getExpiredTime() < System.currentTimeMillis()) {
                log.error("INVITATION ACTIVITY INFO!activity already sold out,activityId={},uid={}", activityJoinHistory.getActivityId(), userInfo.getUid());
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
                log.error("INVITATION ACTIVITY INFO!activity join fail,activityHistoryId={},uid={}", activityJoinHistory.getId(), userInfo.getUid());
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

        // 获取租户下所有上架的套餐返现活动
        List<InvitationActivity> invitationActivities = invitationActivityService.selectUsableActivity(userInfo.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivities)) {
            log.info("Invitation activity info! invitationActivities is empty,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return;
        }
    
        // 过滤掉未上架的活动
        Set<Long> activityIdsSet = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toSet());
        activityJoinHistoryList = activityJoinHistoryList.stream().filter(history -> !activityIdsSet.contains(history.getActivityId())).collect(Collectors.toList());
    
        //增加换电套餐和租车及车电一体套餐的判断逻辑
        Long packageId;
        Integer payCount = userInfo.getPayCount();
        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)){
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromDB(userInfo.getUid());

            if (Objects.isNull(userBatteryMemberCard)) {
                log.info("Invitation activity info, not found user battery package ,orderId={},uid={}", orderNo, userInfo.getUid());
                return;
            }
            packageId = userBatteryMemberCard.getMemberCardId();
        }else{
            //获取租车或者车电一体订单信息
            CarRentalPackageOrderPo carRentalPackageOrderPO = carRentalPackageOrderService.selectByOrderNo(orderNo);
            if(Objects.isNull(carRentalPackageOrderPO)){
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
        if(CollectionUtils.isEmpty(activityIdsByPackage)) {
            log.info("Invitation activity info! package not bound to any activity, package type = {}, package Id={},uid={}", packageType, packageId, userInfo.getUid());
            return;
        }
        
        // 由于规则:参与的所有活动下的套餐不会重复，所以查询套餐对应的活动唯一
        InvitationActivityJoinHistory activityJoinHistory = null;
        Long activityId = null;
        for (Long activityIdByPackage : activityIdsByPackage) {
            for (InvitationActivityJoinHistory history : activityJoinHistoryList) {
                    if(history.getActivityId().equals(activityIdByPackage)){
                        activityJoinHistory = history;
                        activityId = history.getActivityId();
                        break;
                    }
            }
        }
        
        if(Objects.isNull(activityJoinHistory) || Objects.isNull(activityId)) {
            log.info("Invitation activity info! package not bound to activity, package type = {}, package Id={},uid={}", packageType, packageId, userInfo.getUid());
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
                log.error("Invitation activity error! activity already sold out,activityId={},uid={}", activityJoinHistory.getActivityId(), userInfo.getUid());
                return;
            }

            log.info("handle invitation activity for first purchase package. join record id = {}, join uid = {}, invitor uid = {}", activityJoinHistory.getRecordId(), activityJoinHistory.getJoinUid(), activityJoinHistory.getUid());
            rewardAmount = invitationActivity.getFirstReward();
            //修改参与状态
            InvitationActivityJoinHistory activityJoinHistoryUpdate = new InvitationActivityJoinHistory();
            activityJoinHistoryUpdate.setId(activityJoinHistory.getId());
            activityJoinHistoryUpdate.setStatus(InvitationActivityJoinHistory.STATUS_SUCCESS);
            activityJoinHistoryUpdate.setMoney(rewardAmount);
            activityJoinHistoryUpdate.setPayCount(payCount);
            activityJoinHistoryUpdate.setUpdateTime(System.currentTimeMillis());
            invitationActivityJoinHistoryService.update(activityJoinHistoryUpdate);

            //给邀请人增加邀请成功人数及返现金额
            this.addCountAndMoneyByUid(rewardAmount, activityJoinHistory.getRecordId());
        } else {
            //非首次购买需要判断 首次购买是否成功
            if (!Objects.equals(activityJoinHistory.getStatus(), InvitationActivityJoinHistory.STATUS_SUCCESS)) {
                log.error("Invitation activity error! Unsuccessful join the first activity, activity join fail,activityHistoryId={},uid={}", activityJoinHistory.getId(), userInfo.getUid());
                return;
            }

            log.info("handle invitation activity for renewal package. join record id = {}, join uid = {}, invitor uid = {}", activityJoinHistory.getRecordId(), activityJoinHistory.getJoinUid(), activityJoinHistory.getUid());
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
            activityJoinHistoryInsert.setPayCount(payCount);
            activityJoinHistoryInsert.setMoney(rewardAmount);
            activityJoinHistoryInsert.setTenantId(userInfo.getTenantId());
            activityJoinHistoryInsert.setCreateTime(System.currentTimeMillis());
            activityJoinHistoryInsert.setUpdateTime(System.currentTimeMillis());
            invitationActivityJoinHistoryService.insert(activityJoinHistoryInsert);

            //给邀请人增加返现金额
            this.addMoneyByRecordId(rewardAmount, activityJoinHistory.getRecordId());

        }

        //处理返现
        userAmountService.handleInvitationActivityAmount(userInfo, activityJoinHistory.getUid(), rewardAmount);
        log.info("handle invitation activity for package end. join record id = {}, join uid = {}, invitor uid = {}", activityJoinHistory.getRecordId(), activityJoinHistory.getJoinUid(), activityJoinHistory.getUid());

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
