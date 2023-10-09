package com.xiliulou.electricity.mapper.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseRentBatteryOrderQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseFreezePackageRecordVO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseRefundDepositOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseRentBatteryOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/23 8:36
 */
public interface EnterpriseBatteryPackageMapper extends BaseMapper<ElectricityMemberCardOrder> {

    Integer insertMemberCardOrder(ElectricityMemberCardOrder memberCardOrder);

    List<EnterprisePackageOrderVO> queryBatteryPackageOrder(@Param("query") EnterprisePackageOrderQuery query);
    
    List<EnterpriseRentBatteryOrderVO> queryRentBatteryOrder(@Param("query") EnterpriseRentBatteryOrderQuery query);
    
    List<EnterpriseFreezePackageRecordVO> queryBatteryFreezeOrder(@Param("query") EnterprisePackageOrderQuery query);
    
    List<EnterpriseRefundDepositOrderVO> queryBatteryDepositOrder(@Param("query") EnterprisePackageOrderQuery query);
    
    List<EnterprisePackageOrderVO> queryExpiredPackageOrder(EnterprisePurchaseOrderQuery query);
    
    List<EnterprisePackageOrderVO> queryPaidPackageOrder(EnterprisePurchaseOrderQuery query);
    
    List<EnterprisePackageOrderVO> queryUnpaidPackageOrder(EnterprisePurchaseOrderQuery query);
    
}
