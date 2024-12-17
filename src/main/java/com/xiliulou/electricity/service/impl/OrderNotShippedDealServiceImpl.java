package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.wp.beanposstprocessor.WechatAccessTokenSevice;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.OrderNotShippedDealService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.pay.shipping.entity.ShippingOrder;
import com.xiliulou.pay.shipping.entity.ShippingQueryOrderRequest;
import com.xiliulou.pay.shipping.entity.ShippingQueryOrderResult;
import com.xiliulou.pay.shipping.exception.ShippingException;
import com.xiliulou.pay.shipping.service.ShippingUploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/11/21 9:43
 * @desc
 */
@Service
@Slf4j
public class OrderNotShippedDealServiceImpl implements OrderNotShippedDealService {
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
    @Resource
    private WechatAccessTokenSevice wechatAccessTokenSevice;
    
    @Resource
    private ShippingUploadService shippingUploadService;
    
    @Resource
    private ElectricityTradeOrderService electricityTradeOrderService;
    
    
    @Override
    public void handleNotShippedOrder(Integer tenantId) {
        Integer startId = 0;
        Integer size = 200;
        boolean tenantFlag = true;
        List<Integer> tenantIdList = new ArrayList<>();
        tenantIdList.add(tenantId);
        
        // 昨天开始
        long startTime = DateUtils.getTimeAgoStartTime(DateFormatConstant.YESTERDAY);
        // 一小时之前的时间戳
        long endTime = System.currentTimeMillis() - TimeConstant.HOURS_MILLISECOND;
        
        while (tenantFlag) {
            if (Objects.nonNull(tenantId)) {
                tenantFlag = false;
            } else {
                tenantIdList = tenantService.queryIdListByStartId(startId, size);
            }
            
            if (ObjectUtils.isEmpty(tenantIdList)) {
                break;
            }
            
            startId = tenantIdList.get(tenantIdList.size() - 1);
    
            List<Integer> existByTenantIdList = electricityTradeOrderService.existByTenantIdList(tenantIdList, startTime, endTime);
            if (Objects.isNull(tenantId) && ObjectUtils.isEmpty(existByTenantIdList)) {
                return;
            }
            
            tenantIdList.stream().forEach(id -> {
                ElectricityPayParams electricityPayParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(id, MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
                if (Objects.isNull(electricityPayParams)) {
                    return;
                }
                
                // 检测租户昨天是否存在套餐购买订单
                if (Objects.isNull(tenantId) && !existByTenantIdList.contains(id)) {
                    return;
                }
                
                String merchantMinProAppId = electricityPayParams.getMerchantMinProAppId();
                String merchantMinProAppSecert = electricityPayParams.getMerchantMinProAppSecert();
                if (ObjectUtils.isEmpty(merchantMinProAppId) || ObjectUtils.isEmpty(merchantMinProAppSecert)) {
                    return;
                }
                
                String lastIndex = null;
                Integer orderState = ShippingQueryOrderRequest.NOT_SHIPPING;
                Integer pageSize = 200;
                boolean flag = true;
                
                // 查询未发货的订单
                while (flag) {
                    try {
                        Pair<Boolean, Object> accessToken = wechatAccessTokenSevice.getAccessToken(merchantMinProAppId, merchantMinProAppSecert);
                        if (!accessToken.getLeft() || ObjectUtils.isEmpty(accessToken.getRight())) {
                            return;
                        }
    
                        String token = (String) accessToken.getRight();
    
                        if (StringUtils.isBlank(token)) {
                            return;
                        }
                        
                        ShippingQueryOrderResult shippingQueryOrderResult = shippingUploadService.listOrder(token, orderState, lastIndex, pageSize);
                        if (Objects.isNull(shippingQueryOrderResult) || !shippingQueryOrderResult.isSuccess() || ObjectUtils.isEmpty(shippingQueryOrderResult.getOrder_list())) {
                            break;
                        }
                        
                        // 没有数据则跳出循环
                        if (!shippingQueryOrderResult.isHas_more()) {
                            flag = false;
                        }
                        
                        lastIndex = shippingQueryOrderResult.getLast_index();
                        List<ShippingOrder> list = shippingQueryOrderResult.getOrder_list();
                        // 判断订单是否存在
                        List<String> transactionIdList = list.stream().map(ShippingOrder::getTransaction_id).collect(Collectors.toList());
                        List<ElectricityTradeOrder> electricityTradeOrderList = electricityTradeOrderService.listByChannelOrderNoList(transactionIdList, ElectricityTradeOrder.STATUS_SUCCESS, endTime);
                        // 不存在则进行下一步循环操作
                        if (ObjectUtils.isEmpty(electricityTradeOrderList)) {
                            continue;
                        }
                        
                        Set<String> existsChannelOrderNoSet = electricityTradeOrderList.stream().map(ElectricityTradeOrder::getChannelOrderNo).collect(Collectors.toSet());
                        list.stream().forEach(shippingOrder -> {
                            if (!existsChannelOrderNoSet.contains(shippingOrder.getTransaction_id())) {
                                return;
                            }
                            
                            try {
                                // 调用发货接口
                                shippingUploadService.shippingUploadInfoByToken(shippingOrder.getOpenid(), shippingOrder.getTransaction_id(), token);
                                TimeUnit.MILLISECONDS.sleep(200);
                                log.info("SHIPPING INFO SUCCESS! shipping upload success,tenantId={},orderNo={}", id, shippingOrder.getTransaction_id());
                            } catch (InterruptedException ex) {
                                log.error("SHIPPING INFO ERROR! shipping upload fail,orderNo={}", shippingOrder.getTransaction_id(), ex);
                                Thread.currentThread().interrupt();
                            } catch (ShippingException e) {
                                log.error("SHIPPING INFO ERROR! shipping upload fail,tenantId={}, orderNo={}", id,shippingOrder.getTransaction_id(), e);
                            }
                        });
                        
                    } catch (ShippingException e) {
                        log.error("SHIPPING INFO ERROR! shipping upload fail,tenantId={}", id, e);
                        break;
                    }
                }
            });
        }
    }
}
