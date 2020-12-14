package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;

/**
 * 租电池记录(TRentBatteryOrder)表服务接口
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
public interface RentBatteryOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    RentBatteryOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    RentBatteryOrder queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 实例对象
     */
    RentBatteryOrder insert(RentBatteryOrder rentBatteryOrder);

    /**
     * 修改数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 实例对象
     */
    Integer update(RentBatteryOrder rentBatteryOrder);


    R queryList(RentBatteryOrderQuery rentBatteryOrderQuery);
}