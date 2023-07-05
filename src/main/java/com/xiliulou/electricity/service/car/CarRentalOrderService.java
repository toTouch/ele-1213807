package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalOrderPO;
import com.xiliulou.electricity.model.car.opt.CarRentalOrderOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;

import java.util.List;

/**
 * 车辆租赁订单表 Service
 * @author xiaohui.song
 **/
public interface CarRentalOrderService {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalOrderPO>> list(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalOrderPO>> page(CarRentalOrderQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalOrderQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    R<CarRentalOrderPO> selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    R<CarRentalOrderPO> selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param optModel 操作模型
     * @return
     */
    R<Long> insert(CarRentalOrderOptModel optModel);

}
