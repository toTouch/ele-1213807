package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.OrderForBatteryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

import static com.xiliulou.electricity.entity.ExchangeBatterySoc.RETURN_POWER_DEFAULT;

/**
 * @ClassName: ReturnBatterySuccessHandlerServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-11-06 16:37
 */
@Service
@Slf4j
public class ReturnBatterySuccessHandlerServiceImpl implements ReturnBatterySuccessHandlerService {
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private BatteryTrackRecordService batteryTrackRecordService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private ExchangeBatterySocService exchangeBatterySocService;
    
    XllThreadPoolExecutorService operateBatterSocThreadPool = XllThreadPoolExecutors.newFixedThreadPool("OPERATE_BATTERY_SOC_ANALYZE", 1, "operate-battery-soc-pool-thread");
    
    
    @Override
    public void checkReturnBatteryDoor(RentBatteryOrder rentBatteryOrder) {
        
        BatteryTrackRecord batteryTrackRecord = new BatteryTrackRecord().setSn(rentBatteryOrder.getElectricityBatterySn())
                .setEId(Long.valueOf(rentBatteryOrder.getElectricityCabinetId()))
                .setEName(Optional.ofNullable(electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId())).map(ElectricityCabinet::getName).orElse(""))
                .setENo(rentBatteryOrder.getCellNo()).setType(BatteryTrackRecord.TYPE_RETURN_IN)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(rentBatteryOrder.getUpdateTime())).setOrderId(rentBatteryOrder.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(batteryTrackRecord);
        
        //查找用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return;
        }
        
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(rentBatteryOrder.getUid());
        
        //放入电池改为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySnFromDb(rentBatteryOrder.getElectricityBatterySn());
        
        if (Objects.nonNull(electricityBattery)) {
            if (!Objects.equals(electricityBattery.getSn(), rentBatteryOrder.getElectricityBatterySn())) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(electricityBattery.getId());
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newElectricityBattery.setUid(null);
                //如果放入的电池和用户绑定的电池不一样且放入的电池不为空
                if (Objects.nonNull(oldElectricityBattery) && Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(electricityBattery.getUid(),
                        oldElectricityBattery.getUid())) {
                    newElectricityBattery.setGuessUid(oldElectricityBattery.getUid());
                }
                newElectricityBattery.setElectricityCabinetId(null);
                newElectricityBattery.setElectricityCabinetName(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                
                // 删除redis中保存的租电订单或换电订单
                OrderForBatteryUtil.delete(electricityBattery.getSn());
                
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
            }
        }
        
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId());
            if (Objects.nonNull(electricityCabinet)) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(oldElectricityBattery.getId());
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
                newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
                newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
                newElectricityBattery.setUid(null);
                newElectricityBattery.setGuessUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                newElectricityBattery.setBorrowExpireTime(null);
                
                // 删除redis中保存的租电订单或换电订单
                OrderForBatteryUtil.delete(oldElectricityBattery.getSn());
                
                Long bindTime = oldElectricityBattery.getBindTime();
                log.info("return bindTime={},currentTime={}", bindTime, System.currentTimeMillis());
                //如果绑定时间为空或者电池绑定时间小于当前时间则更新电池信息
                if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
                    newElectricityBattery.setBindTime(System.currentTimeMillis());
                    electricityBatteryService.updateBatteryUser(newElectricityBattery);
                }
            }
            
        }
        // 归还电池
        operateBatterSocThreadPool.execute(() -> handlerUserRentBatterySoc(oldElectricityBattery.getSn(), oldElectricityBattery.getPower()));
        
    }
    
    
    /**
     * 退电电池 记录soc
     */
    private void handlerUserRentBatterySoc(String returnSn, Double returnPower) {
        if (StrUtil.isBlank(returnSn)) {
            log.warn("EleOperateQueueHandler/handlerUserRentBatterySoc is error,returnSn is null");
            return;
        }
        if (Objects.isNull(returnPower)) {
            log.warn("EleOperateQueueHandler/handlerUserRentBatterySoc is error,returnPower is null, sn={}", returnSn);
            return;
        }
        
        ExchangeBatterySoc exchangeBatterySoc = exchangeBatterySocService.queryOneByUidAndSn(returnSn);
        if (Objects.isNull(exchangeBatterySoc)) {
            log.warn("EleOperateQueueHandler/handlerUserRentBatterySoc is error, rentBatterySoc should is null, sn={}", returnSn);
            return;
        }
        
        try {
            // 处理异常交换
            if (Objects.equals(exchangeBatterySoc.getReturnPower(), RETURN_POWER_DEFAULT) && Objects.isNull(exchangeBatterySoc.getUpdateTime())) {
                ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
                batterySoc.setId(exchangeBatterySoc.getId());
                batterySoc.setReturnPower(returnPower);
                batterySoc.setPoorPower(exchangeBatterySoc.getTakeAwayPower() - returnPower);
                batterySoc.setUpdateTime(System.currentTimeMillis());
                exchangeBatterySocService.update(batterySoc);
            }
        } catch (Exception e) {
            log.error("EleOperateQueueHandler/handlerUserRentBatterySoc is error, sn={}", returnSn, e);
        }
    }
}
