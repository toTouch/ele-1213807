package com.xiliulou.electricity.handler.eleapi.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.EleApiConstant;
import com.xiliulou.electricity.entity.ApiExchangeOrder;
import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import com.xiliulou.electricity.entity.ApiRentOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.service.ApiExchangeOrderService;
import com.xiliulou.electricity.service.ApiOrderOperHistoryService;
import com.xiliulou.electricity.service.ApiRentOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.api.ApiExchangeOrderVo;
import com.xiliulou.electricity.vo.api.ApiRentOrderVo;
import com.xiliulou.electricity.web.query.ApiOrderQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author : eclair
 * @date : 2021/11/9 9:31 上午
 */
@Service(value = EleApiConstant.RENT_ORDER_QUERY)
@Slf4j
public class EleRentOrderQueryHandler implements EleApiHandler {
    @Autowired
    ApiRentOrderService apiRentOrderService;

    @Autowired
    ApiOrderOperHistoryService apiOrderOperHistoryService;
    @Autowired
    ElectricityCabinetService cabinetService;


    @Override
    public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {

        ApiOrderQuery apiOrderQuery = JsonUtil.fromJson(apiRequestQuery.getData(), ApiOrderQuery.class);
        if (Objects.isNull(apiOrderQuery) || StrUtil.isEmpty(apiOrderQuery.getOrderId())) {
            log.error("ELE QUERY ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "orderId不存在");
        }

        ApiRentOrder apiRentOrder = apiRentOrderService.queryByOrderId(apiOrderQuery.getOrderId(), TenantContextHolder.getTenantId());
        if (Objects.isNull(apiRentOrder)) {
            return Triple.of(false, "API.10004", "查无此单");
        }

        ApiRentOrderVo apiRentOrderVo = new ApiRentOrderVo();
        BeanUtils.copyProperties(apiRentOrder, apiRentOrderVo);

        ElectricityCabinet electricityCabinet = Optional.ofNullable(cabinetService.queryByIdFromCache(apiRentOrder.getEid())).orElse(new ElectricityCabinet());
        apiRentOrderVo.setDeviceName(electricityCabinet.getDeviceName());
        apiRentOrderVo.setProductKey(electricityCabinet.getProductKey());
        apiRentOrderVo.setCabinetName(electricityCabinet.getName());

        if (Objects.isNull(apiOrderQuery.getNeedOperateRecord()) || !apiOrderQuery.getNeedOperateRecord()) {
            return Triple.of(true, null, apiRentOrderVo);
        }

        List<ApiOrderOperHistory> history = apiOrderOperHistoryService.queryByOrderId(apiRentOrderVo.getOrderId(), ApiOrderOperHistory.ORDER_TYPE_EXCHANGE);
        apiRentOrderVo.setOperateRecords(history);

        return Triple.of(true, null, apiRentOrderVo);
    }
}
