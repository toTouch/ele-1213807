package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;

import java.util.List;

/**
 * 租车套餐押金退款表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageDepositRefundService {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageDepositRefundPO>> list(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageDepositRefundPO>> page(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    R<CarRentalPackageDepositRefundPO> selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    R<CarRentalPackageDepositRefundPO> selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param optModel 操作模型
     * @return
     */
    R<Long> insert(CarRentalPackageDepositRefundOptModel optModel);
}
