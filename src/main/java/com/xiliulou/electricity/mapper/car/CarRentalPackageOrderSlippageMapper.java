package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐订单逾期表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageOrderSlippageMapper {

    /**
     * 根据用户ID查询未支付的逾期订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePO> selectUnPaidByByUid(@Param("tenantId") Integer tenantId, @Param("tenantId") Long uid);

    /**
     * 是否存在未缴纳的逾期订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param time 时间
     * @return 总条数
     */
    Integer isExitUnpaid(@Param("tenantId") Integer tenantId, @Param("tenantId") Long uid, @Param("tenantId")Long time);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePO> list(CarRentalPackageOrderSlippageQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return 逾期订单信息集
     */
    List<CarRentalPackageOrderSlippagePO> page(CarRentalPackageOrderSlippageQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
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
     * 插入
     * @param entity 实体类
     * @return 操作条数
     */
    int insert(CarRentalPackageOrderSlippagePO entity);
}
