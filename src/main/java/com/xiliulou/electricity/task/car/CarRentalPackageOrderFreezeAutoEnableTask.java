package com.xiliulou.electricity.task.car;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 车辆套餐冻结订单启用 Job
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@JobHandler
public class CarRentalPackageOrderFreezeAutoEnableTask extends IJobHandler {

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    /**
     * 车辆套餐冻结订单启用
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        log.info("CarRentalPackageOrderFreezeAutoEnableTask begin.");
        Integer offset = 0;
        Integer size = 500;
        if (!StringUtils.isBlank(param)) {
            JSONObject jsonObjectParam = JSON.parseObject(param);
            offset = ObjectUtils.isEmpty(jsonObjectParam.getInteger("offset")) ? offset : jsonObjectParam.getInteger("offset");
            size = ObjectUtils.isEmpty(jsonObjectParam.getInteger("size")) ? size : jsonObjectParam.getInteger("size");
        }
        try {
            carRentalPackageOrderBizService.enableFreezeRentOrderAuto(offset, size);
        } catch (Exception e) {
            log.info("CarRentalPackageOrderFreezeAutoEnableTask error. ", e);
        }
        log.info("CarRentalPackageOrderFreezeAutoEnableTask end.");

        return IJobHandler.SUCCESS;
    }
}
