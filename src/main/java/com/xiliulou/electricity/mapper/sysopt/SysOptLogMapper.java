package com.xiliulou.electricity.mapper.sysopt;

import com.xiliulou.electricity.entity.sysopt.SysOptLog;
import com.xiliulou.electricity.model.sysopt.query.SysOptLogQryModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志 Mapper
 **/
@Mapper
public interface SysOptLogMapper {
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询条件模型
     * @return 系统操作记录集
     */
    List<SysOptLog> selectPageByCondition(SysOptLogQryModel qryModel);
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询条件模型
     * @return 总数
     */
    Integer countByCondition(SysOptLogQryModel qryModel);
    
    
    /**
     * 插入
     *
     * @param entity 实体类
     * @return 操作条数
     */
    int insert(SysOptLog entity);
    
}
