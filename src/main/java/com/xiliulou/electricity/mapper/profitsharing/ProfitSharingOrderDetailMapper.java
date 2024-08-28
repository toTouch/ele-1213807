package com.xiliulou.electricity.mapper.profitsharing;


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
}

