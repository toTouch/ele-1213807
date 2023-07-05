package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderRentRefundQryModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 租车套餐订单租金退款表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageOrderRentRefundMapper {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderRentRefundPO> list(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderRentRefundPO> page(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageOrderRentRefundPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageOrderRentRefundPO selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageOrderRentRefundPO entity);
}
