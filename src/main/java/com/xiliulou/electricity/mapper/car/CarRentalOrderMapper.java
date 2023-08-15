package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalOrderPo;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车辆租赁订单表 Mappper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalOrderMapper {

    /**
     * 根据用户UID、车辆SN码、状态查询最后一条数据
     *
     * @param tenantId    租户ID
     * @param uid         用户UID
     * @param type        订单类型，1-租车、2-还车
     * @param rentalState 状态，1-审核中、2-成功、3-审核拒绝
     * @param carSn       车辆编码
     * @return 车辆租赁订单
     */
    CarRentalOrderPo selectLastByUidAndSnAndState(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("type") Integer type,
                                                  @Param("rentalState") Integer rentalState, @Param("carSn") String carSn);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return 车辆租赁订单集
     */
    List<CarRentalOrderPo> list(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return 车辆租赁订单集
     */
    List<CarRentalOrderPo> page(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
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
     * 插入
     * @param entity 实体类
     * @return 操作条数
     */
    int insert(CarRentalOrderPo entity);
}
