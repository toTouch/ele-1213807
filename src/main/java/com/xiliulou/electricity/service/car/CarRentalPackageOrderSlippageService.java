package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;

import java.util.List;

/**
 * 租车套餐订单逾期表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderSlippageService {

    /**
     * 根据主键ID进行更新
     * @param entity 操作实体
     * @return true(成功)、false(失败)
     */
    boolean updateById(CarRentalPackageOrderSlippagePo entity);

    /**
     * 根据套餐购买订单编号和逾期订单类型，查询未支付的逾期订单信息
     * @param rentalPackageOrderNo 套餐购买订单编码
     * @param type 逾期订单类型：1-过期、2-冻结
     * @return 逾期订单信息
     */
    CarRentalPackageOrderSlippagePo selectByPackageOrderNoAndType(String rentalPackageOrderNo, Integer type);

    /**
     * 根据用户ID查询未支付的逾期订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePo> selectUnPayByByUid(Integer tenantId, Long uid);

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
    List<CarRentalPackageOrderSlippagePo> list(CarRentalPackageOrderSlippageQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePo> page(CarRentalPackageOrderSlippageQryModel qryModel);

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
    CarRentalPackageOrderSlippagePo selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 逾期订单信息
     */
    CarRentalPackageOrderSlippagePo selectById(Long id);

    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return 主键ID
     */
    Long insert(CarRentalPackageOrderSlippagePo entity);
}
