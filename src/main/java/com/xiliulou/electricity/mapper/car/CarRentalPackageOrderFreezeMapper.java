package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 租车套餐订单冻结表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageOrderFreezeMapper {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderFreezePO> list(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderFreezePO> page(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageOrderFreezePO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageOrderFreezePO selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageOrderFreezePO entity);
}
