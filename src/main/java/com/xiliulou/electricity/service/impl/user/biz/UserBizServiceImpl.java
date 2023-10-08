package com.xiliulou.electricity.service.impl.user.biz;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;

    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private UserInfoService userInfoService;

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
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;

    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;

    /**
     * 获取名下的总滞纳金（单电、单车、车电一体）
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return 总金额
     */
    @Override
    public BigDecimal querySlippageTotal(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        BigDecimal batterySlippage = serviceFeeUserInfoService.selectBatteryServiceFeeByUid(uid);

        // 查询车的总滞纳金
        BigDecimal carSlippage = carRenalPackageSlippageBizService.queryCarPackageUnpaidAmountByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(carSlippage)) {
            carSlippage = BigDecimal.ZERO;
        }
        return batterySlippage.add(carSlippage).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 退押解绑用户信息
     * @param tenantId 租户ID
     * @param uid  用户ID
     * @param type 操作类型：0-单电、1-单车、2-车电一体 <br />
     * @return true(成功)、false(失败)
     *
     * @see RentalPackageTypeEnum
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean depositRefundUnbind(Integer tenantId, Long uid, Integer type) {
        if (!ObjectUtils.allNotNull(tenantId, uid, type) || !BasicEnum.isExist(type, RentalPackageTypeEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        if (ObjectUtils.isEmpty(userInfo) || !userInfo.getTenantId().equals(tenantId)) {
            log.error("depositRefundUnbind failed. not found t_user_info or tenantId mismatching. uid is {}, user's tenantId is {}, params tenantId is {}", uid, userInfo.getTenantId(), tenantId);
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 定义数据
        ElectricityCar electricityCarUpdate = null;
        CarLockCtrlHistory carLockCtrlHistory = null;

        // 待更新的数据
        UserInfo userInfoEntity = new UserInfo();
        userInfoEntity.setUid(userInfo.getUid());
        userInfoEntity.setTenantId(tenantId);
        userInfoEntity.setUpdateTime(System.currentTimeMillis());

        if (RentalPackageTypeEnum.CAR.getCode().equals(type)) {
            // 解绑用户车辆
            if (UserInfo.CAR_RENT_STATUS_YES.equals(userInfo.getCarRentStatus())) {
                ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
                electricityCarUpdate = new ElectricityCar();
                electricityCarUpdate.setId(electricityCar.getId());
                electricityCarUpdate.setUid(null);
                electricityCarUpdate.setUserName(null);
                electricityCarUpdate.setUserInfoId(null);
                electricityCarUpdate.setPhone(null);

                // JT808 加锁
                carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo);
            }

            // 设置用户租赁状态
            userInfoEntity.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            userInfoEntity.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
            // 置空加盟商、门店
            if (UserInfo.BATTERY_DEPOSIT_STATUS_NO.equals(userInfo.getBatteryDepositStatus())) {
                userInfoEntity.setFranchiseeId(0L);
                userInfoEntity.setStoreId(0L);
            }
        }

        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(type)) {
            // 解绑用户车辆
            if (UserInfo.CAR_RENT_STATUS_YES.equals(userInfo.getCarRentStatus())) {
                ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
                electricityCarUpdate = new ElectricityCar();
                electricityCarUpdate.setId(electricityCar.getId());
                electricityCarUpdate.setUid(null);
                electricityCarUpdate.setUserName(null);
                electricityCarUpdate.setUserInfoId(null);
                electricityCarUpdate.setPhone(null);

                // JT808 加锁
                carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo);

            }

            // 设置用户租赁状态
            userInfoEntity.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            userInfoEntity.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
            userInfoEntity.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            userInfoEntity.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
            userInfoEntity.setCarBatteryDepositStatus(YesNoEnum.NO.getCode());
            // 置空加盟商、门店
            userInfoEntity.setFranchiseeId(0L);
            userInfoEntity.setStoreId(0L);

        }

        // TODO 志龙检查一下
        // TODO t_franchisee_user_info 这张表还有没有用，需要找志龙确认下
        if (RentalPackageTypeEnum.BATTERY.getCode().equals(type)) {

            // 设置用户租赁状态
            userInfoEntity.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            userInfoEntity.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
            // 置空加盟商、门店
            if (UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus())) {
                userInfoEntity.setFranchiseeId(0L);
                userInfoEntity.setStoreId(0L);
            }
        }

        // 事务处理
        log.info("depositRefundUnbind saveDepositRefundUnbindTx params userInfoEntity is {}, electricityCarUpdate is {}, carLockCtrlHistory is {}",
                JsonUtil.toJson(userInfoEntity), JsonUtil.toJson(electricityCarUpdate), JsonUtil.toJson(carLockCtrlHistory));
        saveDepositRefundUnbindTx(userInfoEntity, electricityCarUpdate, carLockCtrlHistory, type);

        return true;
    }

    /**
     * 退押解绑用户信息事务操作
     * @param userInfoEntity 更新用户实体信息
     * @param electricityCarUpdate 解绑用户车辆信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDepositRefundUnbindTx(UserInfo userInfoEntity, ElectricityCar electricityCarUpdate, CarLockCtrlHistory carLockCtrlHistory, Integer type) {
        // 解绑用户信息
        userInfoService.update(userInfoEntity);
        // 解绑用户车辆
        if (ObjectUtils.isNotEmpty(electricityCarUpdate)) {
            carService.updateCarBindStatusById(electricityCarUpdate);
        }
        // 加解锁记录
        if (ObjectUtils.isNotEmpty(carLockCtrlHistory)) {
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }
        
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(type)) {
            //删除用户绑定的电池型号
            userBatteryTypeService.deleteByUid(userInfoEntity.getUid());
        }
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
        if (count > 0) {
            return true;
        }

        // 查询换电套餐购买记录
        Integer num = electricityMemberCardOrderService.selectCountByUid(tenantId, uid, ElectricityMemberCardOrder.STATUS_SUCCESS);
        if (num> 0) {
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
                List<Long> packageIds;

                //兼容2.0中，如果没选择套餐时，默认是针对全部的换电套餐
                if(CollectionUtils.isEmpty(shareMoneyActivityPackages)){
                    List<BatteryMemberCardVO> batteryMemberCardVOList = getAllBatteryPackages(tenantId);
                    packageIds = batteryMemberCardVOList.stream().map(BatteryMemberCardVO::getId).collect(Collectors.toList());
                }else{
                    packageIds = shareMoneyActivityPackages.stream().map(ShareMoneyActivityPackage::getPackageId).collect(Collectors.toList());
                }

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

    private List<BatteryMemberCardVO> getAllBatteryPackages(Integer tenantId){
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .status(BatteryMemberCard.STATUS_UP)
                .isRefund(BatteryMemberCard.NO)
                .tenantId(tenantId).build();

        List<BatteryMemberCardVO> batteryMemberCardVOS = batteryMemberCardService.selectListByQuery(query);

        return batteryMemberCardVOS;
    }

    /**
     * 构建JT808
     * @param electricityCar
     * @param userInfo
     * @return
     */
    private CarLockCtrlHistory buildCarLockCtrlHistory(ElectricityCar electricityCar, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {

            boolean result = carRentalOrderBizService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_LOCK, 3);

            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory
                    .setStatus(result ? CarLockCtrlHistory.STATUS_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_LOCK_FAIL);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_UN_BIND_USER_LOCK);

            return carLockCtrlHistory;
        }
        return null;
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
