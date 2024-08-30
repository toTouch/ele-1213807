package com.xiliulou.electricity.service.profitsharing;


import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
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
    
    int batchInsert(List<ProfitSharingOrderDetail> profitSharingOrderDetailList);
    
    int insert(ProfitSharingOrderDetail profitSharingOrderDetail);
    
    boolean existsNotUnfreezeByThirdOrderNo(String thirdOrderNo);
    
    boolean existsNotCompleteByThirdOrderNo(String thirdOrderNo);
    
    boolean existsFailByThirdOrderNo(String thirdOrderNo);
    
    int updateUnfreezeStatusByThirdOrderNo(String thirdOrderNo, Integer status, Integer unfreezeStatus, List<Integer> businessTypeList, long updateTime);
    
    /**
     * 根据id批量查询
     *
     * @param tenantId
     * @param ids
     * @author caobotao.cbt
     * @date 2024/8/29 18:24
     */
    List<ProfitSharingOrderDetail> queryListByProfitSharingOrderIds(Integer tenantId, List<Long> ids);
}
