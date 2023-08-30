package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPo;
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
     * @return 操作条数
     */
    Integer delByRentalPackageId(@Param("rentalPackageId") Long rentalPackageId, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据套餐ID集查询
     * @param rentalPackageIdList 套餐ID集
     * @return 套餐车辆型号和电池型号关联数据集
     */
    List<CarRentalPackageCarBatteryRelPo> selectByRentalPackageIds(@Param("rentalPackageIdList") List<Long> rentalPackageIdList);

    /**
     * 根据套餐ID查询
     * @param rentalPackageId 套餐ID
     * @return 套餐车辆型号和电池型号关联数据集
     */
    List<CarRentalPackageCarBatteryRelPo> selectByRentalPackageId(Long rentalPackageId);

    /**
     * 批量新增
     * @param entityList 实体数据集
     * @return 操作条数
     */
    Integer batchInsert(@Param("entityList") List<CarRentalPackageCarBatteryRelPo> entityList);

}
