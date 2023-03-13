package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.vo.ElectricityCarMoveVo;
import com.xiliulou.electricity.vo.ElectricityCarOverviewVo;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜表(TElectricityCar)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarMapper extends BaseMapper<ElectricityCar> {


    List<ElectricityCarVO> queryList(@Param("query") ElectricityCarQuery electricityCarQuery);

    Integer queryCount(@Param("query") ElectricityCarQuery electricityCarQuery);

    Integer updateBindUser(ElectricityCar electricityCar);

    Integer queryCountByStoreIds(@Param("tenantId") Integer tenantId,@Param("storeIds") List<Long> storeIds);

    ElectricityCar selectBySn(@Param("sn") String sn, @Param("tenantId") Integer tenantId);
    
    Integer updateLockTypeById(@Param("ids") List<Long> tempIds, @Param("typeLock") Integer typeLock);
    
    List<ElectricityCar> queryByStoreIds(@Param("storeIds") List<Long> storeIds, @Param("tenantId") Integer tenantId);
    
    List<ElectricityCarOverviewVo> queryElectricityCarOverview(@Param("carIds") List<Integer> sidList,
            @Param("sn") String sn,
            @Param("tenantId") Integer tenantId);
    
    Long batteryStatistical(@Param("carIds") List<Integer> carIdList, @Param("tenantId") Integer tenantId);
    
    List<ElectricityCarMoveVo> queryEnableMoveCarByStoreId(@Param("storeId") Long storeId,
            @Param("tenantId") Integer tenantId);
    
    List<Long> queryIdsBySidAndIds(@Param("carIds") List<Long> carIds, @Param("sid") Long sid,
            @Param("status") Integer status, @Param("tenantId") Integer tenantId);
    
    Integer updateStoreIdByIds(@Param("id") Long id, @Param("carIds") List<Long> carIds,
            @Param("updateTime") Long updateTime, @Param("tenantId") Integer tenantId);
}
