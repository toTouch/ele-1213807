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
     * 根据用户UID、车辆SN码、类型最后一条数据
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @param type 订单类型，1-租车、2-还车
     * @param carSn 车辆编码
     * @return 车辆租赁订单
     */
    CarRentalOrderPo selectLastByUidAndSnAndType(Integer tenantId, Long uid, Integer type, String carSn);

    /**
     * 根据ID进行数据更新
     * @param entity 更新数据集
     * @return true(成功)、false(失败)
     */
    boolean updateById(CarRentalOrderPo entity);

    /**
     * 根据用户UID、车辆SN码、类型、状态查询最后一条数据
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @param type 订单类型，1-租车、2-还车
     * @param rentalState 状态，1-审核中、2-成功、3-审核拒绝
     * @param carSn 车辆编码
     * @return 车辆租赁订单
     */
    CarRentalOrderPo selectLastByUidAndSnAndTypeAndState(Integer tenantId, Long uid, Integer type, Integer rentalState, String carSn);

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
