package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.OffLineElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;

/**
 * 订单异常状态记录表(TElectricityExceptionOrderStatusRecord)表服务接口
 *
 * @author makejava
 * @since 2022-07-21 17:57:22
 */
public interface ElectricityExceptionOrderStatusRecordService {


    void insert(ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord);

    ElectricityExceptionOrderStatusRecord queryByOrderId(String orderId);

    void update(ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord);

}
