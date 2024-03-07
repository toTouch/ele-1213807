package com.xiliulou.electricity.service.sysopt;

import com.xiliulou.electricity.entity.sysopt.SysOptLog;
import com.xiliulou.electricity.model.sysopt.query.SysOptLogQryModel;

import java.util.List;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志 Service
 **/
public interface SysOptLogService {
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 系统操作记录集
     */
    List<SysOptLog> listPageByCondition(SysOptLogQryModel qryModel);
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    Integer countByCondition(SysOptLogQryModel qryModel);
    
    /**
     * 新增数据，返回主键ID
     *
     * @param entity 实体数据
     * @return 主键ID
     */
    Long insert(SysOptLog entity);
    
}
