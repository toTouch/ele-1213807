package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOfflineReportOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ExchangeBatterySoc;
import com.xiliulou.electricity.entity.OffLineElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOfflineReportOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ExchangeBatterySocService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.vo.OperateMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static cn.hutool.core.lang.PatternPool.MOBILE;
import static com.xiliulou.electricity.entity.ExchangeBatterySoc.RETURN_POWER_DEFAULT;

/**
 * @author: HRP
 * @Date: 2022/03/03 15:22
 * @Description: 离线换电
 */
@Service(value = ElectricityIotConstant.NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER)
@Slf4j
public class NormalOffLineEleExchangeHandlerIot extends AbstractElectricityIotHandler {
    
    /**
     * 订单来源 APP离线换电
     */
    private static final Integer ORDER_SOURCE_FOR_OFFLINE = 3;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCabinetOfflineReportOrderService electricityCabinetOfflineReportOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    BatteryTrackRecordService batteryTrackRecordService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private ElectricityMemberCardOrderService batteryMemberCardOrderService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    private ExchangeBatterySocService exchangeBatterySocService;
    
    XllThreadPoolExecutorService offLineExchangeBatterSocThreadPool = XllThreadPoolExecutors.newFixedThreadPool("OFF_LINE_EXCHANGE_BATTERY_SOC_ANALYZE", 1,
            "off-line-exchange-battery-soc-pool-thread");
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        OfflineOrderMessage offlineOrderMessage = JsonUtil.fromJson(receiverMessage.getOriginContent(), OfflineOrderMessage.class);
        if (StringUtils.isBlank(receiverMessage.getSessionId()) || Objects.isNull(offlineOrderMessage)) {
            log.warn("OFFLINE EXCHANGE WARN! originalMessage is illegal,sessionId={}", receiverMessage.getSessionId());
            return;
        }
    
        User user = userService.queryByUserPhone(acquireUserPhone(offlineOrderMessage.getPhone()), User.TYPE_USER_NORMAL_WX_PRO, electricityCabinet.getTenantId());
        if (Objects.isNull(user)) {
            orderConfirm(electricityCabinet, offlineOrderMessage, null);
            log.warn("OFFLINE EXCHANGE WARN! not found user,phone={},sessionId={}", offlineOrderMessage.getPhone(), receiverMessage.getSessionId());
            return;
        }
        
        if (!redisService.setNx(CacheConstant.OFFLINE_ELE_RECEIVER_CACHE_KEY + offlineOrderMessage.getOrderId() + receiverMessage.getType(), "true", 10 * 1000L, true)) {
            log.warn("OFFLINE EXCHANGE WARN! orderId is lock,orderId={},uid={}", offlineOrderMessage.getOrderId(), user.getUid());
            return;
        }
        
        orderConfirm(electricityCabinet, offlineOrderMessage, user);
        
        ElectricityCabinetOfflineReportOrder oldElectricityCabinetOfflineReportOrder = electricityCabinetOfflineReportOrderService.queryByOrderId(offlineOrderMessage.getOrderId());
        if (Objects.nonNull(oldElectricityCabinetOfflineReportOrder)) {
            log.warn("OFFLINE EXCHANGE WARN! orderId already processed,orderId={},uid={}", offlineOrderMessage.getOrderId(), user.getUid());
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("OFFLINE EXCHANGE WARN! userInfo is null,uid={}", user.getUid());
            return;
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = buildElectricityCabinetOrder(electricityCabinet, offlineOrderMessage, user);
        electricityCabinetOrderService.insertOrder(electricityCabinetOrder);
        
        //处理电池轨迹
        handleBatteryTrackRecord(electricityCabinetOrder, electricityCabinet, userInfo);
        
        //操作记录
        electricityCabinetOrderOperHistoryService.insertOffLineOperateHistory(buildOrderOperHistory(electricityCabinetOrder, electricityCabinet, offlineOrderMessage));
        
        electricityCabinetOfflineReportOrderService.insertOrder(buildOfflineReportOrder(offlineOrderMessage));
        
        if (offlineOrderMessage.getIsProcessFail()) {
            log.warn("OFFLINE EXCHANGE WARN! exchange exception,orderId={},uid={}", offlineOrderMessage.getOrderId(), user.getUid());
            return;
        }
        
        //处理用户套餐
        if (!handlerUserBatteryMemberCard(userInfo, offlineOrderMessage, receiverMessage.getSessionId())) {
            return;
        }
        
        //获取用户绑定的电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        
        //查询当前归还的电池信息
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySnFromDb(offlineOrderMessage.getOldElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            log.warn("OFFLINE EXCHANGE WARN! oldElectricityBattery is null,sn={},uid={}", offlineOrderMessage.getOldElectricityBatterySn(), user.getUid());
            return;
        }
        
        //通过guessUid获取电池信息; 如果有电池的guessUid为当前换电用户 ,则将此电池更新为放入电池的Uid
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.listBatteryByGuessUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(electricityBatteries) && !Objects.equals(oldElectricityBattery.getUid(), userInfo.getUid())) {
            List<Long> batteryIdList = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            if (Objects.nonNull(oldElectricityBattery.getUid())) {
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, oldElectricityBattery.getUid());
            } else {
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, oldElectricityBattery.getGuessUid());
            }
        }
        
        //归还电池的上次绑定时间
        Long oldBatteryBindTime = oldElectricityBattery.getBindTime();
        
        //如果电池绑定时间为空或者绑定时间小于当前订单结束时间则更新电池信息
        if (Objects.isNull(oldBatteryBindTime) || offlineOrderMessage.getEndTime() > oldBatteryBindTime) {
            ElectricityBattery oldElectricityBatteryUpdate = new ElectricityBattery();
            oldElectricityBatteryUpdate.setId(oldElectricityBattery.getId());
            oldElectricityBatteryUpdate.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
            oldElectricityBatteryUpdate.setElectricityCabinetId(electricityCabinet.getId());
            oldElectricityBatteryUpdate.setElectricityCabinetName(electricityCabinet.getName());
            oldElectricityBatteryUpdate.setUid(null);
            oldElectricityBatteryUpdate.setGuessUid(null);
            oldElectricityBatteryUpdate.setUpdateTime(System.currentTimeMillis());
            oldElectricityBatteryUpdate.setBorrowExpireTime(null);
            oldElectricityBatteryUpdate.setBindTime(offlineOrderMessage.getEndTime());
            electricityBatteryService.updateBatteryUser(oldElectricityBatteryUpdate);
        }
        
        // 归还电池soc
        offLineExchangeBatterSocThreadPool.execute(
                () -> handlerUserRentBatterySoc(offlineOrderMessage.getOldElectricityBatterySn(), offlineOrderMessage.getPlaceBatterySoc()));
        
        //判断用户是已租赁电池状态
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_NO)) {
            log.warn("OFFLINE EXCHANGE WARN! user not rent battery,uid={}", user.getUid());
            return;
        }
        
        ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySnFromDb(offlineOrderMessage.getNewElectricityBatterySn());
        if (Objects.isNull(newElectricityBattery)) {
            log.warn("OFFLINE EXCHANGE WARN! electricityBattery is null,sn={},uid={}", offlineOrderMessage.getNewElectricityBatterySn(), user.getUid());
            return;
        }
        
        //如果已租电池的时间小于用户当前绑定电池的时间  则不需要更新
        if (Objects.nonNull(electricityBattery) && Objects.nonNull(electricityBattery.getBindTime()) && Objects.nonNull(newElectricityBattery.getBindTime())
                && electricityBattery.getBindTime() > offlineOrderMessage.getEndTime()) {
            log.warn("OFFLINE EXCHANGE WARN! order endTime less than user bind battery bindTime,bindTime={},endTime={},uid={}", electricityBattery.getBindTime(),
                    newElectricityBattery.getBindTime(), user.getUid());
            return;
        }
        
        //用户绑定电池和还入电池是否一致，不一致绑定的电池更新为游离态
        if (Objects.nonNull(electricityBattery)) {
            //如果用户绑定的电池与放入的电池不一致 并且绑定的电池的uid和放入的电池的uid相同（离线换电会发生这种情况）
            if (!Objects.equals(electricityBattery.getSn(), oldElectricityBattery.getSn()) && Objects.equals(electricityBattery.getUid(), oldElectricityBattery.getUid())
                    && Objects.nonNull(electricityBattery.getBindTime()) && electricityBattery.getBindTime() > offlineOrderMessage.getEndTime()) {
                log.warn("OFFLINE EXCHANGE WARN! user binding battery! uid={},return batterySn={},binding batterySn={}", user.getUid(), oldElectricityBattery.getSn(),
                        electricityBattery.getSn());
                return;
            }
            
            if (!Objects.equals(electricityBattery.getSn(), offlineOrderMessage.getOldElectricityBatterySn())) {
                ElectricityBattery newBattery = new ElectricityBattery();
                newBattery.setId(electricityBattery.getId());
                newBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newBattery.setUid(null);
                
                //设置用户绑定的电池的guessId为归还电池的用户Id
                if (Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(oldElectricityBattery.getUid(), electricityBattery.getUid())) {
                    newBattery.setGuessUid(oldElectricityBattery.getUid());
                }
                
                //设置用户绑定的电池的guessId为归还电池的guessUId
                if (Objects.isNull(oldElectricityBattery.getUid()) && Objects.nonNull(oldElectricityBattery.getGuessUid())) {
                    newBattery.setGuessUid(oldElectricityBattery.getGuessUid());
                }
                
                //设置用户绑定的电池的guessId为归还电池的guessUId
                if (Objects.isNull(oldElectricityBattery.getUid()) && Objects.isNull(oldElectricityBattery.getGuessUid())) {
                    newBattery.setGuessUid(null);
                }
                
                newBattery.setUpdateTime(System.currentTimeMillis());
                newBattery.setElectricityCabinetId(null);
                newBattery.setElectricityCabinetName(null);
                newBattery.setBorrowExpireTime(null);
                electricityBatteryService.updateBatteryUser(newBattery);
            }
        }
        
        //如果电池绑定时间为空或者绑定时间小于当前订单结束时间则更新电池信息
        if (Objects.isNull(newElectricityBattery.getBindTime()) || offlineOrderMessage.getEndTime() > newElectricityBattery.getBindTime()) {
            ElectricityBattery newElectricityBatteryUpdate = new ElectricityBattery();
            newElectricityBatteryUpdate.setId(newElectricityBattery.getId());
            newElectricityBatteryUpdate.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
            newElectricityBatteryUpdate.setElectricityCabinetId(null);
            newElectricityBatteryUpdate.setElectricityCabinetName(null);
            newElectricityBatteryUpdate.setUid(user.getUid());
            newElectricityBatteryUpdate.setUpdateTime(System.currentTimeMillis());
            newElectricityBatteryUpdate.setBorrowExpireTime(Long.parseLong(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
            newElectricityBatteryUpdate.setBindTime(offlineOrderMessage.getEndTime());
            electricityBatteryService.updateBatteryUser(newElectricityBatteryUpdate);
            
            // 取走电池soc
            offLineExchangeBatterSocThreadPool.execute(
                    () -> handlerUserTakeBatterySoc(user.getUid(), offlineOrderMessage.getNewElectricityBatterySn(), offlineOrderMessage.getTakeBatterySoc()));
        }
        
    }
    
    /**
     * 换电取走电池 记录soc
     */
    private void handlerUserTakeBatterySoc(Long uid, String takeBatterySn, Double takeAwayPower) {
        if (Objects.isNull(takeAwayPower)) {
            log.warn("NormalOffLineEleExchangeHandlerIot/handlerUserTakeBatterySoc is error,takeAwayPower is null, sn={}", takeBatterySn);
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("NormalOffLineEleExchangeHandlerIot/handlerUserTakeBatterySoc is error,userInfo is null, uid={},sn={}", uid, takeBatterySn);
            return;
        }
        
        try {
            ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
            batterySoc.setUid(userInfo.getUid());
            batterySoc.setSn(takeBatterySn);
            batterySoc.setTenantId(userInfo.getTenantId());
            batterySoc.setFranchiseeId(userInfo.getFranchiseeId());
            batterySoc.setStoreId(userInfo.getStoreId());
            batterySoc.setTakeAwayPower(takeAwayPower);
            batterySoc.setReturnPower(0.00);
            batterySoc.setPoorPower(0.00);
            batterySoc.setDelFlag(0);
            batterySoc.setCreateTime(System.currentTimeMillis());
            exchangeBatterySocService.insertOne(batterySoc);
        } catch (Exception e) {
            log.error("NormalOffLineEleExchangeHandlerIot/handlerUserTakeBatterySoc/insert is exception, uid={},sn={}", userInfo.getUid(), takeBatterySn, e);
        }
        
    }
    
    /**
     * 换电归还电池 记录soc
     */
    private void handlerUserRentBatterySoc(String returnSn, Double returnPower) {
        if (StrUtil.isBlank(returnSn)) {
            log.warn("NormalOffLineEleExchangeHandlerIot/handlerUserRentBatterySoc is error,returnSn is null");
            return;
        }
        if (Objects.isNull(returnPower)) {
            log.warn("NormalOffLineEleExchangeHandlerIot/handlerUserRentBatterySoc is error,returnPower is null, returnSn={}", returnSn);
            return;
        }
        
        //  上报的sn绑定的用户+Sn;兼容异常交换场景
        ExchangeBatterySoc exchangeBatterySoc = exchangeBatterySocService.queryOneByUidAndSn(returnSn);
        if (Objects.isNull(exchangeBatterySoc)) {
            log.warn("NormalOffLineEleExchangeHandlerIot/handlerUserRentBatterySoc is error, rentBatterySoc should is not null, sn={}", returnSn);
            return;
        }
        
        try {
            if (Objects.equals(exchangeBatterySoc.getReturnPower(), RETURN_POWER_DEFAULT) && Objects.isNull(exchangeBatterySoc.getUpdateTime())) {
                ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
                batterySoc.setId(exchangeBatterySoc.getId());
                batterySoc.setReturnPower(returnPower);
                batterySoc.setPoorPower(exchangeBatterySoc.getTakeAwayPower() - returnPower);
                batterySoc.setUpdateTime(System.currentTimeMillis());
                exchangeBatterySocService.update(batterySoc);
            }
        } catch (Exception e) {
            log.error("NormalOffLineEleExchangeHandlerIot/handlerUserRentBatterySoc/update is exception,sn={}", returnPower, e);
        }
        
    }
    
    private boolean handlerUserBatteryMemberCard(UserInfo userInfo, OfflineOrderMessage offlineOrderMessage, String sessionId) {
        if (!(Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getCarBatteryDepositStatus(),
                YesNoEnum.YES.getCode()))) {
            log.warn("OFFLINE EXCHANGE WARN! user not pay deposit uid={},sessionId={}", userInfo.getUid(), sessionId);
            return Boolean.FALSE;
        }
        
        //单电
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("OFFLINE EXCHANGE WARN! not found userBatteryMemberCard,uid={},sessionId={}", userInfo.getUid(), sessionId);
                return Boolean.FALSE;
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("OFFLINE EXCHANGE WARN! not found batteryMemberCard,uid={},sessionId={}", userInfo.getUid(), sessionId);
                return Boolean.FALSE;
            }
            
            if (!Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.UN_LIMIT) && Objects.nonNull(userBatteryMemberCard.getOrderEffectiveTime())) {
                //如果换电订单的时间在当前套餐生效时间之后，则扣减次数
                if (offlineOrderMessage.getEndTime() > userBatteryMemberCard.getOrderEffectiveTime() && userBatteryMemberCard.getOrderExpireTime() > System.currentTimeMillis()) {
                    //扣除月卡
                    userBatteryMemberCardService.minCount(userBatteryMemberCard);
                }
                
                //如果套餐没过期并且剩余次数为1
                if ((userBatteryMemberCard.getOrderExpireTime() < System.currentTimeMillis()) || Objects.equals(userBatteryMemberCard.getOrderRemainingNumber(),
                        UserBatteryMemberCard.MEMBER_CARD_ONE_REMAINING)) {
                    updateUserBatteryMemberCardInfo(userBatteryMemberCard, userInfo, offlineOrderMessage.getEndTime());
                }
            }
        }
        
        //车电一体
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            try {
                carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid());
            } catch (Exception e) {
                log.error("OFFLINE EXCHANGE ERROR! carRentalPackageMember error, uid={},sessionId={}", userInfo.getUid(), sessionId, e);
            }
        }
        
        return Boolean.TRUE;
    }
    
    private ElectricityCabinetOfflineReportOrder buildOfflineReportOrder(OfflineOrderMessage offlineOrderMessage) {
        return ElectricityCabinetOfflineReportOrder.builder().orderId(offlineOrderMessage.getOrderId()).createTime(System.currentTimeMillis()).build();
    }
    
    private OffLineElectricityCabinetOrderOperHistory buildOrderOperHistory(ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet,
            OfflineOrderMessage offlineOrderMessage) {
        return OffLineElectricityCabinetOrderOperHistory.builder().orderId(electricityCabinetOrder.getOrderId()).type(offlineOrderMessage.getOfflineOrderStatus())
                .tenantId(electricityCabinet.getTenantId()).operateMsgVos(offlineOrderMessage.getMsg()).build();
    }
    
    private ElectricityCabinetOrder buildElectricityCabinetOrder(ElectricityCabinet electricityCabinet, OfflineOrderMessage offlineOrderMessage, User user) {
        int newCellNo = StringUtils.isNotBlank(offlineOrderMessage.getNewCellNo()) ? Integer.parseInt(offlineOrderMessage.getNewCellNo()) : 0;
        int oldCellNo = StringUtils.isNotBlank(offlineOrderMessage.getOldCellNo()) ? Integer.parseInt(offlineOrderMessage.getOldCellNo()) : 0;
        
        String orderStatus = offlineOrderMessage.getIsProcessFail() ? ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL : offlineOrderMessage.getStatus();
        
        return ElectricityCabinetOrder.builder().orderId(generateOrderId(electricityCabinet.getId(), offlineOrderMessage.getNewCellNo(), user.getUid())).uid(user.getUid())
                .phone(user.getPhone()).electricityCabinetId(electricityCabinet.getId()).oldCellNo(oldCellNo).newCellNo(newCellNo)
                .newElectricityBatterySn(offlineOrderMessage.getNewElectricityBatterySn()).oldElectricityBatterySn(offlineOrderMessage.getOldElectricityBatterySn()).orderSeq(null)
                .status(orderStatus).source(Objects.isNull(offlineOrderMessage.getOfflineOrderStatus()) ? ORDER_SOURCE_FOR_OFFLINE : offlineOrderMessage.getOfflineOrderStatus())
                .paymentMethod(BatteryMemberCard.BUSINESS_TYPE_BATTERY).createTime(offlineOrderMessage.getStartTime()).updateTime(offlineOrderMessage.getEndTime())
                .franchiseeId(electricityCabinet.getFranchiseeId()).storeId(electricityCabinet.getStoreId()).tenantId(electricityCabinet.getTenantId()).build();
    }
    
    private void handleBatteryTrackRecord(ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet, UserInfo userInfo) {
        if (StrUtil.isEmpty(electricityCabinetOrder.getOldElectricityBatterySn()) || StrUtil.isEmpty(electricityCabinetOrder.getNewElectricityBatterySn())) {
            return;
        }
        
        BatteryTrackRecord outBatteryTrackRecord = new BatteryTrackRecord().setSn(electricityCabinetOrder.getNewElectricityBatterySn())
                .setEId(Long.valueOf(electricityCabinet.getId())).setEName(electricityCabinet.getName()).setENo(electricityCabinetOrder.getNewCellNo())
                .setType(BatteryTrackRecord.TYPE_OFFLINE_EXCHANGE_OUT).setCreateTime(TimeUtils.convertToStandardFormatTime(electricityCabinetOrder.getUpdateTime()))
                .setOrderId(electricityCabinetOrder.getOrderId()).setUid(userInfo.getUid()).setName(userInfo.getName()).setPhone(userInfo.getPhone());
        batteryTrackRecordService.putBatteryTrackQueue(outBatteryTrackRecord);
        
        BatteryTrackRecord inBatteryTrackRecord = new BatteryTrackRecord().setSn(electricityCabinetOrder.getOldElectricityBatterySn())
                .setEId(Long.valueOf(electricityCabinet.getId())).setEName(electricityCabinet.getName()).setENo(electricityCabinetOrder.getOldCellNo())
                .setType(BatteryTrackRecord.TYPE_OFFLINE_EXCHANGE_IN).setCreateTime(TimeUtils.convertToStandardFormatTime(electricityCabinetOrder.getCreateTime()))
                .setOrderId(electricityCabinetOrder.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(inBatteryTrackRecord);
    }
    
    private void updateUserBatteryMemberCardInfo(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo, Long endTime) {
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = userBatteryMemberCardPackageService.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(endTime);
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            return;
        }
        
        //更新当前用户绑定的套餐数据
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        userBatteryMemberCardUpdate.setMemberCardId(userBatteryMemberCardPackageLatest.getMemberCardId());
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis() + userBatteryMemberCardPackageLatest.getMemberCardExpireTime());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(
                userBatteryMemberCard.getMemberCardExpireTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
        userBatteryMemberCardUpdate.setOrderRemainingNumber(userBatteryMemberCardPackageLatest.getRemainingNumber());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        //删除资源包
        userBatteryMemberCardPackageService.deleteByOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        
        //更新原来绑定的套餐订单状态
        ElectricityMemberCardOrder oldMemberCardOrder = new ElectricityMemberCardOrder();
        oldMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
        oldMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        oldMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(oldMemberCardOrder);
        
        //更新新绑定的套餐订单的状态
        ElectricityMemberCardOrder currentMemberCardOrder = new ElectricityMemberCardOrder();
        currentMemberCardOrder.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        currentMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
        currentMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(currentMemberCardOrder);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(userBatteryMemberCardPackageLatest.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("TRANSFER BATTERY MEMBER CARD PACKAGE WARN!not found user member card order Info,uid={},orderId={}", userBatteryMemberCard.getUid(),
                    userBatteryMemberCardPackageLatest.getOrderId());
            return;
        }
        
        //更新用户电池型号
        userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
    }
    
    private void orderConfirm(ElectricityCabinet electricityCabinet, OfflineOrderMessage offlineOrderMessage, User user) {
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", offlineOrderMessage.getOrderId());
        dataMap.put("status", offlineOrderMessage.getStatus());
        
        Long uid = -1L;
        if (Objects.nonNull(user)) {
            uid = user.getUid();
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + uid + "_" + offlineOrderMessage.getOrderId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.warn("OFFLINE EXCHANGE WARN! send command error! orderId={}", offlineOrderMessage.getOrderId());
        }
    }
    
    private static String acquireUserPhone(String receivePhone) {
        String phone = "";
        if (StringUtils.isBlank(receivePhone)) {
            return phone;
        }
        
        if (PhoneUtil.isMobile(receivePhone)) {
            return receivePhone;
        }
        
        Matcher matcher = MOBILE.matcher(receivePhone);
        if(matcher.find()){
            return matcher.group(0);
        }
        
        return phone;
    }
    
    private String generateOrderId(Integer id, String cellNo, Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + id + cellNo + uid;
    }
    
    
    @Data
    class OfflineOrderMessage {
        
        /**
         * 换电柜旧仓门号
         */
        private String oldCellNo;
        
        /**
         * 换电柜新仓门号
         */
        private String newCellNo;
        
        /**
         * 旧电池编号
         */
        private String oldElectricityBatterySn;
        
        /**
         * 新电池编号
         */
        private String newElectricityBatterySn;
        
        /**
         * 订单状态
         */
        private String status;
        
        /**
         * 订单开始时间
         */
        private Long startTime;
        
        /**
         * 订单结束时间
         */
        private Long endTime;
        
        /**
         * 本次操作是否执行失败
         */
        private Boolean isProcessFail;
        
        /**
         * 订单号
         */
        private String orderId;
        
        /**
         * 用户手机号
         */
        private String phone;
        
        /**
         * 是否上报电池类型
         */
        private Boolean isMultiBatteryModel;
        
        /**
         * 旧电池电量
         */
        private Double power;
        
        /**
         * 操作记录列表
         */
        private List<OperateMsgVo> msg;
        /**
         * 订单来源 APP离线换电
         */
        //        protected static final Integer ORDER_SOURCE_FOR_OFFLINE = 3;
        
        /**
         * 离线订单类型  3 离线换电  4  蓝牙
         */
        private Integer offlineOrderStatus;
        
        
        /**
         * 归还soc
         */
        private Double placeBatterySoc;
        
        /**
         * 取走soc
         */
        private Double takeBatterySoc;
    }
}


