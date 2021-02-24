package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.query.RentBatteryQuery;
import com.xiliulou.electricity.query.ReturnBatteryQuery;

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

    R returnBattery(ReturnBatteryQuery returnBatteryQuery);
}