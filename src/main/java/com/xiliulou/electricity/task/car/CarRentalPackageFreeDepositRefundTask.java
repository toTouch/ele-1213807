package com.xiliulou.electricity.task.car;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 租赁套餐，免押退押解冻授权
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@JobHandler("carRentalPackageFreeDepositRefundTask")
public class CarRentalPackageFreeDepositRefundTask extends IJobHandler {

    @Resource
    private CarRenalPackageDepositBizService carRenalPackageDepositBizService;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        MDC.put(CommonConstant.TRACE_ID, UUID.randomUUID().toString().replaceAll("-", ""));
        log.info("carRentalPackageFreeDepositRefundTask begin.");

        Integer offset = 0;
        Integer size = 500;
        if (!StringUtils.isBlank(param)) {
            JSONObject jsonObjectParam = JSON.parseObject(param);
            offset = ObjectUtils.isEmpty(jsonObjectParam.getInteger("offset")) ? offset : jsonObjectParam.getInteger("offset");
            size = ObjectUtils.isEmpty(jsonObjectParam.getInteger("size")) ? size : jsonObjectParam.getInteger("size");
        }

        try {
            carRenalPackageDepositBizService.freeDepositRefundHandler(offset, size);
        } catch (Exception e) {
            log.error("carRentalPackageFreeDepositRefundTask error. ", e);
        } finally {
            MDC.clear();
        }

        log.info("carRentalPackageFreeDepositRefundTask end.");

        return IJobHandler.SUCCESS;
    }
}
