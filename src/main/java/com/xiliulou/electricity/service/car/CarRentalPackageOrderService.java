package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;

import java.util.List;

/**
 * 租车套餐订单表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderService {

    /**
     * 根据套餐ID查询是否存在购买订单
     * @param rentalPackageId
     * @return
     */
    R<Boolean> checkByRentalPackageId(Long rentalPackageId);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    R<List<CarRentalPackageOrderPO>> list(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    R<List<CarRentalPackageOrderPO>> page(CarRentalPackageOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param queryModel 查询条件模型
     * @return
     */
    R<Integer> count(CarRentalPackageOrderQryModel queryModel);

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
    R<CarRentalPackageOrderPO> selectById(Long id);

    /**
     * 根据订单编号更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @return
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState);

    /**
     * 根据订单编号更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @return
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState, String remark, Long uid);

    /**
     * 根据ID更新支付状态
     * @param id 主键ID
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @return
     */
    R<Boolean> updatePayStateById(Long id, Integer payState, String remark, Long uid);

    /**
     * 根据ID更新使用状态
     * @param orderNo 订单编码
     * @param useState 使用状态
     * @param uid 操作人
     * @return
     */
    R<Boolean> updateUseStateByOrderNo(String orderNo, Integer useState, Long uid);

    /**
     * 根据ID更新使用状态
     * @param id 主键ID
     * @param useState 使用状态
     * @param uid 操作人
     * @return
     */
    R<Boolean> updateUseStateById(Long id, Integer useState, Long uid);

    /**
     * 新增数据，返回主键ID
     * @param optModel 操作数据
     * @return
     */
    R<Long> insert(CarRentalPackageOrderOptModel optModel);

}
