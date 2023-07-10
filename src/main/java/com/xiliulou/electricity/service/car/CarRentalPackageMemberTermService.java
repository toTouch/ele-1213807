package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageMemberTermOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;

import java.util.List;

/**
 * 租车套餐会员期限表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageMemberTermService {

    /**
     * 根据用户ID和租户ID更新状态
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param status 状态
     * @param optId 操作人ID（可以为空）
     * @return
     */
    Boolean updateStatusByUidAndTenantId(Integer tenantId, Long uid, Integer status, Long optId);

    /**
     * 根据主键ID更新状态
     * @param id 主键ID
     * @param status 状态
     * @param optId 操作人（可以为空）
     * @return
     */
    Boolean updateStatusById(Long id, Integer status, Long optId);

    /**
     * 根据主键ID更新数据
     * @param optModel
     * @return
     */
    Boolean updateById(CarRentalPackageMemberTermOptModel optModel);

    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息<br />
     * 优先查询缓存，缓存没有查询DB，懒加载缓存<br />
     * 可能返回<code>null</code>
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    CarRentalPackageMemberTermPO selectByTenantIdAndUid(Integer tenantId, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageMemberTermPO>> list(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageMemberTermPO>> page(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    R<CarRentalPackageMemberTermPO> selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return
     */
    Long insert(CarRentalPackageMemberTermPO entity);
    
}
