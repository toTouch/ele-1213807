package com.xiliulou.electricity.service.impl.sysopt;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.sysopt.SysOptLog;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.sysopt.SysOptLogMapper;
import com.xiliulou.electricity.model.sysopt.query.SysOptLogQryModel;
import com.xiliulou.electricity.service.sysopt.SysOptLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志 ServiceImpl
 **/
@Slf4j
@Service
public class SysOptLogServiceImpl implements SysOptLogService {
    
    @Resource
    private SysOptLogMapper mapper;
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 系统操作记录集
     */
    @Slave
    @Override
    public List<SysOptLog> listPageByCondition(SysOptLogQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new SysOptLogQryModel();
        }
        
        return mapper.selectPageByCondition(qryModel);
    }
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    @Slave
    @Override
    public Integer countByCondition(SysOptLogQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new SysOptLogQryModel();
        }
        
        return mapper.countByCondition(qryModel);
    }
    
    /**
     * 新增数据，返回主键ID
     *
     * @param entity 实体数据
     * @return 主键ID
     */
    @Override
    public Long insert(SysOptLog entity) {
        if (ObjectUtils.anyNotNull(entity, entity.getTenantId(), entity.getType(), entity.getCreateUid(), entity.getOptIp(), entity.getContent())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        if (ObjectUtils.isEmpty(entity.getCreateTime())) {
            entity.setCreateTime(System.currentTimeMillis());
        }
        
        mapper.insert(entity);
        
        return entity.getId();
    }
}
