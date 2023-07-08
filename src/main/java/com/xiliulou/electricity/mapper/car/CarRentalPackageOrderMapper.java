package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐购买订单 Mapper 操作
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageOrderMapper {

    /**
     * 根据套餐ID查询购买记录总数
     * @param rentalPackageId
     * @return
     */
    Integer countByRentalPackageId(Long rentalPackageId);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderPO> list(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderPO> page(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageOrderQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageOrderPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageOrderPO selectById(Long id);

    /**
     * 根据订单编码更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @param optTime 操作时间
     * @return
     */
    int updatePayStateByOrderNo(@Param("orderNo") String orderNo, @Param("payState") Integer payState, @Param("remark") String remark, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID更新支付状态
     * @param id 主键ID
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @param optTime 操作时间
     * @return
     */
    int updatePayStateById(@Param("id") Long id, @Param("payState") Integer payState, @Param("remark") String remark, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID更新使用状态
     * @param orderNo 订单编码
     * @param useState 使用状态
     * @param uid 操作人
     * @param optTime 操作时间
     * @return
     */
    int updateUseStateByOrderNo(@Param("orderNo") String orderNo, @Param("useState") Integer useState, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID更新使用状态
     * @param id 主键ID
     * @param useState 使用状态
     * @param uid 操作人
     * @param optTime 操作时间
     * @return
     */
    int updateUseStateById(@Param("id") Long id, @Param("useState") Integer useState, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageOrderPO entity);
}
