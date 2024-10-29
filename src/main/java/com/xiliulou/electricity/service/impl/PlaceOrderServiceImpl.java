package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.handler.placeorder.chain.PlaceOrderChainManager;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.electricity.service.PlaceOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/29 13:41
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceOrderServiceImpl implements PlaceOrderService {
    
    private final PlaceOrderChainManager placeOrderChainManager;
    
    private final RedisService redisService;
    
    @Override
    public R<Object> placeOrder(PlaceOrderQuery query, HttpServletRequest request) {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_PLACE_ORDER_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            // 封装上下文，获取执行链处理结果
            PlaceOrderContext context = PlaceOrderContext.builder().placeOrderQuery(query).request(request).tokenUser(tokenUser).tenantId(TenantContextHolder.getTenantId())
                    .build();
            R<Object> result = placeOrderChainManager.chooseNodeAndProcess(context);
            if (!result.isSuccess()) {
                return result;
            }
            
            // 保存订单并调起支付
            return placeOrderChainManager.saveOrdersAndPay(context);
        } catch (Exception e) {
            log.error("PLACE ORDER ERROR! wechat v3 order  error! uid={}", tokenUser.getUid(), e);
        }
        return R.fail("PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
    }
}
