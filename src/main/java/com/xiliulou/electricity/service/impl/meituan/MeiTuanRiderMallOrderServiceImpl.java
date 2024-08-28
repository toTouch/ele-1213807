package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.thirdmall.config.meituan.MeiTuanRiderMallHostConfig;
import com.xiliulou.thirdmall.entity.meituan.MeiTuanRiderMallApiConfig;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrderRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrdersDataRsp;
import com.xiliulou.thirdmall.service.meituan.virtualtrade.VirtualTradeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:06:11
 */
@Slf4j
@Service
public class MeiTuanRiderMallOrderServiceImpl implements MeiTuanRiderMallOrderService {
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Resource
    private VirtualTradeService virtualTradeService;
    
    @Resource
    private MeiTuanRiderMallHostConfig meiTuanRiderMallHostConfig;
    
    /**
     * 定时任务：从美团拉取订单
     */
    @Override
    public void handelFetchOrders(String sessionId) {
        List<MeiTuanRiderMallConfig> configs = meiTuanRiderMallConfigService.listAll();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        
        configs.forEach(this::handleFetchOrdersByTenant);
    }
    
    private void handleFetchOrdersByTenant(MeiTuanRiderMallConfig config) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        Long cursor = null;
        Integer pageSize = 100;
        Long endTime = System.currentTimeMillis() / 1000;
        Long beginTime = endTime - 24 * 60 * 60;
        List<OrderRsp> list = new ArrayList<>();
        
        while (true) {
            OrdersDataRsp ordersDataRsp = virtualTradeService.listAllOrders(apiConfig, cursor, pageSize, beginTime, endTime, false);
            if (Objects.isNull(ordersDataRsp)) {
                break;
            }
            
            if (ordersDataRsp.getHasNext()) {
                cursor += ordersDataRsp.getCursor();
                ordersDataRsp = virtualTradeService.listAllOrders(apiConfig, cursor, pageSize, beginTime, endTime, false);
                
                list.addAll(ordersDataRsp.getList());
            }
        }
    }
}
