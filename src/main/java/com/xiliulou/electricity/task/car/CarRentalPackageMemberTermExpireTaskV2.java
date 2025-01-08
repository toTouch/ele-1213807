package com.xiliulou.electricity.task.car;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.reqparam.opt.carpackage.ExpirePackageOrderReq;
import com.xiliulou.electricity.service.car.biz.CarRentalMemberTermExpireBizService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * name: 租车套餐过期任务 v2版本
 *
 * @author caobotao.cbt
 * @date 2024/11/25 15:56
 */
@Slf4j
@Component
@JobHandler("carRentalPackageMemberTermExpireV2Task")
public class CarRentalPackageMemberTermExpireTaskV2 extends IJobHandler {
    
    @Resource
    private CarRentalMemberTermExpireBizService carRentalMemberTermExpireBizService;
    
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        log.info(" start param:{} ", param);
        
        ExpirePackageOrderReq expirePackageOrderReq = new ExpirePackageOrderReq();
        
        if (StringUtils.isNotBlank(param)) {
            expirePackageOrderReq = JsonUtil.fromJson(param, ExpirePackageOrderReq.class);
        }
        
        try {
            carRentalMemberTermExpireBizService.expirePackageOrder(expirePackageOrderReq);
        } catch (Exception e) {
            log.error("ERROR! Exception:", e);
        } finally {
            TtlTraceIdSupport.clear();
        }
        
        log.info("CarRentalPackageOrderExpireTask end.");
        
        return IJobHandler.SUCCESS;
    }
}
