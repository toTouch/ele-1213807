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
