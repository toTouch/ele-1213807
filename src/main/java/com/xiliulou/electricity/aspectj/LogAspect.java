package com.xiliulou.electricity.aspectj;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.SysOperLog;
import com.xiliulou.electricity.filter.RequestFilter;
import com.xiliulou.electricity.service.SysOperLogService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ServletUtils;
import com.xiliulou.electricity.utils.WebUtils;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * 操作日志记录处理
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-10-11-17:42
 */
@Aspect
@Component
public class LogAspect {
    
    private ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("eleOperateLogExecutor", 1, "ELE_OPERATE_LOG_EXECUTOR");
    
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    
    @Autowired
    private SysOperLogService sysOperLogService;
    
    // 配置织入点
    @Pointcut("@annotation(com.xiliulou.electricity.annotation.Log)")
    public void logPointCut() {
    }
    
    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }
    
    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }
    
    
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            
            TokenUser currentUser = SecurityUtils.getUserInfo();
            
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(SysOperLog.STATUS_SUCCESS);
            
            // 请求的地址
            String ip = WebUtils.getIP(request);
            operLog.setOperIp(ip);
            
            //获取请求id
            String requestId = String.valueOf(request.getAttribute(RequestFilter.REQUEST_ID));
            operLog.setRequestId(requestId);
            
            operLog.setTenantId(TenantContextHolder.getTenantId().longValue());
            operLog.setOperTime(System.currentTimeMillis());
            
            // 设置操作人
            if (currentUser != null) {
                operLog.setOperatorUid(currentUser.getUid());
            }
            
            if (e != null) {
                operLog.setStatus(SysOperLog.STATUS_FAIL);
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 200));
            }
            
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);

            //Logstash日志
            log.info("requestId={} ip={} uid={} method={} uri={} result={}", requestId, ip, currentUser.getUid(),
                    ServletUtils.getRequest().getMethod(), ServletUtils.getRequest().getRequestURI(),
                    JsonUtil.toJson(jsonResult));
    
            if (e == null) {
                throw new RuntimeException("异常啦");
            }
            
            // 保存到数据库
            executorService.execute(() -> saveSysOperLog(operLog));
        } catch (Exception exp) {
            log.error("操作日志前置通知异常:{}", exp.getMessage());
        }
    }
    
    /**
     * 获取注解中对方法的描述信息
     *
     * @param log     日志
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult)
            throws Exception {
        // 设置标题
        operLog.setTitle(log.title());
        
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && Objects.nonNull(jsonResult)) {
        
        }
    }
    
    /**
     * 操作日志保存到数据库中
     *
     * @param operLog 操作日志
     * @throws Exception 异常
     */
    private void saveSysOperLog(SysOperLog operLog) {
        sysOperLogService.insert(operLog);
    }
    
}
