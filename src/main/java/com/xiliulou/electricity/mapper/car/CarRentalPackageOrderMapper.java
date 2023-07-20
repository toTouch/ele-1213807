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
     * 根据用户ID查找最后一条的购买记录信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 购买订单信息
     */
    CarRentalPackageOrderPO seletLastByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 根据用户ID进行退押操作<br />
     * 将使用中、未使用的订单全部设置为已失效
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param optId    操作人ID（可为空）
     * @param optTime    操作时间
     * @return 操作总条数
     */
    Integer refundDepositByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据订单编号更改支付状态、使用状态、使用时间
     * @param orderNo 订单编号
     * @param payState 支付状态
     * @param useState 使用状态
     * @param useBeginTime 开始使用时间
     * @return 操作总条数
     */
    Integer updateStateByOrderNo(@Param("orderNo") String orderNo, @Param("payState") Integer payState, @Param("useState") Integer useState, @Param("useState") Long useBeginTime);

    /**
     * 根据用户ID查询未使用的订单总数
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 总条数
     */
    Integer countByUnUseByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 根据套餐ID查询购买记录总数
     * @param rentalPackageId 套餐ID
     * @return 总条数
     */
    Integer countByRentalPackageId(Long rentalPackageId);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return 套餐购买订单集
     */
    List<CarRentalPackageOrderPO> list(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return 套餐购买订单集
     */
    List<CarRentalPackageOrderPO> page(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return 总条数
     */
    Integer count(CarRentalPackageOrderQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return 套餐购买订单
     */
    CarRentalPackageOrderPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 套餐购买订单
     */
    CarRentalPackageOrderPO selectById(Long id);

    /**
     * 根据订单编码更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @param optTime 操作时间
     * @return 操作总条数
     */
    int updatePayStateByOrderNo(@Param("orderNo") String orderNo, @Param("payState") Integer payState, @Param("remark") String remark, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID更新支付状态
     * @param id 主键ID
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @param optTime 操作时间
     * @return 操作总条数
     */
    int updatePayStateById(@Param("id") Long id, @Param("payState") Integer payState, @Param("remark") String remark, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID更新使用状态
     * @param orderNo 订单编码
     * @param useState 使用状态
     * @param uid 操作人
     * @param optTime 操作时间
     * @return 操作总条数
     */
    int updateUseStateByOrderNo(@Param("orderNo") String orderNo, @Param("useState") Integer useState, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID更新使用状态
     * @param id 主键ID
     * @param useState 使用状态
     * @param uid 操作人
     * @param optTime 操作时间
     * @return 操作总条数
     */
    int updateUseStateById(@Param("id") Long id, @Param("useState") Integer useState, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 插入
     * @param entity 实体类
     * @return 操作总条数
     */
    int insert(CarRentalPackageOrderPO entity);
}
