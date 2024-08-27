package com.xiliulou.electricity.mapper.profitsharing;


import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分账订单表(profitSharingOrder)表数据库访问层
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:53
 */
public interface ProfitSharingOrderMapper {
    int insert(ProfitSharingOrder profitSharingOrder);
    
    List<ProfitSharingOrder> selectListByIds(@Param("idList") List<Long> profitSharingOrderIdList);
    
    Integer existsUnfreezeByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
}

