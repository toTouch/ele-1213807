package com.xiliulou.electricity.mapper.profitsharing;


import com.xiliulou.electricity.bo.profitsharing.ProfitSharingOrderTypeUnfreezeBO;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderDetailQueryModel;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderQueryModel;
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
    
    int updateUnfreezeStatusByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo, @Param("status") Integer status, @Param("unfreezeStatus") Integer unfreezeStatus,
            @Param("businessTypeList") List<Integer> businessTypeList,@Param("updateTime") long updateTime);
    
    /**
     * 更新
     *
     * @param profitSharingOrderDetail
     * @author caobotao.cbt
     * @date 2024/8/29 15:31
     */
    int update(ProfitSharingOrderDetail profitSharingOrderDetail);
    
    
    List<ProfitSharingOrderTypeUnfreezeBO> selectListOrderTypeUnfreeze(@Param("tenantId") Integer tenantId, @Param("startId") Long startId,@Param("size") Integer size);
    
    int updateUnfreezeOrderById(ProfitSharingOrderDetail profitSharingOrderDetailUpdate);
    
    List<ProfitSharingOrderDetail> selectListFailByThirdOrderNo(@Param("thirdTradeOrderNo") String thirdTradeOrderNo);
    
   
    
    
    /**
     * 根据分账主表订单号查询
     *
     * @param tenantId
     * @param profitSharingOrderIds
     * @author caobotao.cbt
     * @date 2024/8/29 18:27
     */
    List<ProfitSharingOrderDetail> selectListByProfitSharingOrderIds(@Param("tenantId") Integer tenantId, @Param("profitSharingOrderIds") List<Long> profitSharingOrderIds);
}

