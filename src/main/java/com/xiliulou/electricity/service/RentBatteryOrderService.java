package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.EleCabinetUsedRecordVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * 租电池记录(TRentBatteryOrder)表服务接口
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
public interface RentBatteryOrderService {

    /**
     * 新增数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 实例对象
     */
    RentBatteryOrder insert(RentBatteryOrder rentBatteryOrder);

    R queryList(RentBatteryOrderQuery rentBatteryOrderQuery);

    R rentBattery(RentBatteryQuery rentBatteryQuery);

    R returnBattery(Integer electricityCabinetId);

    void update(RentBatteryOrder rentBatteryOrder);

    R openDoor(RentOpenDoorQuery rentOpenDoorQuery);

    RentBatteryOrder queryByOrderId(String orderId);

    R endOrder(String orderId);

    RentBatteryOrder queryByUidAndType(Long uid);

	void exportExcel(RentBatteryOrderQuery rentBatteryOrderQuery, HttpServletResponse response);

	R queryNewStatus(String orderId);

    Triple<Boolean, String, Object> findUsableBatteryCellNo(ElectricityCabinet electricityCabinet, String cellNo,String batteryType,Long franchiseeId,Integer OrderSource);

    R queryCount(RentBatteryOrderQuery rentBatteryOrderQuery);

    Integer queryCountForScreenStatistic(RentBatteryOrderQuery rentBatteryOrderQuery);


    RentBatteryOrder selectLatestByUid(Long uid, Integer tenantId, String status);

    List<EleCabinetUsedRecordVO> findEleCabinetUsedRecords(EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery);

    Integer findUsedRecordsTotalCount(EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery);
    
    List<RentBatteryOrder> selectByUidAndTime(Long uid, Long membercardStartTime, Long currentTime);
    
    R queryRentBatteryOrderLimitCountByUid(Long uid);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    R listSuperAdminPage(RentBatteryOrderQuery rentBatteryOrderQuery);
    
    Integer existSameCabinetCellSameTimeOpenReturnOrder(Long createTime, Integer electricityCabinetId, Integer oldCellNo);
    
    Integer existReturnRentOrderInSameCabinetAndCell(Long startTime, Long endTime, Integer eid, Integer cell);
    
    String acquireFullBatteryBox(List<ElectricityCabinetBox> electricityCabinetBoxList, UserInfo userInfo, Franchisee franchisee, Double fullyCharged);
    
    List<RentBatteryOrder> listByOrderIdList(Set<String> returnOrderIdList);

    RentBatteryOrder queryLatelyRentReturnOrder(Long uid, Long scanTime, Long startTime, List<Integer> orderTypes);


    R returnBatteryCheck(Integer electricityCabinetId);

    R lessRentReturnSelfOpenCell(LessExchangeSelfOpenCellQuery query);

    /**
     * 根据订单号判断是否租退电订单
     */
    Boolean isRendReturnOrder(String orderId);
}
