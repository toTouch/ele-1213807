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
     * @param optId 操作人ID
     * @return true(成功)、false(失败)
     */
    boolean delByRentalPackageId(Long rentalPackageId, Long optId);

    /**
     * 根据套餐ID集查询
     * @param rentalPackageIdList 套餐ID集
     * @return 套餐车辆电池关联数据集
     */
    List<CarRentalPackageCarBatteryRelPO> selectByRentalPackageIds(List<Long> rentalPackageIdList);

    /**
     * 根据套餐ID查询
     * @param rentalPackageId 套餐ID
     * @return 套餐车辆电池关联数据集
     */
    List<CarRentalPackageCarBatteryRelPO> selectByRentalPackageId(Long rentalPackageId);

    /**
     * 批量新增
     * @param entityList 操作实体集
     * @return true(成功)、false(失败)
     */
    boolean batchInsert(List<CarRentalPackageCarBatteryRelPO> entityList);
}
