package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;

import java.util.List;

/**
 * 租车套餐押金退款表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageDepositRefundService {

    /**
     * 根据押金缴纳订单编码，查询最后一笔的退押订单信息
     * @param depositPayOrderNo 押金缴纳编码
     * @return 押金退款订单编码
     */
    CarRentalPackageDepositRefundPo selectLastByDepositPayOrderNo(String depositPayOrderNo);

    /**
     * 根据退押申请单编码进行更新
     * @param entity 实体数据
     * @return
     */
    boolean updateByOrderNo(CarRentalPackageDepositRefundPo entity);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageDepositRefundPo> list(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageDepositRefundPo> page(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    Integer count(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageDepositRefundPo selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageDepositRefundPo selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 实体数据
     * @return
     */
    Long insert(CarRentalPackageDepositRefundPo entity);
}
