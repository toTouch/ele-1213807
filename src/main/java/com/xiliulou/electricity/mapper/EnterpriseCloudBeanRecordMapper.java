package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 企业云豆操作记录表(EnterpriseCloudBeanRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-14 10:16:20
 */
public interface EnterpriseCloudBeanRecordMapper extends BaseMapper<EnterpriseCloudBeanRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseCloudBeanRecord queryById(Long id);

    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanRecord 实例对象
     * @return 影响行数
     */
    int update(EnterpriseCloudBeanRecord enterpriseCloudBeanRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
