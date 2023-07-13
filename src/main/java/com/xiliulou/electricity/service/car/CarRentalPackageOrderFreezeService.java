package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;

import java.util.List;

/**
 * 租车套餐订单冻结表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderFreezeService {

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageOrderFreezePO>> list(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageOrderFreezePO>> page(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    R<CarRentalPackageOrderFreezePO> selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    R<CarRentalPackageOrderFreezePO> selectById(Long id);

    /**
     * 新增数据，返回主键ID
     * @param entity 实体模型
     * @return
     */
    Long insert(CarRentalPackageOrderFreezePO entity);
    
}
