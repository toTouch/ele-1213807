package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderBizService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;


/**
 * description:
 *
 * @author maxiaodong
 * @date 2024/8/26 16:33
 */
@Slf4j
@Component
@JobHandler(value = "profitSharingOrderUnfreezeTask")
public class ProfitSharingOrderUnfreezeTask extends IJobHandler {
    
    @Resource
    private ProfitSharingOrderBizService profitSharingOrderBizService;
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        
        try {
            profitSharingOrderBizService.doUnfreezeTask();
            
        } catch (Exception e) {
            log.error("profit sharing order unfreeze task execute error", e);
        } finally {
            TtlTraceIdSupport.clear();
        }
        
        return ReturnT.SUCCESS;
    }
    
}
