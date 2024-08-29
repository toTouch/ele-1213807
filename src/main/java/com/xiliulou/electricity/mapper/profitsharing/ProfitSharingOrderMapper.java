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
    
    /**
     * 批量新增
     *
     * @param insertProfitSharingOrderList
     * @author caobotao.cbt
     * @date 2024/8/28 11:15
     */
    int batchInsert(List<ProfitSharingOrder> insertProfitSharingOrderList);
    
    
    Integer existsUnfreezeByThirdOrderNo(@Param("thirdOrderNo") String thirdOrderNo);
    
    /**
     * 更新
     *
     * @param profitSharingOrder
     * @author caobotao.cbt
     * @date 2024/8/29 15:14
     */
    int update(ProfitSharingOrder profitSharingOrder);
}

