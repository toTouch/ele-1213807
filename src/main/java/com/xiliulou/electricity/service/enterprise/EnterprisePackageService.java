package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;

import java.util.List;

/**
 * 企业关联套餐表(EnterprisePackage)表服务接口
 *
 * @author zzlong
 * @since 2023-09-14 10:15:43
 */
public interface EnterprisePackageService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterprisePackage queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterprisePackage queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param enterprisePackage 实例对象
     * @return 实例对象
     */
    Integer update(EnterprisePackage enterprisePackage);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    void batchInsert(List<EnterprisePackage> packageList);

    List<Long> selectByEnterpriseId(Long id);
}
