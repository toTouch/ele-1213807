package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderRentRefundQryModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐订单租金退款表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderRentRefundService {

    /**
     * 根据用户UID查询退款成功的总金额
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 总金额
     */
    BigDecimal selectRefundSuccessAmountTotal(Integer tenantId, Long uid);

    /**
     * 根据退租申请单编码进行更新
     * @param entity 实体数据
     * @return
     */
    boolean updateByOrderNo(CarRentalPackageOrderRentRefundPo entity);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageOrderRentRefundPo> list(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageOrderRentRefundPo> page(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    Integer count(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageOrderRentRefundPo selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageOrderRentRefundPo selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return
     */
    Long insert(CarRentalPackageOrderRentRefundPo entity);


}
