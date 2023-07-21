package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;

import java.util.List;

/**
 * 租车套餐订单逾期表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderSlippageService {

    /**
     * 根据用户ID查询未支付的逾期订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePO> selectUnPayByByUid(Integer tenantId, Long uid);

    /**
     * 距当前时间，是否存在未缴纳的逾期订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return true(存在)、false(不存在)
     */
    boolean isExitUnpaid(Integer tenantId, Long uid);
    
    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePO> list(CarRentalPackageOrderSlippageQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePO> page(CarRentalPackageOrderSlippageQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return 总数
     */
    Integer count(CarRentalPackageOrderSlippageQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return 逾期订单信息
     */
    CarRentalPackageOrderSlippagePO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 逾期订单信息
     */
    CarRentalPackageOrderSlippagePO selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return 主键ID
     */
    Long insert(CarRentalPackageOrderSlippagePO entity);
}
