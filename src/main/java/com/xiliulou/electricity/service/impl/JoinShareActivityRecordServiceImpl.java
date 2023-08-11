package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.JoinShareActivityRecordMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
            log.error("joinActivity  ERROR! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //查找活动
        ShareActivity shareActivity = shareActivityService.queryByStatus(activityId);
        if (Objects.isNull(shareActivity)) {
            log.error("joinActivity  ERROR! not found Activity ! ActivityId:{} ", activityId);
            return R.fail("ELECTRICITY.00106", "活动已下架");
        }

        //查找分享的用户
        User oldUser = userService.queryByUidFromCache(uid);
        if (Objects.isNull(oldUser)) {
            log.error("joinActivity  ERROR! not found oldUser ,uid :{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //1、自己点自己的链接，则返回自己该活动的参与人数及领劵规则
        if (Objects.equals(uid, user.getUid())) {
            return R.ok();
        }

        //判断是否为重复扫邀请人的码
        Boolean isJoinedActivityFromSameInviter = joinShareActivityHistoryService.checkTheActivityFromSameInviter(user.getUid(), oldUser.getUid(), activityId.longValue());
        if(isJoinedActivityFromSameInviter){
            return R.ok();
        }

        log.info("start join share activity, join uid = {}, inviter uid = {}, activity id = {}", user.getUid(), oldUser.getUid(), activityId);

        //2、别人点击链接登录

        //2.1 判断此人是否首次购买月卡,已购买月卡,则直接返回首页
        //3.0扩展了活动范围，扩展到登录注册，实名认证及购买套餐。所以不需要再判断是否购买过套餐
        /*if (Boolean.TRUE.equals(checkUserIsCard(userInfo))) {
            return R.ok();
        }*/

        //3.0修改为用户只能参加邀请返券或者邀请返现其中一种活动。如果已经参与了，则提示已参加对应活动。不允许参加多个活动。如果邀请活动未完成，但是已过期或者下架了。则还可以正常参加。
        //1. 查看当前用户是否存在正在参加或者已成功参加的活动，如果是，则提示已参加过邀请活动。
        //2. 若以上都没有参与过，则查看是否存在邀请返现的活动，判断规则和1一致。
        List<JoinShareActivityHistory> joinShareActivityHistories = joinShareActivityHistoryService.queryUserJoinedActivity(user.getUid(), tenantId);
        if(CollectionUtils.isNotEmpty(joinShareActivityHistories)){
            return R.fail("000106", "已参加过邀请返券活动");
        }

        //查询当前用户是否参与了邀请返现活动
        List<JoinShareMoneyActivityHistory> joinShareMoneyActivityHistories = joinShareMoneyActivityHistoryService.queryUserJoinedActivity(user.getUid(), tenantId);
        if(CollectionUtils.isNotEmpty(joinShareMoneyActivityHistories)){
            return R.fail("000107", "已参加过邀请返现活动");
        }

        //未购买月卡则添加用户参与记录
        //2.2 判断此人是否参与过活动
        //TODO 待删除。 3.0版本后活动不能重复参加，所以如下代码被注释
        /*JoinShareActivityRecord oldJoinShareActivityRecord = joinShareActivityRecordMapper.selectOne(new LambdaQueryWrapper<JoinShareActivityRecord>()
                .eq(JoinShareActivityRecord::getJoinUid, user.getUid()).eq(JoinShareActivityRecord::getTenantId, tenantId)
                .eq(JoinShareActivityRecord::getActivityId, activityId)
                .in(JoinShareActivityRecord::getStatus, JoinShareActivityRecord.STATUS_INIT));

        if (Objects.nonNull(oldJoinShareActivityRecord)) {
            if (Objects.equals(oldJoinShareActivityRecord.getUid(), uid)) {
                return R.ok();
            }
            //切换邀请用户
            oldJoinShareActivityRecord.setUid(uid);
            //过期时间可配置
            oldJoinShareActivityRecord.setStartTime(System.currentTimeMillis());
            oldJoinShareActivityRecord.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
            oldJoinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
            joinShareActivityRecordMapper.updateById(oldJoinShareActivityRecord);

            //修改被替换掉的历史记录状态
            JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(oldJoinShareActivityRecord.getId(), user.getUid());
            if (Objects.nonNull(oldJoinShareActivityHistory)) {
                oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_REPLACE);
                oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
            }

            //新增邀请历史记录
            JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
            joinShareActivityHistory.setRecordId(oldJoinShareActivityRecord.getId());
            joinShareActivityHistory.setUid(uid);
            joinShareActivityHistory.setJoinUid(user.getUid());
            joinShareActivityHistory.setCreateTime(System.currentTimeMillis());
            joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
            joinShareActivityHistory.setStartTime(System.currentTimeMillis());
            joinShareActivityHistory.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
            joinShareActivityHistory.setTenantId(tenantId);
            joinShareActivityHistory.setActivityId(oldJoinShareActivityRecord.getActivityId());
            joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_INIT);
            joinShareActivityHistoryService.insert(joinShareActivityHistory);
            return R.ok();
        }*/

        JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
        joinShareActivityRecord.setUid(uid);
        joinShareActivityRecord.setJoinUid(user.getUid());
        joinShareActivityRecord.setCreateTime(System.currentTimeMillis());
        joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
        joinShareActivityRecord.setStartTime(System.currentTimeMillis());
        joinShareActivityRecord.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
        joinShareActivityRecord.setTenantId(tenantId);
        joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_INIT);
        joinShareActivityRecord.setActivityId(activityId);
        joinShareActivityRecordMapper.insert(joinShareActivityRecord);

        //新增邀请历史记录
        JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
        joinShareActivityHistory.setRecordId(joinShareActivityRecord.getId());
        joinShareActivityHistory.setUid(uid);
        joinShareActivityHistory.setJoinUid(user.getUid());
        joinShareActivityHistory.setCreateTime(System.currentTimeMillis());
        joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
        joinShareActivityHistory.setStartTime(System.currentTimeMillis());
        joinShareActivityHistory.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
        joinShareActivityHistory.setTenantId(tenantId);
        joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_INIT);
        joinShareActivityHistory.setActivityId(joinShareActivityRecord.getActivityId());
        joinShareActivityHistoryService.insert(joinShareActivityHistory);

        return R.ok();

    }

    @Override
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
}
