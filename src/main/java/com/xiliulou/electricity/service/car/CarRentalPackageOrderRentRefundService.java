package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderRentRefundOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderRentRefundQryModel;

import java.util.List;

/**
 * 租车套餐订单租金退款表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderRentRefundService {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageOrderRentRefundPO>> list(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageOrderRentRefundPO>> page(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalPackageOrderRentRefundQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    R<CarRentalPackageOrderRentRefundPO> selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    R<CarRentalPackageOrderRentRefundPO> selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param optModel 操作模型
     * @return
     */
    R<Long> insert(CarRentalPackageOrderRentRefundOptModel optModel);
}
