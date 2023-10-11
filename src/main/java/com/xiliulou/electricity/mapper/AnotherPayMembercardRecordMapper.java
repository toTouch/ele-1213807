package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 代付记录表(AnotherPayMembercardRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-10-10 15:07:39
 */
public interface AnotherPayMembercardRecordMapper extends BaseMapper<AnotherPayMembercardRecord> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AnotherPayMembercardRecord queryById(Long id);
    
    /**
     * 修改数据
     *
     * @param anotherPayMembercardRecord 实例对象
     * @return 影响行数
     */
    int update(AnotherPayMembercardRecord anotherPayMembercardRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    int deleteByUid(Long uid);
    
    AnotherPayMembercardRecord selectLatestByUid(Long uid);
}
