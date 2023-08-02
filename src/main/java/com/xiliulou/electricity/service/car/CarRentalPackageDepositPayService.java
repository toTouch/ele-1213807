package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;

import java.util.List;

/**
 * 租车套餐押金缴纳订单表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageDepositPayService {

    /**
     * 同步免押状态
     * @param orderNo 押金缴纳订单编码
     * @param optUid 操作人ID
     * @return true(成功)、false(失败)
     */
    boolean syncFreeState(String orderNo, Long optUid);

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
     * @param uid 操作人
     * @return true(成功)、false(失败)
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState, String remark, Long uid);

    /**
     * 根据用户ID和租户ID查询支付成功的最后一条押金信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 押金支付订单
     */
    CarRentalPackageDepositPayPO selectLastPaySucessByUid(Integer tenantId, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageDepositPayPO> list(CarRentalPackageDepositPayQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageDepositPayPO> page(CarRentalPackageDepositPayQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    Integer count(CarRentalPackageDepositPayQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageDepositPayPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageDepositPayPO selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return
     */
    Long insert(CarRentalPackageDepositPayPO entity);
}
