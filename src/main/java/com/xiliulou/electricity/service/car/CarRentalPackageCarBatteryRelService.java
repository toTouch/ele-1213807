package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPO;

import java.util.List;

/**
 * 租车套餐车辆电池关联表 Service
 * @author xiaohui.song
 **/
public interface CarRentalPackageCarBatteryRelService {

    /**
     * 根据套餐ID删除(逻辑删除)
     * @param rentalPackageId 套餐ID
     * @return
     */
    boolean delByRentalPackageId(Long rentalPackageId);

    /**
     * 根据套餐ID查询
     * @param rentalPackageId 套餐ID
     * @return
     */
    List<CarRentalPackageCarBatteryRelPO> selectByRentalPackageId(Long rentalPackageId);

    /**
     * 批量新增
     * @return
     */
    boolean batchInsert(List<CarRentalPackageCarBatteryRelPO> entityList);
}
