package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeMixedOrderQueryModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 分账交易混合订单(TProfitSharingTradeMixedOrder)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-27 19:19:18
 */
public interface ProfitSharingTradeMixedOrderMapper {
    
    
    /**
     * 新增数据
     *
     * @param profitSharingTradeMixedOrder 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder);
    
    
    /**
     * 列表查询
     *
     * @param queryModel
     * @author caobotao.cbt
     * @date 2024/8/28 08:56
     */
    List<ProfitSharingTradeMixedOrder> selectListByParam(ProfitSharingTradeMixedOrderQueryModel queryModel);
    
    /**
     * 状态更新
     *
     * @param mixedOrder
     * @author caobotao.cbt
     * @date 2024/8/28 09:58
     */
    int updateStatusById(ProfitSharingTradeMixedOrder mixedOrder);
    
    ProfitSharingTradeMixedOrder selectByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
    
    List<ProfitSharingTradeMixedOrder> selectListThirdOrderNoByTenantId(@Param("tenantId") Integer tenantId, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("startId") Long startId, @Param("size") Integer size);
    
    int updateThirdOrderNoById(ProfitSharingTradeMixedOrder profitSharingTradeMixedOrderUpdate);
    
    ProfitSharingTradeMixedOrder selectById(@Param("id") Long profitSharingMixedOrderId);
}

