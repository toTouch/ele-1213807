package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.SysOperLog;

import java.util.List;

import com.xiliulou.electricity.query.SysOperLogQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 操作日志记录(SysOperLog)表数据库访问层
 *
 * @author zzlong
 * @since 2022-10-11 19:47:27
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    SysOperLog selectById(Long id);

    /**
     * 查询指定行数据
     * @return 对象列表
     */
    List<SysOperLog> selectByPage(SysOperLogQuery sysOperLogQuery);
    
    int pageCount(SysOperLogQuery sysOperLogQuery);
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param sysOperLog 实例对象
     * @return 对象列表
     */
    List<SysOperLog> selectByQuery(SysOperLog sysOperLog);

    /**
     * 新增数据
     *
     * @param sysOperLog 实例对象
     * @return 影响行数
     */
    int insertOne(SysOperLog sysOperLog);

    /**
     * 修改数据
     *
     * @param sysOperLog 实例对象
     * @return 影响行数
     */
    int update(SysOperLog sysOperLog);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
}
