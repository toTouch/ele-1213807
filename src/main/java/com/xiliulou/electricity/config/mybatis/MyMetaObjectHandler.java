package com.xiliulou.electricity.config.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.springframework.stereotype.Component;

/**
 * mybatisplus自动填充
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    
    /**
     * 创建时间
     */
    private static final String CREATE_TIME = "createTime";
    
    /**
     * 更新时间
     */
    private static final String UPDATE_TIME = "updateTime";
    
    private static final String OP_USER = "opUser";
    
    private static final String TENANT_ID = "tenantId";
    
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ....");
        try {
            //为空则设置createTime
            Object createTime = metaObject.getValue(CREATE_TIME);
            if (ObjectUtil.isNull(createTime)) {
                setFieldValByName(CREATE_TIME, System.currentTimeMillis(), metaObject);
            }
        } catch (ReflectionException ignored) {
        }
        try {
            //为空则设置updateTime
            Object updateTime = metaObject.getValue(UPDATE_TIME);
            if (ObjectUtil.isNull(updateTime)) {
                setFieldValByName(UPDATE_TIME, System.currentTimeMillis(), metaObject);
            }
        } catch (ReflectionException ignored) {
        }
        
        try {
            Object opUser = metaObject.getValue(OP_USER);
            if (ObjectUtil.isNull(opUser)) {
                setFieldValByName(OP_USER, SecurityUtils.getUid(), metaObject);
            }
        } catch (ReflectionException ignored) {
        }
        
        try {
            Object tenantId = metaObject.getValue(TENANT_ID);
            if (ObjectUtil.isNull(tenantId)) {
                setFieldValByName(TENANT_ID, TenantContextHolder.getTenantId(), metaObject);
            }
        } catch (ReflectionException ignored) {
        }
        
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            setFieldValByName(UPDATE_TIME, System.currentTimeMillis(), metaObject);
        } catch (ReflectionException ignored) {
        }
        
        try {
            setFieldValByName(OP_USER, SecurityUtils.getUid(), metaObject);
        } catch (ReflectionException ignored) {
        }
    }
}
