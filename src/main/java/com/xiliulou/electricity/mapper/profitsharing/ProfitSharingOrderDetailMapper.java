package com.xiliulou.electricity.mapper.profitsharing;


import com.xiliulou.electricity.bo.profitsharing.ProfitSharingOrderTypeUnfreezeBO;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderDetailQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分账订单明细表(ProfitSharingOrderDetail)表数据库访问层
 *
 * @author maxiaodong
 * @since 2024-08-22 17:00:34
 */
public interface ProfitSharingOrderDetailMapper {
    
    int batchInsert(@Param("list") List<ProfitSharingOrderDetail> profitSharingOrderDetailList);
    
    Integer countTotal(ProfitSharingOrderDetailQueryModel queryModel);
    
    List<ProfitSharingOrderDetail> selectListByPage(ProfitSharingOrderDetailQueryModel queryModel);
    
    int insert(ProfitSharingOrderDetail profitSharingOrderDetail);
    
    List<String> selectListUnfreezeByThirdOrderNo(@Param("list") List<String> thirdOrderNoList);
    
    Integer existsNotUnfreezeByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
    
    Integer existsNotCompleteByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
    
    Integer existsFailByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
    
    int updateUnfreezeStatusByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo,@Param("status") Integer status,@Param("unfreezeStatus") Integer unfreezeStatus,@Param("businessTypeList") List<Integer> businessTypeList, long updateTime);
    
    List<ProfitSharingOrderTypeUnfreezeBO> selectListOrderTypeUnfreeze(@Param("tenantId") Integer tenantId, @Param("startId") Long startId,@Param("size") Integer size);
    
    int updateUnfreezeOrderById(ProfitSharingOrderDetail profitSharingOrderDetailUpdate);
    
    List<ProfitSharingOrderDetail> selectListFailByThirdOrderNo(@Param("thirdTradeOrderNo") String thirdTradeOrderNo);
}

