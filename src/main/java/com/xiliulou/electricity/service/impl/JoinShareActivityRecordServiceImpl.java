package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.mapper.JoinShareActivityRecordMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareActivityRecordService")
@Slf4j
public class JoinShareActivityRecordServiceImpl implements JoinShareActivityRecordService {
    @Resource
    private JoinShareActivityRecordMapper joinShareActivityRecordMapper;

    @Autowired
    private JoinShareActivityHistoryService joinShareActivityHistoryService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ShareActivityService shareActivityService;

    @Autowired
    UserService userService;
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    /**
     * 修改数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(JoinShareActivityRecord joinShareActivityRecord) {
        return this.joinShareActivityRecordMapper.updateById(joinShareActivityRecord);

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R joinActivity(Integer activityId, Long uid) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("joinActivity  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
    
        //用户是否可用
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("joinActivity  WARN! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
    
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfoExtra)) {
            log.warn("joinActivity  WARN! not found userInfoExtra,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "未找到用户");
        }
    
        //查找活动
        ShareActivity shareActivity = shareActivityService.queryByStatus(activityId);
        if (Objects.isNull(shareActivity)) {
            log.warn("joinActivity WARN! not found Activity ! ActivityId:{} ", activityId);
            return R.fail("ELECTRICITY.00106", "活动已下架");
        }
        
        //查找分享的用户
        User oldUser = userService.queryByUidFromCache(uid);
        if (Objects.isNull(oldUser)) {
            log.warn("joinActivity  WARN! not found oldUser ,uid :{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        //1、自己点自己的链接，则返回自己该活动的参与人数及领劵规则
        if (Objects.equals(uid, user.getUid())) {
            return R.ok();
        }
    
        // 530活动互斥判断
        R canJoinActivity = merchantJoinRecordService.canJoinActivity(userInfo, userInfoExtra, activityId, UserInfoActivitySourceEnum.SUCCESS_SHARE_ACTIVITY.getCode());
        if (!canJoinActivity.isSuccess()) {
            return canJoinActivity;
        }
    
        // 判断是否已经参与过该活动
        List<JoinShareActivityHistory> joinShareActivityHistories = joinShareActivityHistoryService.queryUserJoinedActivity(user.getUid(), tenantId);
        if (CollectionUtils.isNotEmpty(joinShareActivityHistories)) {
            return R.fail("110206", "已参加过邀请返券活动");
        }
        
        // 计算活动有效期
        long expiredTime;
        if (Objects.nonNull(shareActivity.getHours()) && !Objects.equals(shareActivity.getHours(), NumberConstant.ZERO)) {
            expiredTime = System.currentTimeMillis() + shareActivity.getHours() * TimeConstant.HOURS_MILLISECOND;
        } else {
            Integer minutes = Objects.isNull(shareActivity.getMinutes()) ? NumberConstant.ZERO : shareActivity.getMinutes();
            expiredTime = System.currentTimeMillis() + minutes * TimeConstant.MINUTE_MILLISECOND;
        }
    
        JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
        joinShareActivityRecord.setUid(uid);
        joinShareActivityRecord.setJoinUid(user.getUid());
        joinShareActivityRecord.setCreateTime(System.currentTimeMillis());
        joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
        joinShareActivityRecord.setStartTime(System.currentTimeMillis());
        joinShareActivityRecord.setExpiredTime(expiredTime);
        joinShareActivityRecord.setTenantId(tenantId);
        joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_INIT);
        joinShareActivityRecord.setActivityId(activityId);
    
        //新增邀请历史记录
        JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
        joinShareActivityHistory.setRecordId(joinShareActivityRecord.getId());
        joinShareActivityHistory.setUid(uid);
        joinShareActivityHistory.setJoinUid(user.getUid());
        joinShareActivityHistory.setCreateTime(System.currentTimeMillis());
        joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
        joinShareActivityHistory.setStartTime(System.currentTimeMillis());
        joinShareActivityHistory.setExpiredTime(expiredTime);
        joinShareActivityHistory.setTenantId(tenantId);
        joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_INIT);
        joinShareActivityHistory.setActivityId(joinShareActivityRecord.getActivityId());
    
        Integer activityFranchiseeId = shareActivity.getFranchiseeId();
        if (Objects.nonNull(activityFranchiseeId) && !Objects.equals(activityFranchiseeId, NumberConstant.ZERO)) {
            joinShareActivityRecord.setFranchiseeId(activityFranchiseeId.longValue());
            joinShareActivityHistory.setFranchiseeId(activityFranchiseeId.longValue());
        }
    
        joinShareActivityRecordMapper.insert(joinShareActivityRecord);
        joinShareActivityHistoryService.insert(joinShareActivityHistory);
    
        // 530会员扩展表更新最新参与活动类型
        userInfoExtraService.updateByUid(UserInfoExtra.builder().uid(user.getUid()).latestActivitySource(UserInfoActivitySourceEnum.SUCCESS_SHARE_ACTIVITY.getCode()).build());
    
        return R.ok();
    }
    
    @Override
    @Slave
    public JoinShareActivityRecord queryByJoinUid(Long uid) {
        return joinShareActivityRecordMapper.selectOne(new LambdaQueryWrapper<JoinShareActivityRecord>()
                .eq(JoinShareActivityRecord::getJoinUid, uid).gt(JoinShareActivityRecord::getExpiredTime, System.currentTimeMillis())
                .eq(JoinShareActivityRecord::getStatus, JoinShareActivityRecord.STATUS_INIT));
    }

    @Override
    public void handelJoinShareActivityExpired() {
        //
        JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
        joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_FAIL);
        joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
        joinShareActivityRecordMapper.updateExpired(joinShareActivityRecord);

        JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
        joinShareActivityHistory.setStatus(JoinShareActivityRecord.STATUS_FAIL);
        joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
        joinShareActivityHistoryService.updateExpired(joinShareActivityHistory);

    }

    @Override
    public void updateByActivityId(JoinShareActivityRecord joinShareActivityRecord) {
        joinShareActivityRecordMapper.updateByActivityId(joinShareActivityRecord);
    }

    private Boolean checkUserIsCard(UserInfo userInfo) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() == 0) {
            return Boolean.FALSE;
        }

//            //未找到用户
//        if (Objects.isNull(userBatteryMemberCard)) {
//            return false;
//
//        }
//
//        //用户是否开通月卡
//        if (Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
//                && Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
//            return false;
//        }

        return Boolean.TRUE;
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
    
}
