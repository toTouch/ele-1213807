package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 退款订单表(TEleBatteryServiceFeeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-04-20 10:21:24
 */
public interface EleBatteryServiceFeeOrderService {

    EleBatteryServiceFeeOrder queryEleBatteryServiceFeeOrderByOrderId(String orderNo);

    void update(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder);

    R queryList(Long offset, Long size, Long startTime, Long endTime);
}
