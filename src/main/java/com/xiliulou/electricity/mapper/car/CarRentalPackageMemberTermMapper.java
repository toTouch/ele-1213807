package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐会员期限表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageMemberTermMapper {

    /**
     * 根据用户ID和套餐购买订单编码进行退租
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid 操作人ID
     * @param optTime 操作时间
     * @return
     */
    int rentRefundByUidAndPackageOrderNo(@Param("uid") Long uid, @Param("packageOrderNo") String packageOrderNo, @Param("optUid") Long optUid, @Param("optTime") Long optTime);

    /**
     * 根据用户ID和套餐订单编码查询
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 套餐购买订单编码
     * @return
     */
    CarRentalPackageMemberTermPO selectByUidAndPackageOrderNo(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("packageOrderNo") String packageOrderNo);

    /**
     * 根据用户ID和租户ID更新状态
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param optId 操作人ID（可以为空）
     * @param optTime 操作时间
     * @return
     */
    int delByUidAndTenantId(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据用户ID和租户ID更新状态
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param status 状态
     * @param optId 操作人ID（可以为空）
     * @param optTime 操作时间
     * @return
     */
    int updateStatusByUidAndTenantId(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("status") Integer status, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据主键ID更新状态
     * @param id 主键ID
     * @param status 状态
     * @param optId 操作人（可以为空）
     * @param optTime 操作时间
     * @return
     */
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据主键ID更新数据
     * @param entity
     * @return
     */
    int updateById(CarRentalPackageMemberTermPO entity);

    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    CarRentalPackageMemberTermPO selectByTenantIdAndUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageMemberTermPO> list(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageMemberTermPO> page(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageMemberTermPO selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageMemberTermPO entity);

}
