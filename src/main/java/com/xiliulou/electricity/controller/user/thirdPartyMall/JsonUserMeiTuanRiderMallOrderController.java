package com.xiliulou.electricity.controller.user.thirdPartyMall;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.thirdPartyMall.OrderQuery;
import com.xiliulou.electricity.request.thirdPartyMall.CreateMemberCardOrderRequest;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.thirdPartyMall.OrderVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/29 13:49:50
 */

@Slf4j
@RestController
public class JsonUserMeiTuanRiderMallOrderController extends BaseController {
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    /**
     * 点击“美团商城”后，获取用户美团订单信息
     */
    @GetMapping("/user/meiTuanRiderMall/listOrders")
    public R listOrders(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "orderId", required = false) String orderId) {
        if (size < 0 || size > 500) {
            size = 500L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        
        OrderQuery query = OrderQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).orderId(orderId).build();
        List<MeiTuanRiderMallOrder> riderMallOrders = meiTuanRiderMallOrderService.listOrders(query);
        if (CollectionUtils.isEmpty(riderMallOrders)) {
            return R.ok(Collections.emptyList());
        }
        
        List<OrderVO> voList = riderMallOrders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            
            return vo;
        }).collect(Collectors.toList());
        
        return R.ok(voList);
    }
    
    /**
     * 根据美团订单，创建换电套餐订单
     */
    @PostMapping("/user/meiTuanRiderMall/createBatteryMemberCardOrder")
    public R createBatteryMemberCardOrder(@RequestBody @Valid CreateMemberCardOrderRequest orderRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        
        OrderQuery query = OrderQuery.builder().tenantId(TenantContextHolder.getTenantId()).uid(user.getUid()).orderId(orderRequest.getOrderId()).build();
        return returnTripleResult(meiTuanRiderMallOrderService.createBatteryMemberCardOrder(query));
    }
}
