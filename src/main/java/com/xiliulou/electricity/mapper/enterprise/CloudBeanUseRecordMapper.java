package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;

import java.util.List;

import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 云豆使用记录表(CloudBeanUseRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-18 10:35:12
 */
public interface CloudBeanUseRecordMapper extends BaseMapper<CloudBeanUseRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CloudBeanUseRecord queryById(Long id);

    /**
     * 修改数据
     *
     * @param cloudBeanUseRecord 实例对象
     * @return 影响行数
     */
    int update(CloudBeanUseRecord cloudBeanUseRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    double selectCloudBeanByEnterpriseIdAndType(@Param("enterpriseId") Long enterpriseId, @Param("type") Integer type);

    List<CloudBeanUseRecord> selectByUserPage(CloudBeanUseRecordQuery query);
    
    List<CloudBeanUseRecord> selectByEnterpriseIdAndType(@Param("enterpriseId") Long enterpriseId, @Param("type") Integer type);
    
    List<CloudBeanUseRecord> selectCanRecycleRecord(@Param("enterpriseId") Long enterpriseId, @Param("currentTimeMillis") long currentTimeMillis);
    
    List<CloudBeanUseRecord> selectByTime(@Param("beginTime") Long beginTime, @Param("endTime") Long endTime, @Param("enterpriseId") Long enterpriseId);
}
