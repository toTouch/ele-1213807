package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐车辆电池关联 Mapper
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageCarBatteryRelMapper {

    /**
     * 根据套餐ID删除(逻辑删除)
     * @param rentalPackageId 套餐ID
     * @return
     */
    Integer delByRentalPackageId(Long rentalPackageId);

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
    Integer batchInsert(@Param("entityList") List<CarRentalPackageCarBatteryRelPO> entityList);

}
