package com.xiliulou.electricity.aspectj;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * @Description 请求参数处理切面类
 * @Author: SongJP
 * @Date: 2024/8/28 11:18
 */
@Aspect
@Component
@Slf4j
public class ProcessParameterAspect {
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @Around("@annotation(processParameter)")
    public Object processParameter(ProceedingJoinPoint joinPoint, ProcessParameter processParameter) throws Throwable {
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok();
        }
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok();
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        Object[] args = joinPoint.getArgs();
        Object requestQuery = args[0];
        Class<?> clazz = requestQuery.getClass();
        
        // 给请求对象设置数据权限查询条件tenantId、franchiseeIds
        Field tenantIdField = findField(clazz, "tenantId");
        Field franchiseeIdsField = findField(clazz, "franchiseeIds");
        if (Objects.isNull(tenantIdField) || Objects.isNull(franchiseeIdsField)) {
            throw new BizException("方法请求参数对象属性无tenantId、franchiseeIds");
        }
        tenantIdField.setAccessible(true);
        tenantIdField.set(clazz, tenantId);
        franchiseeIdsField.setAccessible(true);
        franchiseeIdsField.set(clazz, franchiseeIds);
        
        if (Objects.equals(InstallmentConstants.PROCESS_PARAMETER_TYPE_PAGE, processParameter.type())) {
            handlePageRequest(clazz);
        }
        return joinPoint.proceed(args);
    }
    
    private void handlePageRequest(Class<?> clazz) throws Exception {
        Field offsetField = findField(clazz, "offset");
        Field sizeField = findField(clazz, "size");
        if (Objects.isNull(offsetField) || Objects.isNull(sizeField)) {
            throw new BizException("方法请求参数对象中无分页参数");
        }
        
        offsetField.setAccessible(true);
        sizeField.setAccessible(true);
        
        Integer offset = (Integer) offsetField.get(clazz);
        Integer size = (Integer) sizeField.get(clazz);
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
            offsetField.set(clazz, offset);
        }
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10;
            sizeField.set(clazz, size);
        }
    }
    
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果当前类中找不到，递归地查找父类
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findField(superClass, fieldName);
            }
        }
        return null;
    }
}
