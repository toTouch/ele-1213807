package com.xiliulou.electricity.service.profitsharing;


import com.xiliulou.electricity.request.profitsharing.ProfitSharingOrderDetailPageRequest;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingOrderDetailVO;

import java.util.List;

/**
 * 分账订单明细表(TRofitSharingOrderDetail)表服务接口
 *
 * @author maxiaodong
 * @since 2024-08-22 17:00:35
 */
public interface ProfitSharingOrderDetailService {
    Integer countTotal(ProfitSharingOrderDetailPageRequest merchantPageRequest);
    
    List<ProfitSharingOrderDetailVO> listByPage(ProfitSharingOrderDetailPageRequest profitSharingOrderPageRequest);
}
