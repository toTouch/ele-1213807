package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetOfflineReportOrder;

/**
 * 离线换电上报订单表(TElectricityCabinetOfflineReportOrder)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOfflineReportOrderService {


    void insertOrder(ElectricityCabinetOfflineReportOrder electricityCabinetOfflineReportOrder);

    ElectricityCabinetOfflineReportOrder queryByOrderId(String orderId);

}
