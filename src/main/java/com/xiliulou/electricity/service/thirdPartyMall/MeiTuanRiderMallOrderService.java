package com.xiliulou.electricity.service.thirdPartyMall;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.meituan.BatteryDepositBO;
import com.xiliulou.electricity.bo.meituan.MtBatteryPackageBO;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.thirdPartyMall.OrderQuery;
import com.xiliulou.electricity.vo.thirdPartyMall.LimitTradeVO;
import com.xiliulou.electricity.vo.thirdPartyMall.MtBatteryDepositVO;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:05:30
 */
public interface MeiTuanRiderMallOrderService {
    
    MeiTuanRiderMallOrder queryByMtOrderId(String orderId, String phone, Long uid, Integer tenantId);
    
    MeiTuanRiderMallOrder queryByOrderId(String orderId, Long uid, Integer tenantId);
    
    List<MeiTuanRiderMallOrder> listOrders(OrderQuery query);
    
    List<MeiTuanRiderMallOrder> listOrdersByPhone(OrderQuery query);
    
    Triple<Boolean, String, Object> createBatteryMemberCardOrder(OrderQuery query);
    
    Integer updateStatusByOrderId(MeiTuanRiderMallOrder meiTuanRiderMallOrder);
    
    Boolean isMtOrder(Long uid);
    
    void updatePhone(String oldPhone, String newPhone, Integer tenantId);
    
    /**
     * 美团骑手商城限制提单校验
     */
    LimitTradeVO meiTuanLimitTradeCheck(String providerSkuId, String phone);
    
    R queryBatteryDeposit(Long uid);
    
    BatteryDepositBO queryMaxPackageDeposit(String phone, Integer tenantId);
    
    MtBatteryPackageBO queryBatteryPackageInfo(OrderQuery query);
}
