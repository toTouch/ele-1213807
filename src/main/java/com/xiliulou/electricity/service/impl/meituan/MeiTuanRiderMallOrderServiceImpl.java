package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallOrderMapper;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.thirdmall.config.meituan.MeiTuanRiderMallHostConfig;
import com.xiliulou.thirdmall.entity.meituan.MeiTuanRiderMallApiConfig;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrderRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrdersDataRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.SkuRsp;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.service.meituan.virtualtrade.VirtualTradeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:06:11
 */
@Slf4j
@Service
public class MeiTuanRiderMallOrderServiceImpl implements MeiTuanRiderMallOrderService {
    
    @Resource
    private MeiTuanRiderMallOrderMapper meiTuanRiderMallOrderMapper;
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Resource
    private VirtualTradeService virtualTradeService;
    
    @Resource
    private MeiTuanRiderMallHostConfig meiTuanRiderMallHostConfig;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Slave
    @Override
    public MeiTuanRiderMallOrder queryByOrderIdAndPhone(String orderId, String phone) {
        return meiTuanRiderMallOrderMapper.selectByOrderIdAndPhone(orderId, phone);
    }
    
    /**
     * 定时任务：从美团拉取订单
     */
    @Override
    public void handelFetchOrders(String sessionId, Long startTime, Integer recentDay) {
        List<MeiTuanRiderMallConfig> configs = meiTuanRiderMallConfigService.listAll();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        
        // 遍历租户
        configs.forEach(config -> handleFetchOrdersByTenant(config, recentDay));
        
        Long costTime = System.currentTimeMillis() - startTime;
        log.info("MeiTuanRiderMallFetchOrderTask end! sessionId={}, costTime={}", sessionId, costTime);
    }
    
    private void handleFetchOrdersByTenant(MeiTuanRiderMallConfig config, Integer recentDay) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).tenantId(config.getTenantId()).build();
        
        // 分页拉取最近N天的订单
        List<OrderRsp> orderRspList = this.fetchOrdersByRecentDay(apiConfig, recentDay);
        if (CollectionUtils.isEmpty(orderRspList)) {
            return;
        }
        
        List<MeiTuanRiderMallOrder> insertList = new ArrayList<>();
        List<List<OrderRsp>> partition = ListUtils.partition(orderRspList, 100);
        partition.forEach(orders -> orders.forEach(order -> {
            String orderId = order.getOrderId();
            SkuRsp skuRsp = order.getSkuList().get(0);
            String phone = skuRsp.getAccount();
            
            MeiTuanRiderMallOrder existOrder = this.queryByOrderIdAndPhone(orderId, phone);
            if (Objects.nonNull(existOrder)) {
                return;
            }
            
            Integer tenantId = apiConfig.getTenantId();
            UserInfo userInfo = userInfoService.queryUserInfoByPhone(phone, tenantId);
            
            MeiTuanRiderMallOrder meiTuanRiderMallOrder = MeiTuanRiderMallOrder.builder().meiTuanOrderId(orderId).meiTuanOrderTime(order.getOrderTime())
                    .meiTuanOrderStatus(order.getOrderStatus()).meiTuanActuallyPayPrice(new BigDecimal(order.getActuallyPayPrice()))
                    .meiTuanVirtualRechargeType(skuRsp.getVirtualRechargeType()).meiTuanAccount(phone).orderId(StringUtils.EMPTY)
                    .orderHandleReasonStatus(VirtualTradeStatusEnum.ORDER_HANDLE_REASON_STATUS_UNHANDLED.getCode())
                    .orderUseStatus(VirtualTradeStatusEnum.ORDER_USE_STATUS_UNUSED.getCode()).packageId(skuRsp.getSkuId())
                    .packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()).uid(Optional.ofNullable(userInfo.getUid()).orElse(0L)).tenantId(tenantId)
                    .delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            
            insertList.add(meiTuanRiderMallOrder);
        }));
        
        // 批量入库
        if (CollectionUtils.isNotEmpty(insertList)) {
            meiTuanRiderMallOrderMapper.batchInsert(insertList);
        }
    }
    
    private List<OrderRsp> fetchOrdersByRecentDay(MeiTuanRiderMallApiConfig apiConfig, Integer recentDay) {
        Long cursor = null;
        Integer pageSize = 100;
        Long endTime = System.currentTimeMillis() / 1000;
        Long beginTime = endTime - recentDay * 24 * 60 * 60;
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
        
        return list;
    }
}
