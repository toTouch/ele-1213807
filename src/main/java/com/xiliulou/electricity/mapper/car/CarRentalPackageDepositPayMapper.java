package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐押金缴纳订单表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageDepositPayMapper {

    /**
     * 根据用户ID和租户ID查询最后一条押金信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 押金支付订单
     */
    CarRentalPackageDepositPayPO selectLastByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 根据订单编码更新支付状态
     * @param orderNo 订单编码
     * @param payState 支付状态
     * @param remark 备注
     * @param uid 操作人
     * @param optTime 操作时间
     * @return
     */
    int updatePayStateByOrderNo(@Param("orderNo") String orderNo, @Param("payState") Integer payState, @Param("remark") String remark, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据用户ID和租户ID查询支付成功且未退的押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    CarRentalPackageDepositPayPO selectUnRefundCarDeposit(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageDepositPayPO> list(CarRentalPackageDepositPayQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageDepositPayPO> page(CarRentalPackageDepositPayQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageDepositPayQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageDepositPayPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageDepositPayPO selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageDepositPayPO entity);
}
