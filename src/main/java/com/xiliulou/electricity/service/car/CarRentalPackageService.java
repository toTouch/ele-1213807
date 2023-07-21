package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;

import java.util.List;

/**
 * 租车套餐表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageService {

    /**
     * 根据主键ID查询，不区分是否删除
     * @param ids 主键ID集
     * @return
     */
    List<CarRentalPackagePO> selectByIds(List<Long> ids);

    /**
     * 根据条件查询<br />
     * @param qryModel
     * @return
     */
    List<CarRentalPackagePO> listByCondition(CarRentalPackageQryModel qryModel);

    /**
     * 检测唯一：租户ID+套餐名称
     * @param tenantId 租户ID
     * @param name 套餐名称
     * @return true(存在)、false(不存在)
     */
    Boolean uqByTenantIdAndName(Integer tenantId, String name);

    /**
     * 根据ID修改上下架状态
     * @param id 主键ID
     * @param status 上下架状态
     * @param uid 操作人ID
     * @return
     */
    Boolean updateStatusById(Long id, Integer status, Long uid);

    /**
     * 根据ID删除
     * @param id 主键ID
     * @param uid 操作人ID
     * @return
     */
    Boolean delById(Long id, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackagePO> list(CarRentalPackageQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackagePO> page(CarRentalPackageQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    Integer count(CarRentalPackageQryModel qryModel);

    /**
     * 根据ID查询<br />
     * 优先查询缓存，缓存没有查询DB，懒加载缓存<br />
     * 可能返回<code>null</code>
     * @param id 主键ID
     * @return
     */
    CarRentalPackagePO selectById(Long id);

    /**
     * 根据ID更新
     * @param entity 实体数据
     * @return
     */
    Boolean updateById(CarRentalPackagePO entity);

    /**
     * 新增数据，返回主键ID<br />
     * 若为车电一体，则会联动调用换电套餐的逻辑
     * @param entity 实体数据
     * @return
     */
    Long insert(CarRentalPackagePO entity);

}
