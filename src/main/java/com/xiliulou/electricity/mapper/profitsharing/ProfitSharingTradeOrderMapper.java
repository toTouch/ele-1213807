package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
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
     * @param tenantId
     * @param channel
     * @param thirdOrderNos
     * @author caobotao.cbt
     * @date 2024/8/28 09:25
     */
    List<ProfitSharingTradeOrder> selectListByThirdOrderNosAndChannelAndProcessState(@Param("tenantId") Integer tenantId, @Param("processState") Integer processState,
            @Param("channel") String channel, @Param("thirdOrderNos") List<String> thirdOrderNos);
    
    
    /**
     * 批量更新状态
     *
     * @param ids
     * @param processState
     * @param updateTime
     * @param remark
     * @author caobotao.cbt
     * @date 2024/8/28 10:53
     */
    int batchUpdateStateByIds(@Param("ids") List<Long> ids, @Param("processState") Integer processState, @Param("updateTime") Long updateTime, @Param("remark")String remark);
    
    
    Integer existsNotRefundByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo, @Param("orderNo") String orderNo);
    
    String selectOrderNoyByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
}

