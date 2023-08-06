package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalOrderPo;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;

import java.util.List;

/**
 * 车辆租赁订单表 Service
 * @author xiaohui.song
 **/
public interface CarRentalOrderService {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return 车辆租赁订单集
     */
    List<CarRentalOrderPo> list(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return 车辆租赁订单集
     */
    List<CarRentalOrderPo> page(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return 总数
     */
    Integer count(CarRentalOrderQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return 车辆租赁订单
     */
    CarRentalOrderPo selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 车辆租赁订单
     */
    CarRentalOrderPo selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 操作模型
     * @return 主键ID
     */
    Long insert(CarRentalOrderPo entity);

}
