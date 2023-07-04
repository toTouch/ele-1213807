package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;

import java.util.List;

/**
 * 租车套餐业务接口定义
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageService {

    /**
     * 根据条件查询<br />
     * PS：<br />
     * 1、不区分租户<br />
     * 2、不区分删除<br />
     * @param qryModel
     * @return
     */
    R<List<CarRentalPackagePO>> listByCondition(CarRentalPackageQryModel qryModel);

    /**
     * 检测唯一：租户ID+套餐名称
     * @param tenantId 租户ID
     * @param name 套餐名称
     * @return
     */
    R<Boolean> uqByTenantIdAndName(Integer tenantId, String name);

    /**
     * 根据ID修改上下架状态
     * @param id 主键ID
     * @param status 上下架状态
     * @param uid 操作人ID
     * @return
     */
    R<Boolean> updateStatusById(Long id, Integer status, Long uid);

    /**
     * 根据ID删除
     * @param id 主键ID
     * @param uid 操作人ID
     * @return
     */
    R<Boolean> delById(Long id, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackagePO>> list(CarRentalPackageQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackagePO>> page(CarRentalPackageQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalPackageQryModel qryModel);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    R<CarRentalPackagePO> selectById(Long id);

    /**
     * 根据ID更新
     * @param optModel 操作模型
     * @return
     */
    R<Boolean> updateById(CarRentalPackageOptModel optModel);

    /**
     * 新增数据，返回主键ID
     * @param optModel 操作模型
     * @return
     */
    R<Long> insert(CarRentalPackageOptModel optModel);

}
