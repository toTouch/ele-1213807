package com.xiliulou.electricity.task.cabinet;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.service.cabinet.ElectricityCabinetSendNormalService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@JobHandler(value = "electricityCabinetSendNormalTask")
@Slf4j
public class ElectricityCabinetSendNormalTask extends IJobHandler {
    @Resource
    private ElectricityCabinetSendNormalService electricityCabinetSendNormalService;


    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();

        try {
            ElectricityCabinetSendNormalTask.SendNormalTaskParam taskParam = new ElectricityCabinetSendNormalTask.SendNormalTaskParam();
            if (StringUtils.isNotBlank(param)) {
                taskParam = JsonUtil.fromJson(param, ElectricityCabinetSendNormalTask.SendNormalTaskParam.class);
            }

            electricityCabinetSendNormalService.sendNormalCommand(taskParam);
        } catch (Exception e) {
            log.error("electricity cabinet send normal task", e);
        } finally {
            TtlTraceIdSupport.clear();
        }

        return IJobHandler.SUCCESS;
    }

    @Data
    public static class SendNormalTaskParam {
        private List<Integer> tenantIds;

        private List<Integer> cabinetIds;
    }
}
