package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetStatistic;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeFrequencyVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderService {

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetOrder electricityCabinetOrder);

    ElectricityCabinetOrder queryByOrderId(String orderId);

    void insertOrder(ElectricityCabinetOrder electricityCabinetOrder);

    R openDoor(OpenDoorQuery openDoorQuery);

    R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    Integer homepageExchangeOrderSumCount(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequencyCount(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    Integer queryCountForScreenStatistic(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    void exportExcel(ElectricityCabinetOrderQuery electricityCabinetOrderQuery, HttpServletResponse response);

    R endOrder(String orderId);

    Integer homeOneCount(Long first, Long now, List<Integer> eleIdList, Integer tenantId);

    BigDecimal homeOneSuccess(Long first, Long now, List<Integer> eleIdList, Integer tenantId);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList, Integer tenantId);

    Integer homeMonth(Long uid, Long firstMonth, Long now);

    Integer homeTotal(Long uid);

    ElectricityCabinetOrder queryByUid(Long uid);

    ElectricityCabinetOrder queryByCellNoAndEleId(Integer eleId, Integer cellNo);

    String findUsableCellNo(Integer id);

    @Deprecated
    R queryNewStatus(String orderId);

    R selfOpenCell(OrderSelfOpenCellQuery orderSelfOpenCellQuery);

    R checkOpenSessionId(String sessionId);

    Triple<Boolean, String, Object> orderV2(OrderQueryV2 orderQuery);
    
    Triple<Boolean, String, Object> orderSelectionExchange(OrderSelectionExchangeQuery orderQuery);
    
    Triple<Boolean, String, String> checkAndModifyMemberCardCount(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard);

    Triple<Boolean, String, Object> queryOrderStatusForShow(String orderId);
    
    ElectricityCabinetOrder selectLatestByUidV2(Long uid);

    ElectricityCabinetOrder selectLatestByUid(Long uid, Integer tenantId);

    List<ElectricityCabinetOrder> selectTodayExchangeOrder(Integer id, long todayStartTimeStamp, long todayEndTimeStamp, Integer tenantId);

    List<ElectricityCabinetOrder> selectMonthExchangeOrders(Integer id, long todayStartTimeStamp, long todayEndTimeStamp, Integer tenantId);

    Triple<Boolean, String, Object> bluetoothExchangeCheck(String productKey, String deviceName);
    
    ElectricityCabinetOrderVO selectLatestOrderAndCabinetInfo(Long uid);
    ElectricityCabinetStatistic queryExchangeOrder(Integer eid,long startTimeStamp, long endTimeStamp, Integer tenantId);
}
