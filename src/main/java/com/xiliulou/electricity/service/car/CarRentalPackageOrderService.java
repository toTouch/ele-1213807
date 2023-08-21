package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐订单表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderService {

    /**
     * 根据用户UID查询支付成功的总金额(实际支付金额)
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 总金额
     */
    BigDecimal selectPaySuccessAmountTotal(Integer tenantId, Long uid);

    /**
     * 根据用户ID查找最后一条未支付成功的购买记录信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 购买订单信息
     */
    CarRentalPackageOrderPo selectLastUnPayByUid(Integer tenantId, Long uid);

    /**
     * 根据用户ID查询第一条未使用的支付成功的订单信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 套餐购买订单
     */
    CarRentalPackageOrderPo selectFirstUnUsedAndPaySuccessByUid(Integer tenantId, Long uid);

    /**
     * 根据用户ID查找最后一条支付成功的购买记录信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 购买订单信息
     */
    CarRentalPackageOrderPo selectLastPaySuccessByUid(Integer tenantId, Long uid);

    /**
     * 根据用户ID进行退押操作<br />
     * 将使用中、未使用的订单全部设置为已失效
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param optId 操作人ID
     * @return true(成功)、false(失败)
     */
    boolean refundDepositByUid(Integer tenantId, Long uid, Long optId);

    /**
     * 根据订单编号更改支付状态、使用状态、使用时间<br />
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @param useState 使用状态
     * @return true(成功)、false(失败)
     */
    Boolean updateStateByOrderNo(String orderNo, Integer payState, Integer useState);

    /**
     * 根据用户ID查询未使用状态的订单总条数<br />
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 总数
     */
    Integer countByUnUseByUid(Integer tenantId, Long uid);


    /**
     * 根据用户ID查询是否存在未使用且可退的订单<br />
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param rentRebateEndTime      可退截止时间，可为空，若为空，则默认取当前时间
     * @return true(存在未使用的订单)、false(不存在未使用的订单)
     */
    boolean isExitUnUseAndRefund(Integer tenantId, Long uid, Long rentRebateEndTime);

    /**
     * 根据用户ID查询是否存在未使用状态的订单<br />
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return true(存在未使用的订单)、false(不存在未使用的订单)
     */
    boolean isExitUnUseByUid(Integer tenantId, Long uid);

    /**
     * 根据套餐ID查询是否存在购买订单
     * @param rentalPackageId 套餐ID
     * @return true(存在)、false(不存在)
     */
    Boolean checkByRentalPackageId(Long rentalPackageId);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return 套餐购买订单集
     */
    List<CarRentalPackageOrderPo> list(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return 套餐购买订单集
     */
    List<CarRentalPackageOrderPo> page(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param queryModel 查询条件模型
     * @return 总数
     */
    Integer count(CarRentalPackageOrderQryModel queryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return 套餐购买订单
     */
    CarRentalPackageOrderPo selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 套餐购买订单
     */
    R<CarRentalPackageOrderPo> selectById(Long id);

    /**
     * 根据订单编号更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @return true(成功)、false(失败)
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState);

    /**
     * 根据订单编号更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @param remark 备注
     * @param optUid 操作人ID（可为空）
     * @return true(成功)、false(失败)
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState, String remark, Long optUid);

    /**
     * 根据ID更新支付状态
     * @param id 主键ID
     * @param payState 支付状态
     * @param remark 备注
     * @param optUid 操作人（可为空）
     * @return true(成功)、false(失败)
     */
    Boolean updatePayStateById(Long id, Integer payState, String remark, Long optUid);

    /**
     * 根据ID更新使用状态
     * @param orderNo 订单编码
     * @param useState 使用状态
     * @param optUid 操作人（可为空）
     * @return true(成功)、false(失败)
     */
    Boolean updateUseStateByOrderNo(String orderNo, Integer useState, Long optUid);

    /**
     * 根据ID更新使用状态
     * @param id 主键ID
     * @param useState 使用状态
     * @param optUid 操作人（可为空）
     * @return true(成功)、false(失败)
     */
    Boolean updateUseStateById(Long id, Integer useState, Long optUid);

    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return 主键ID
     */
    Long insert(CarRentalPackageOrderPo entity);

}
