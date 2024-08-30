package com.xiliulou.electricity.mapper.profitsharing;


import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderQueryModel;
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
    
    /**
     * 根据租户id+第三方单号查询
     *
     * @param tenantId
     * @param thirdOrderNos
     * @author caobotao.cbt
     * @date 2024/8/30 08:46
     */
    List<ProfitSharingOrder> selectListByThirdOrderNos(@Param("tenantId") Integer tenantId, @Param("thirdOrderNos") List<String> thirdOrderNos);
    
    /**
     * 多条件
     *
     * @param profitSharingOrderQueryModel
     * @author caobotao.cbt
     * @date 2024/8/30 09:00
     */
    List<ProfitSharingOrder> selectByIdGreaterThanAndOtherConditions(ProfitSharingOrderQueryModel profitSharingOrderQueryModel);
}

