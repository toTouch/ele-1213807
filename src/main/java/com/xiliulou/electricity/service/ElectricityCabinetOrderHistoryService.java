package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderHistory;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单历史表
 */
public interface ElectricityCabinetOrderHistoryService {
    
    
    List<ElectricityCabinetOrderVO> queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
    R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
    Integer queryCountForScreenStatistic(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
    Integer homeOneCount(Long first, Long now, List<Integer> eleIdList, Integer tenantId);
    
    BigDecimal homeOneSuccess(Long first, Long now, List<Integer> eleIdList, Integer tenantId);
    
    Integer homeTotal(Long uid);
    
    ElectricityCabinetOrderHistory queryByUid(Long uid);
    
    ElectricityCabinetOrderHistory selectLatestByUidV2(Long uid);
    
    ElectricityCabinetOrderVO selectLatestOrderAndCabinetInfo(Long uid);
    
    List<ElectricityCabinetOrderVO> listSuperAdminPage(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
}
