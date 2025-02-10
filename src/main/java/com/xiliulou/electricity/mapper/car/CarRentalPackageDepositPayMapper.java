package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.domain.car.UserDepositPayTypeDO;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 租车套餐押金缴纳订单表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageDepositPayMapper {
    
    /**
     * 查询用户最后一次的免押订单生成信息
     * @param tenantId
     * @param uid
     * @return
     */
    CarRentalPackageDepositPayPo selectLastFreeOrderByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);
    
    /**
     * 根据用户ID和租户ID查询最后一条押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金支付订单
     */
    CarRentalPackageDepositPayPo selectLastByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);
    
    /**
     * 根据订单编码更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @param remark   备注
     * @param uid      操作人
     * @param optTime  操作时间
     * @return
     */
    int updatePayStateByOrderNo(@Param("orderNo") String orderNo, @Param("payState") Integer payState, @Param("remark") String remark, @Param("uid") Long uid,
            @Param("optTime") Long optTime);
    
    /**
     * 根据用户ID和租户ID查询支付成功且未退的押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    CarRentalPackageDepositPayPo selectUnRefundCarDeposit(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);
    
    /**
     * 条件查询列表<br /> 全表扫描，慎用
     *
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageDepositPayPo> list(CarRentalPackageDepositPayQryModel qryModel);
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageDepositPayPo> page(CarRentalPackageDepositPayQryModel qryModel);
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageDepositPayQryModel qryModel);
    
    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageDepositPayPo selectByOrderNo(String orderNo);
    
    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    CarRentalPackageDepositPayPo selectById(Long id);
    
    /**
     * 插入
     *
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageDepositPayPo entity);
    
    
    List<UserDepositPayTypeDO> selectPayTypeByOrders(@Param("list") Collection<String> ordersOn);
    
    List<CarRentalPackageDepositPayPo> selectListByOrders(@Param("tenantId") Integer tenantId, @Param("orderNoList") List<String> orderNoList);
}
