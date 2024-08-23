package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
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
}

