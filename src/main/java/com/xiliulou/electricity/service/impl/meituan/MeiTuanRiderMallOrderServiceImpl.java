package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallOrderMapper;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.vo.meituan.OrderVO;
import com.xiliulou.thirdmall.config.meituan.MeiTuanRiderMallHostConfig;
import com.xiliulou.thirdmall.entity.meituan.MeiTuanRiderMallApiConfig;
import com.xiliulou.thirdmall.entity.meituan.request.virtualtrade.SyncOrderReq;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrderRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrdersDataRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.SkuRsp;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.service.meituan.virtualtrade.VirtualTradeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public MeiTuanRiderMallOrder queryByOrderId(String orderId, String phone, Long uid) {
        return meiTuanRiderMallOrderMapper.selectByOrderId(orderId, phone, uid);
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallOrder> listOrdersByUidAndPhone(OrderQuery query) {
        return meiTuanRiderMallOrderMapper.selectByUidAndPhone(query);
    }
    
    @Override
    public List<MeiTuanRiderMallOrder> listAllUnSyncedOrder(Integer tenantId) {
        List<MeiTuanRiderMallOrder> list = new ArrayList<>();
        int offset = 0;
        int size = 200;
        
        while (true) {
            List<MeiTuanRiderMallOrder> orders = this.listUnSyncedOrder(tenantId, offset, size);
            if (CollectionUtils.isEmpty(orders)) {
                break;
            }
            
            list.addAll(orders);
            offset += size;
        }
        return list;
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallOrder> listUnSyncedOrder(Integer tenantId, Integer offset, Integer size) {
        return meiTuanRiderMallOrderMapper.selectListUnSyncedOrder(tenantId, offset, size);
    }
    
    /**
     * 1.创建套餐成功 2.通知美团发货 3.发货失败，回滚步骤1的数据
     */
    @Override
    public R createBatteryMemberCardOrder(OrderQuery query) {
    
    }
    
    /**
     * 发货失败执行回滚
     */
    private void handleRollback() {
    
    }
    
    @Override
    public List<OrderVO> listOrders(OrderQuery query) {
        Long uid = query.getUid();
        Integer tenantId = query.getTenantId();
        
        // 判断是否需要从美团拉取订单
        Boolean needFetchOrders = this.ifNeedFetchOrders(tenantId, query.getOrderId(), uid, query.getSecond());
        if (needFetchOrders) {
            MeiTuanRiderMallConfig config = meiTuanRiderMallConfigService.queryByTenantIdFromCache(tenantId);
            if (Objects.isNull(config)) {
                log.warn("ListOrders warn! MeiTuanRiderMallConfig is null, uid={}", uid);
                return Collections.emptyList();
            }
            
            MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                    .host(meiTuanRiderMallHostConfig.getHost()).build();
            // 分页拉取最近5分钟的订单
            long endTime = System.currentTimeMillis() / 1000;
            Long startTime = endTime - 5 * 60;
            
            // 从美团拉取订单
            List<OrderRsp> orderRspList = this.fetchOrders(apiConfig, startTime, endTime);
            if (CollectionUtils.isNotEmpty(orderRspList)) {
                // 持久化
                this.handleBatchInsert(orderRspList, config.getTenantId());
            }
        }
        
        List<MeiTuanRiderMallOrder> riderMallOrders = this.listOrdersByUidAndPhone(query);
        if (CollectionUtils.isEmpty(riderMallOrders)) {
            return Collections.emptyList();
        }
        
        return riderMallOrders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    private Boolean ifNeedFetchOrders(Integer tenantId, String orderId, Long uid, Integer second) {
        // 数据库中如果没有该订单，则需要拉取
        MeiTuanRiderMallOrder riderMallOrder = this.queryByOrderId(orderId, null, uid);
        if (Objects.isNull(riderMallOrder)) {
            return Boolean.TRUE;
        }
        
        // 获取定时任务上次执行时间
        String lastTaskTime = redisService.get(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_ORDER_FETCH_TIME + tenantId);
        // 如果查不到场次执行时间，则需要拉取
        if (StringUtils.isBlank(lastTaskTime)) {
            return Boolean.TRUE;
        }
        
        // 判断当前时间与定时任务上次执行时间间隔是否大于指定秒数，默认30秒，如果大于等于则需要拉取
        second = Objects.isNull(second) ? 30 : second;
        return (System.currentTimeMillis() - Long.parseLong(lastTaskTime)) / 1000 >= second;
    }
    
    /**
     * 定时任务：从美团拉取订单
     */
    @Override
    public void handelFetchOrderTask(String sessionId, Long startTime, Integer recentDay) {
        List<MeiTuanRiderMallConfig> configs = meiTuanRiderMallConfigService.listAll();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        
        // 遍历租户
        configs.forEach(config -> handleFetchOrdersByTenant(config, recentDay));
        
        Long costTime = System.currentTimeMillis() - startTime;
        log.info("MeiTuanRiderMallFetchOrderTask end! sessionId={}, costTime={}", sessionId, costTime);
    }
    
    @Override
    public void handelSyncOrderStatusTask(String sessionId, long startTime) {
        List<MeiTuanRiderMallConfig> configs = meiTuanRiderMallConfigService.listAll();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        
        // 遍历租户
        configs.forEach(this::handleSyncOrderStatusByTenant);
        
        Long costTime = System.currentTimeMillis() - startTime;
        log.info("MeiTuanRiderMallSyncOrderStatusTask end! sessionId={}, costTime={}", sessionId, costTime);
    }
    
    private void handleSyncOrderStatusByTenant(MeiTuanRiderMallConfig config) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        List<MeiTuanRiderMallOrder> riderMallOrders = this.listAllUnSyncedOrder(config.getTenantId());
        if (CollectionUtils.isEmpty(riderMallOrders)) {
            return;
        }
        
        List<MeiTuanRiderMallOrder> updateList = new ArrayList<>();
        List<List<MeiTuanRiderMallOrder>> partition = ListUtils.partition(riderMallOrders, 20);
        partition.forEach(orders -> {
            List<SyncOrderReq> syncOrderReqOrderList = orders.stream()
                    .map(order -> SyncOrderReq.builder().orderId(order.getMeiTuanOrderId()).orderHandleResonStatus(order.getOrderSyncStatus()).build())
                    .collect(Collectors.toList());
            
            // 调用美团接口
            Boolean result = virtualTradeService.syncOrderResult(apiConfig, syncOrderReqOrderList, false);
            if (result) {
                updateList.addAll(orders);
            }
        });
        
        if (CollectionUtils.isNotEmpty(updateList)) {
            // 对”已处理“的订单，修改状态为”已对账“；”未处理“的不做更改
            meiTuanRiderMallOrderMapper.batchUpdateSyncOrderStatus(updateList);
        }
    }
    
    private void handleFetchOrdersByTenant(MeiTuanRiderMallConfig config, Integer recentDay) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        // 分页拉取最近N天的订单
        long endTime = System.currentTimeMillis() / 1000;
        Long startTime = endTime - recentDay * 24 * 60 * 60;
        
        // 从美团拉取订单
        List<OrderRsp> orderRspList = this.fetchOrders(apiConfig, startTime, endTime);
        if (CollectionUtils.isEmpty(orderRspList)) {
            return;
        }
        
        // 持久化
        Integer tenantId = config.getTenantId();
        this.handleBatchInsert(orderRspList, config.getTenantId());
        
        // redis记录租户本次定时任务执行时间
        redisService.saveWithString(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_ORDER_FETCH_TIME + tenantId, System.currentTimeMillis());
    }
    
    private List<OrderRsp> fetchOrders(MeiTuanRiderMallApiConfig apiConfig, Long startTime, Long endTime) {
        Long cursor = null;
        Integer pageSize = 100;
        List<OrderRsp> list = new ArrayList<>();
        
        while (true) {
            OrdersDataRsp ordersDataRsp = virtualTradeService.listAllOrders(apiConfig, cursor, pageSize, startTime, endTime, false);
            if (Objects.isNull(ordersDataRsp)) {
                break;
            }
            
            if (ordersDataRsp.getHasNext()) {
                cursor += ordersDataRsp.getCursor();
                ordersDataRsp = virtualTradeService.listAllOrders(apiConfig, cursor, pageSize, startTime, endTime, false);
                
                list.addAll(ordersDataRsp.getList());
            }
        }
        
        return list;
    }
    
    private void handleBatchInsert(List<OrderRsp> list, Integer tenantId) {
        List<MeiTuanRiderMallOrder> insertList = new ArrayList<>();
        
        List<List<OrderRsp>> partition = ListUtils.partition(list, 200);
        partition.forEach(orders -> orders.forEach(order -> {
            String orderId = order.getOrderId();
            SkuRsp skuRsp = order.getSkuList().get(0);
            String phone = skuRsp.getAccount();
            
            MeiTuanRiderMallOrder existOrder = this.queryByOrderId(orderId, phone, null);
            if (Objects.nonNull(existOrder)) {
                return;
            }
            
            UserInfo userInfo = userInfoService.queryUserInfoByPhone(phone, tenantId);
            
            MeiTuanRiderMallOrder meiTuanRiderMallOrder = MeiTuanRiderMallOrder.builder().meiTuanOrderId(orderId).meiTuanOrderTime(order.getOrderTime() * 1000)
                    .meiTuanOrderStatus(order.getOrderStatus()).meiTuanActuallyPayPrice(new BigDecimal(order.getActuallyPayPrice()))
                    .meiTuanVirtualRechargeType(skuRsp.getVirtualRechargeType()).meiTuanAccount(phone).orderId(StringUtils.EMPTY)
                    .orderSyncStatus(VirtualTradeStatusEnum.ORDER_HANDLE_REASON_STATUS_UNHANDLED.getCode()).orderUseStatus(VirtualTradeStatusEnum.ORDER_USE_STATUS_UNUSED.getCode())
                    .packageId(skuRsp.getSkuId()).packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()).uid(Optional.ofNullable(userInfo.getUid()).orElse(0L))
                    .tenantId(tenantId).delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            
            insertList.add(meiTuanRiderMallOrder);
        }));
        
        // 批量入库
        if (CollectionUtils.isNotEmpty(insertList)) {
            meiTuanRiderMallOrderMapper.batchInsert(insertList);
        }
    }
}
