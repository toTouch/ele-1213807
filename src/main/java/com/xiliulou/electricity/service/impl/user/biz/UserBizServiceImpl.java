package com.xiliulou.electricity.service.impl.user.biz;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户业务聚合 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class UserBizServiceImpl implements UserBizService {

    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    @Autowired
    ShareActivityMemberCardService shareActivityMemberCardService;
    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    @Autowired
    ShareActivityRecordService shareActivityRecordService;
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    @Autowired
    ShareMoneyActivityService shareMoneyActivityService;
    @Autowired
    UserAmountService userAmountService;
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    @Autowired
    ShareMoneyActivityPackageService shareMoneyActivityPackageService;
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;


    /**
     * 退押解绑用户信息
     *
     * @param uid  用户ID
     * @param type 操作类型：0-退电、1-退车、2-退车电
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean depositRefundUnbind(Long uid, Integer type) {
        // TODO 实现
        return false;
    }

    /**
     * 是否是老用户<br />
     * 判定规则：用户是否购买成功过租车套餐 or 换电套餐
     * <pre>
     *     true-老用户
     *     false-新用户
     * </pre>
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Override
    public Boolean isOldUser(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询租车套餐购买成功记录
        CarRentalPackageOrderQryModel queryModel = new CarRentalPackageOrderQryModel();
        queryModel.setTenantId(tenantId);
        queryModel.setUid(uid);
        queryModel.setPayState(PayStateEnum.SUCCESS.getCode());
        Integer count = carRentalPackageOrderService.count(queryModel);
        if (count.intValue() > 0) {
            return true;
        }

        // 查询换电套餐购买记录
        Integer num = electricityMemberCardOrderService.selectCountByUid(tenantId, uid, ElectricityMemberCardOrder.STATUS_SUCCESS);
        if (num.intValue() > 0) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinShareActivityProcess(Long joinUid, Long packageId) {
        JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(joinUid);
        try{
            //是否有人邀请
            if (Objects.nonNull(joinShareActivityRecord)) {
                log.info("share activity process start, join uid = {}, package id = {}", joinUid, packageId);
                //是否购买的是活动指定的套餐
                List<Long> memberCardIds = shareActivityMemberCardService.selectMemberCardIdsByActivityId(joinShareActivityRecord.getActivityId());
                if (CollectionUtils.isNotEmpty(memberCardIds) && memberCardIds.contains(packageId)) {
                    //修改邀请状态
                    joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                    joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareActivityRecordService.update(joinShareActivityRecord);

                    //修改历史记录状态
                    JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(joinShareActivityRecord.getId(), joinUid);
                    if (Objects.nonNull(oldJoinShareActivityHistory)) {
                        oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                        oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                    }

                    //给邀请人增加邀请成功人数
                    shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid(), joinShareActivityRecord.getActivityId());
                } else {
                    log.info("share activity, invite fail, activityId = {},memberCardId = {},memberCardIds = {}", joinShareActivityRecord.getActivityId(), packageId, JsonUtil.toJson(memberCardIds));
                }

            }
        }catch (Exception e){
            log.error("share activity process issue, join uid = {}, packageId = {}", joinUid, packageId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinShareMoneyActivityProcess(Long joinUid, Long packageId, Integer tenantId) {
        try{
            //是否有人返现邀请
            JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(joinUid);
            if (Objects.nonNull(joinShareMoneyActivityRecord)) {
                log.info("share money activity process start, join uid = {}, package id = {}, tenant id = {}", joinUid, packageId, tenantId);
                //检查当前购买的套餐是否属于活动指定的套餐
                List<ShareMoneyActivityPackage> shareMoneyActivityPackages = shareMoneyActivityPackageService.findActivityPackagesByActivityId(joinShareMoneyActivityRecord.getActivityId().longValue());
                List<Long> packageIds = shareMoneyActivityPackages.stream().map(ShareMoneyActivityPackage::getPackageId).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(packageIds) && packageIds.contains(packageId)){
                    //修改邀请状态
                    joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                    joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);

                    //修改历史记录状态
                    JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndJoinUid(joinShareMoneyActivityRecord.getId(), joinUid);
                    if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
                        oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_SUCCESS);
                        oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
                    }

                    ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());

                    if (Objects.nonNull(shareMoneyActivity)) {
                        //给邀请人增加邀请成功人数
                        shareMoneyActivityRecordService.addCountByUid(joinShareMoneyActivityRecord.getUid(), shareMoneyActivity.getMoney());
                    }

                    //返现
                    userAmountService.handleAmount(joinShareMoneyActivityRecord.getUid(), joinShareMoneyActivityRecord.getJoinUid(), shareMoneyActivity.getMoney(), tenantId);

                } else {
                    log.info("share money activity, invite fail, activityId = {},memberCardId = {}, memberCardIds = {}", joinShareMoneyActivityRecord.getActivityId(), packageId, JsonUtil.toJson(packageIds));
                }

            }
        }catch (Exception e){
            log.error("share money activity process issue, uid = {}, packageId = {}", joinUid, packageId, e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinChannelActivityProcess(Long joinUid) {
        //如果后台有记录那么一定是用户没购买过套餐时添加，如果为INIT就修改
        try{
            log.info("join channel activity process start, uid = {}", joinUid);
            ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(joinUid);
            if (Objects.nonNull(channelActivityHistory) && Objects
                    .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
                ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
                updateChannelActivityHistory.setId(channelActivityHistory.getId());
                updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
                updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
                channelActivityHistoryService.update(updateChannelActivityHistory);
            }
        }catch (Exception e){
            log.error("join channel activity process issue, uid = {}", joinUid, e);
        }
    }
}
