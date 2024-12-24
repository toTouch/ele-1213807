package com.xiliulou.electricity.aspectj;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.annotation.IdempotentCheck;
import com.xiliulou.electricity.annotation.ParamIdempotent;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 幂等性接口校验切面，防止相同参数重复操作
 *
 * @author: caobotao.cbt
 * @version: 1.0
 */
@Slf4j
@Component
@Aspect
public class IdempotentAspect {
    
    @Resource
    private RedisService redisService;
    
    public static final String KEY_PREFIX = "idempotent%s:%s";
    
    
    @Pointcut("@annotation(com.xiliulou.electricity.annotation.IdempotentCheck)")
    public void pointcut() {
    }
    
    @Around("pointcut()")
    public Object idempotent(ProceedingJoinPoint pjp) throws Throwable {
        ProceedingJoinPointParseModel proceedingJoinPointParseModel = new ProceedingJoinPointParseModel(pjp);
        
        if (!this.idempotentLock(proceedingJoinPointParseModel)) {
            throw new CustomBusinessException("操作频繁");
        }

        return pjp.proceed();
    }
    
    /**
     * 幂等锁-redis分布式锁，拦截有{@link IdempotentCheck} 注解的方法。
     * <p>
     * 加锁过程中出现异常情况，或者是配置错误，将正常返回<pp>true</pp>,不影响正常业务执行逻辑<br/> 返回<pp>false</pp>的情况只有一种 就是加锁失败
     * {@link redisService#setNx(String, String, Long, boolean)}
     * </p>
     * <p>
     * key 获取方式:<br/>1.参数未加注解{@link ParamIdempotent},则根据类名+方法名+tenantId+uid进行幂等校验<br/>
     * 2.参数加注解{@link ParamIdempotent}未指定{@link ParamIdempotent#value()},则根据类名+方法名+tenantId+uid+当前参数的Json字符串幂等校验<br/>
     * 3.参数加注解{@link ParamIdempotent}并指定{@link ParamIdempotent#value()},则根据类名+方法名+tenantId+uid+指定的参数进行幂等校验<br/>
     * </p>
     *
     * @author caobotao.cbt
     */
    private Boolean idempotentLock(ProceedingJoinPointParseModel pjp) {
        try {
            
            Integer tenantId = -1;
            Long uid = -1L;
            
            TokenUser securityUser = SecurityUtils.getUserInfo();
            if (Objects.nonNull(securityUser)) {
                tenantId = securityUser.getTenantId();
                uid = securityUser.getUid();
            }
            
            IdempotentCheck annotation = pjp.getAnnotation();
            String className = pjp.getSimpleClassName();
            String methodName = pjp.getMethod().getName();
            Object[] args = pjp.getArgs();
            Parameter[] parameters = pjp.getMethod().getParameters();

            long timeout = annotation.requestIntervalMilliseconds();
            String prefix = annotation.prefix();

            String idempotentKey = this.generateKeyParam(tenantId, uid, className, methodName, args, parameters);
            if (StringUtils.isBlank(idempotentKey)) {
                return true;
            }
            
            String MD5 = DigestUtils.md5Hex(idempotentKey);
            if (StringUtils.isNotEmpty(prefix)) {
                prefix = StringConstant.UNDERLINE + prefix;
            }

            String key = String.format(KEY_PREFIX, prefix, MD5);
            String clientId = UUID.randomUUID().toString();

            return redisService.setNx(key, clientId, timeout, false);
        } catch (Exception e) {
            log.error("IDEMPOTENT ASPECT ERROR:", e);
            return true;
        }
        
    }
    
    /**
     * 根据参数生成key
     *
     * @author caobotao.cbt
     * @date 2024/5/31 10:53
     */
    private String generateKeyParam(Integer tenantId, Long uid, String className, String methodName, Object[] args, Parameter[] parameters)
            throws IllegalAccessException {
        
        StringBuilder sb = new StringBuilder();
        sb.append(className).append("#").append(methodName).append("&tenantId=").append(tenantId).append("&uid=")
                .append(uid);
        if (Objects.isNull(args) || args.length == 0 || Objects.isNull(parameters)
                || parameters.length == 0 || args.length != parameters.length) {
            return sb.toString();
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            ParamIdempotent annotation = parameters[i].getAnnotation(ParamIdempotent.class);
            if (Objects.isNull(annotation)) {
                continue;
            }

            if (isPrimitiveOrWrapper(arg.getClass())) {
                sb.append("&param").append(i).append("=").append(arg);
                continue;
            }
            
            if (Objects.isNull(annotation.value()) || annotation.value().length == 0) {
                sb.append("&param").append(i).append("=").append(JsonUtil.toJson(arg));
                continue;
            }

            sb.append("&param").append(i).append("=");

            List<String> fieldNames = Arrays.asList(annotation.value());
            Field[] declaredFields = arg.getClass().getDeclaredFields();
            HashMap<String, Object> fieldNameMap = new LinkedHashMap<>();
            for (Field declaredField : declaredFields) {
                if (!fieldNames.contains(declaredField.getName())) {
                    continue;
                }

                declaredField.setAccessible(true);
                fieldNameMap.put(declaredField.getName(), declaredField.get(arg));
            }

            sb.append(JsonUtil.toJson(fieldNameMap));
        }

        return sb.toString();
    }
    
    
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isAssignableFrom(String.class);
    }
    
    
    @Data
    static class ProceedingJoinPointParseModel {
        
        public ProceedingJoinPointParseModel(ProceedingJoinPoint pjp) {
            this.simpleClassName = pjp.getTarget().getClass().getSimpleName();
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            this.method = methodSignature.getMethod();
            this.annotation = this.method.getAnnotation(IdempotentCheck.class);
            this.args = pjp.getArgs();
        }
        
        private String simpleClassName;
        
        private Method method;
        
        private Object[] args;
        
        private IdempotentCheck annotation;
    }
    
    
}
