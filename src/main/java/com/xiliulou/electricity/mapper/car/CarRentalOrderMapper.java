package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalOrderPO;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 车辆租赁订单表 Mappper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalOrderMapper {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalOrderPO> list(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalOrderPO> page(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalOrderQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalOrderPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalOrderPO selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalOrderPO entity);
}
