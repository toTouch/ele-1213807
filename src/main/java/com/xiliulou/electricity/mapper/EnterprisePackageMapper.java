package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 企业关联套餐表(EnterprisePackage)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-14 10:15:43
 */
public interface EnterprisePackageMapper extends BaseMapper<EnterprisePackage> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterprisePackage queryById(Long id);

    /**
     * 修改数据
     *
     * @param enterprisePackage 实例对象
     * @return 影响行数
     */
    int update(EnterprisePackage enterprisePackage);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
