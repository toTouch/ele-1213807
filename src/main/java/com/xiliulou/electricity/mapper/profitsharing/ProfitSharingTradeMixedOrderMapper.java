package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
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
    

}

