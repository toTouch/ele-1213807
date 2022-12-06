package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.SysOperLog;
import com.xiliulou.electricity.query.SysOperLogQuery;
import com.xiliulou.electricity.vo.SysOperLogVO;

import java.util.List;

/**
 * 操作日志记录(SysOperLog)表服务接口
 *
 * @author zzlong
 * @since 2022-10-11 19:47:27
 */
public interface SysOperLogService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    SysOperLog selectByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    SysOperLog selectByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<SysOperLogVO> selectByPage(SysOperLogQuery sysOperLogQuery);
    
    int pageCount(SysOperLogQuery sysOperLogQuery);
    
    /**
     * 新增数据
     *
     * @param sysOperLog 实例对象
     * @return 实例对象
     */
    SysOperLog insert(SysOperLog sysOperLog);
    
    /**
     * 修改数据
     *
     * @param sysOperLog 实例对象
     * @return 实例对象
     */
    Integer update(SysOperLog sysOperLog);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
}
