package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.OffLineElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;

/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
public interface ElectricityCabinetOrderOperHistoryService {


    /**
     * 新增数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 实例对象
     */
    ElectricityCabinetOrderOperHistory insert(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory);

    /**
     * 离线换电新增操作记录
     * @param offLineElectricityCabinetOrderOperHistory
     * @return
     */
    R insertOffLineOperateHistory(OffLineElectricityCabinetOrderOperHistory offLineElectricityCabinetOrderOperHistory);


    R queryListByOrderId(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery);

    R queryCountByOrderId(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery);
    
    Integer updateTenantIdByOrderId(String orderId, Integer superAdminTenantId);
    
    ElectricityCabinetOrderOperHistory queryOrderHistoryFinallyFail(String orderId);
    
    
    void initExchangeOrderOperHistory(String orderId, Integer tenantId, Integer oldCell);
}
