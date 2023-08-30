package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐Mapper操作类
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageMapper {

    /**
     * 根据主键ID查询，不区分是否删除
     * @param idList 主键ID集
     * @return
     */
    List<CarRentalPackagePo> selectByIds(@Param("idList") List<Long> idList);

    /**
     * 检测唯一：租户ID+套餐名称
     * @param tenantId 租户ID
     * @param name 套餐名称
     * @return 总数
     */
    int uqByTenantIdAndName(@Param("tenantId") Integer tenantId, @Param("name") String name);

    /**
     * 根据ID修改上下架状态
     * @param id 逐渐ID
     * @param uid 操作人ID
     * @param status 上下架状态
     * @param optTime 修改时间
     * @return
     */
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 根据ID删除-逻辑删
     * @param id 主键ID
     * @param uid 操作人ID
     * @param optTime 修改时间
     * @return
     */
    int delById(@Param("id") Long id, @Param("uid") Long uid, @Param("optTime") Long optTime);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackagePo> list(CarRentalPackageQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackagePo> page(CarRentalPackageQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageQryModel qryModel);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackagePo selectById(Long id);

    /**
     * 根据ID更新
     * @param entity 实体类
     * @return
     */
    int updateById(CarRentalPackagePo entity);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackagePo entity);

    /**
     * 根据coupon Id 查询套餐信息
     * @param couponId
     * @return
     */
    List<CarRentalPackagePo> selectByCouponId(@Param("couponId") Long couponId);

}
