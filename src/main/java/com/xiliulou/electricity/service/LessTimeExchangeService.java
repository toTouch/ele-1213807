package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.LessTimeExchangeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

/**
 * 二次服务
 *
 * @author renhang
 */
public interface LessTimeExchangeService {

    /**
     * 二次退电
     *
     * @param userInfo userInfo
     * @param cabinet  cabinet
     * @return Pair
     */
    Pair<Boolean, Object> lessTimeReturnBatteryHandler(UserInfo userInfo, ElectricityCabinet cabinet);

    /**
     * 二次扫码换电/选仓换电 兼容租电成功电池未取走
     *
     * @param userInfo           userInfo
     * @param cabinet            cabinet
     * @param electricityBattery electricityBattery
     * @param exchangeDTO        exchangeDTO
     * @return Pair
     */
    Pair<Boolean, ExchangeUserSelectVO> lessTimeExchangeTwoCountAssert(UserInfo userInfo, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, LessTimeExchangeDTO exchangeDTO);

    /**
     * 打开满电仓：
     * 二次扫码换电上次失败：旧仓门开门失败旧电池在仓，需要分配满电仓以及打开满电仓
     * 二次扫码换电上次失败：新仓门开门失败旧电池在仓，需要打开上个订单的新仓门
     * 选仓换电上次开始：同1
     *
     * @param cabinetOrder cabinetOrder
     * @param cabinet      cabinet
     * @param cellNo       cellNo
     * @param batteryName  batteryName
     * @param oldCell      oldCell
     * @return String
     */
    String openFullBatteryCellHandler(ElectricityCabinetOrder cabinetOrder, ElectricityCabinet cabinet, Integer cellNo, String batteryName, String oldCell);


    /**
     * 获取满地仓
     * 1. 上次换电失败旧电池在仓，需要获取新的满电仓
     * 2. todo 快捷换电
     *
     * @param electricityCabinet electricityCabinet
     * @param userInfo           userInfo
     * @param franchisee         franchisee
     * @return Triple
     */
    Triple<Boolean, String, Object> allocateFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee);


    /**
     * 是否满足自主开仓
     *
     * @param orderId orderId
     * @param eid eid
     * @param startTime startTime
     * @param cell cell
     * @return Boolean
     */
    Boolean isSatisfySelfOpenCondition(String orderId, Integer eid, Long startTime, Integer cell);
}
