package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderHistory;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单表(TElectricityCabinetOrderHistory)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderHistoryMapper extends BaseMapper<ElectricityCabinetOrderHistory> {
    
    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<ElectricityCabinetOrderVO> queryList(@Param("query") ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
    Integer queryCount(@Param("query") ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
    Integer homeOneCount(@Param("first") Long first, @Param("now") Long now, @Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);
    
    Integer homeOneSuccess(@Param("first") Long first, @Param("now") Long now, @Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);
    
    ElectricityCabinetOrderHistory selectLatestByUidV2(Long uid);
    
    ElectricityCabinetOrderVO selectLatestOrderAndCabinetInfo(Long uid);
    
    List<ElectricityCabinetOrderVO> selectListSuperAdminPage(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);
    
}
