package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ActivityUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-20-11:32
 */
@Slf4j
@Service("activityService")
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private InvitationActivityUserService invitationActivityUserService;
    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    @Autowired
    UserCouponService userCouponService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    private ElectricityMemberCardOrderService eleMemberCardOrderService;
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
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
    OldUserActivityService oldUserActivityService;
    @Autowired
    UserAmountService userAmountService;
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    @Autowired
    InvitationActivityRecordService invitationActivityRecordService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;
    @Autowired
    ShareMoneyActivityPackageService shareMoneyActivityPackageService;
    @Autowired
    private ShareActivityService shareActivityService;
    @Autowired
    private CarRentalPackageOrderService carRentalPackageOrderService;
    @Resource
    private UserBizService userBizService;

    /**
     * 用户是否有权限参加此活动
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> userActivityInfo() {
        ActivityUserInfoVO activityUserInfoVO = new ActivityUserInfoVO();

        InvitationActivityUser invitationActivityUser = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        activityUserInfoVO.setInvitationActivity(Objects.isNull(invitationActivityUser) ? Boolean.FALSE : Boolean.TRUE);

        return Triple.of(true, "", activityUserInfoVO);
    }

    @Override
    public Triple<Boolean, String, Object> updateCouponByPackage(String orderNo, Integer packageType) {

        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)){
            //获取套餐订单优惠券
            List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(orderNo);
            if(CollectionUtils.isEmpty(userCouponIds)){
                return Triple.of(true, "", null);
            }

            //更新优惠券状态
            if(CollectionUtils.isNotEmpty(userCouponIds)){
                Set<Integer> couponIds=userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_USED,orderNo));
            }

            return Triple.of(true, "", null);
        }else{
            //根据当前租车订单号获取 当前订单所使用的优惠券信息

        }

        return null;
    }

    @Override
    public Triple<Boolean, String, Object> handleActivityByPackage(String orderNo, Integer packageType) {
        log.info("Activity by package flow start, orderNo = {}, package type = {}", orderNo, packageType);

        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)){
            log.info("Activity flow for battery package, orderNo = {}", orderNo);
            ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(orderNo);
            Long uid = electricityMemberCardOrder.getUid();
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);

            if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("handle activity error ! package invalid! uid = {}, order no = {}", uid, orderNo);
                return Triple.of(false, "", "当前套餐已暂停");
            }

            //是否是新用户
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() == 0) {
                //处理邀请活动
                shareActivityProcess(uid, electricityMemberCardOrder.getMemberCardId());

                //处理返现邀请
                shareMoneyActivityProcess(uid, electricityMemberCardOrder.getMemberCardId(), electricityMemberCardOrder.getTenantId());
            }

            invitationPackageAndChannelActivity(uid, electricityMemberCardOrder.getOrderId(), packageType);

        }else{
            //开始处理租车，车电一体购买套餐后的活动
            log.info("Activity flow for car Rental or car with battery package, orderNo = {}", orderNo);
            CarRentalPackageOrderPO carRentalPackageOrderPO = carRentalPackageOrderService.selectByOrderNo(orderNo);
            Long uid = carRentalPackageOrderPO.getUid();
            if(Objects.isNull(carRentalPackageOrderPO)){
                log.error("Activity flow for car Rental or car with battery package error, Not found for car rental package, order number = {}", orderNo);
            }
            Boolean isOldUser = userBizService.isOldUser(carRentalPackageOrderPO.getTenantId(), uid);

            if(!isOldUser){
                //处理邀请活动
                shareActivityProcess(uid, carRentalPackageOrderPO.getRentalPackageId());

                //处理返现邀请
                shareMoneyActivityProcess(uid, carRentalPackageOrderPO.getRentalPackageId(), carRentalPackageOrderPO.getTenantId());
            }

            invitationPackageAndChannelActivity(uid, orderNo, packageType);

        }

        return Triple.of(true, "", null);
    }

    /**
     * 登录注册时 活动处理
     * @param uid
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> handleActivityByLogon(Long uid) {
        log.info("Activity by logon flow start, uid = {}", uid);
        //获取参与邀请活动记录
        JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(uid);

        //是否有人邀请参与该活动
        if(Objects.nonNull(joinShareActivityRecord)){
            //查询对应的活动邀请标准
            ShareActivity shareActivity = shareActivityService.queryByIdFromCache(joinShareActivityRecord.getActivityId());
           //检查该活动是否参与过, 没有参与过，并且活动邀请标准为登录注册，则处理该活动
            if(JoinShareActivityRecord.STATUS_INIT.equals(joinShareActivityRecord.getStatus())
                    && ActivityEnum.INVITATION_CRITERIA_LOGON.equals(shareActivity.getInvitationCriteria())){

                //修改邀请状态
                joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                joinShareActivityRecordService.update(joinShareActivityRecord);

                //修改历史记录状态
                JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(joinShareActivityRecord.getId(), uid);
                if (Objects.nonNull(oldJoinShareActivityHistory)) {
                    oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                    oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                    joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                }

                //给邀请人增加邀请成功人数
                shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid(), joinShareActivityRecord.getActivityId());
            }
        }

        //是否有人返现邀请
        JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(uid);
        if (Objects.nonNull(joinShareMoneyActivityRecord)) {
            ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());
            if(JoinShareMoneyActivityRecord.STATUS_INIT.equals(joinShareMoneyActivityRecord.getStatus())
                    && ActivityEnum.INVITATION_CRITERIA_LOGON.equals(shareMoneyActivity.getInvitationCriteria())){
                //修改邀请状态
                joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);

                //修改历史记录状态
                JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndJoinUid(joinShareMoneyActivityRecord.getId(), uid);
                if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
                    oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_SUCCESS);
                    oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
                }

                //ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());

                if (Objects.nonNull(shareMoneyActivity)) {
                    //给邀请人增加邀请成功人数
                    shareMoneyActivityRecordService.addCountByUid(joinShareMoneyActivityRecord.getUid(), shareMoneyActivity.getMoney());
                }

                //返现
                userAmountService.handleAmount(joinShareMoneyActivityRecord.getUid(), joinShareMoneyActivityRecord.getJoinUid(), shareMoneyActivity.getMoney(), shareMoneyActivity.getTenantId());
            }

        }

        return Triple.of(true, "", null);
    }


    /**
     * 实名认证后的 活动处理
     * @param uid
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> handleActivityByRealName(Long uid) {


        return null;
    }

    /**
     * 处理邀请活动流程
     * @param uid
     * @param packageId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void shareActivityProcess(Long uid, Long packageId){
        JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(uid);
        try{
            //是否有人邀请
            if (Objects.nonNull(joinShareActivityRecord)) {
                //是否购买的是活动指定的套餐
                List<Long> memberCardIds = shareActivityMemberCardService.selectMemberCardIdsByActivityId(joinShareActivityRecord.getActivityId());
                if (CollectionUtils.isNotEmpty(memberCardIds) && memberCardIds.contains(packageId)) {
                    //修改邀请状态
                    joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                    joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareActivityRecordService.update(joinShareActivityRecord);

                    //修改历史记录状态
                    JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(joinShareActivityRecord.getId(), uid);
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
            log.error("share activity process issue, uid = {}, packageId = {}", uid, packageId, e);
        }

    }

    /**
     * 处理邀请返现活动流程
     * @param uid
     * @param packageId
     * @param tenantId
     */
    @Transactional(rollbackFor = Exception.class)
    public void shareMoneyActivityProcess(Long uid, Long packageId, Integer tenantId){
        try{
            //是否有人返现邀请
            JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(uid);
            if (Objects.nonNull(joinShareMoneyActivityRecord)) {

                //检查当前购买的套餐是否属于活动指定的套餐
                List<ShareMoneyActivityPackage> shareMoneyActivityPackages = shareMoneyActivityPackageService.findActivityPackagesByActivityId(joinShareMoneyActivityRecord.getActivityId().longValue());
                List<Long> packageIds = shareMoneyActivityPackages.stream().map(ShareMoneyActivityPackage::getPackageId).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(packageIds) && packageIds.contains(packageId)){
                    //修改邀请状态
                    joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                    joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);

                    //修改历史记录状态
                    JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndJoinUid(joinShareMoneyActivityRecord.getId(), uid);
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
            log.error("share money activity process issue, uid = {}, packageId = {}", uid, packageId, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void invitationPackageAndChannelActivity(Long uid, String orderId, Integer packageType){

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        //处理套餐返现活动
        invitationActivityRecordService.handleInvitationActivityByPackage(userInfo, orderId, packageType);

        //如果后台有记录那么一定是用户没购买过套餐时添加，如果为INIT就修改
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(uid);
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
    }


}
