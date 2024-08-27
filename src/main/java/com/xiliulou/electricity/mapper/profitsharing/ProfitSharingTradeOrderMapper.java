package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.domain.profitsharing.ProfitSharingTradeOrderThirdOrderNoDO;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeOrderQueryModel;
import org.apache.ibatis.annotations.Param;



import java.util.List;

/**
 * 分账交易订单(TProfitSharingTradeOrder)表数据库访问层
 *
 * @author caobotao
 * @since 2024-08-22 17:32:59
 */
public interface ProfitSharingTradeOrderMapper {
    
    /**
     * 新增数据
     *
     * @param profitSharingTradeOrder 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingTradeOrder profitSharingTradeOrder);
    
    int batchInsert(@Param("list") List<ProfitSharingTradeOrder> profitSharingTradeOrderList);
    
    ProfitSharingTradeOrder selectByOrderNo(@Param("orderNo") String orderNo);
    
    int updateById(ProfitSharingTradeOrder profitSharingUpdate);
    
    /**
     * 条件查询
     *
     * @param queryModel
     * @author caobotao.cbt
     * @date 2024/8/26 17:12
     */
    List<ProfitSharingTradeOrderThirdOrderNoDO> selectThirdOrderNoListByParam(ProfitSharingTradeOrderQueryModel queryModel);
    
    /**
     * 根据租户id+第三方订单号查询
     *
     * @param tenantId
     * @param thirdOrderNos
     * @author caobotao.cbt
     * @date 2024/8/26 18:02
     */
    List<ProfitSharingTradeOrder> selectListByThirdOrderNos(@Param("tenantId") Integer tenantId, @Param("thirdOrderNos") List<String> thirdOrderNos);
    
    Integer existsNotRefundByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo, @Param("orderNo") String orderNo);
}

