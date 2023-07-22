package com.xiliulou.electricity.task.car;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
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
 * 租车套餐购买订单过期 Job
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@JobHandler("carRentalPackageMemberTermExpireTask")
public class CarRentalPackageMemberTermExpireTask extends IJobHandler {

    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;

    /**
     * 会员期限订单过期任务
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        MDC.put(CommonConstant.TRACE_ID, UUID.randomUUID().toString().replaceAll("-", ""));
        log.info("CarRentalPackageOrderExpireTask begin.");

        Integer offset = 0;
        Integer size = 500;
        if (!StringUtils.isBlank(param)) {
            JSONObject jsonObjectParam = JSON.parseObject(param);
            offset = ObjectUtils.isEmpty(jsonObjectParam.getInteger("offset")) ? offset : jsonObjectParam.getInteger("offset");
            size = ObjectUtils.isEmpty(jsonObjectParam.getInteger("size")) ? size : jsonObjectParam.getInteger("size");
        }

        try {
            carRentalPackageMemberTermBizService.expirePackageOrder(offset, size);
        } catch (Exception e) {
            log.info("CarRentalPackageOrderExpireTask error. ", e);
        } finally {
            MDC.clear();
        }

        log.info("CarRentalPackageOrderExpireTask end.");

        return IJobHandler.SUCCESS;
    }
}
