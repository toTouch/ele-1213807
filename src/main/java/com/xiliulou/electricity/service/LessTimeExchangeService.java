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
     * 二次扫码换电兼容租电成功电池未取走
     *
     * @param userInfo           userInfo
     * @param cabinet            cabinet
     * @param electricityBattery electricityBattery
     * @param exchangeDTO        exchangeDTO
     * @return Pair
     */
    Pair<Boolean, ExchangeUserSelectVO> lessTimeExchangeTwoCountAssert(UserInfo userInfo, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, LessTimeExchangeDTO exchangeDTO);

    /**
     * 打开满电仓
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
     *
     * @param electricityCabinet electricityCabinet
     * @param userInfo userInfo
     * @param franchisee franchisee
     * @return Triple
     */
    Triple<Boolean, String, Object> allocateFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee);



}
