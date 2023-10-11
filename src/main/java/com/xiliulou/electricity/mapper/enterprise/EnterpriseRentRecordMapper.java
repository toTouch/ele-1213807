package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户使用记录表(EnterpriseRentRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-10-10 20:03:40
 */
public interface EnterpriseRentRecordMapper extends BaseMapper<EnterpriseRentRecord> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseRentRecord queryById(Long id);
    
    /**
     * 修改数据
     *
     * @param enterpriseRentRecord 实例对象
     * @return 影响行数
     */
    int update(EnterpriseRentRecord enterpriseRentRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    EnterpriseRentRecord selectLatestRentRecord(Long uid);
    
    List<EnterpriseRentRecord> selectByUidAndTime(@Param("uid") Long uid, @Param("startTime") long startTime);
    
    int deleteByUid(Long uid);
}
