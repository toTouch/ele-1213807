package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.HomepageElectricityExchangeFrequencyQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeFrequencyVo;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderMapper extends BaseMapper<ElectricityCabinetOrder> {

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<ElectricityCabinetOrderVO> queryList( @Param("query") ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    Integer queryCount( @Param("query") ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    Integer homepageExchangeOrderSumCount( @Param("query") HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency(@Param("query") HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequencyCount(@Param("query") HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    Integer homeOneCount(@Param("first") Long first, @Param("now") Long now, @Param("eleIdList") List<Integer> eleIdList,@Param("tenantId")Integer tenantId);

    Integer homeOneSuccess(@Param("first") Long first, @Param("now") Long now, @Param("eleIdList") List<Integer> eleIdList,@Param("tenantId")Integer tenantId);

    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("eleIdList") List<Integer> eleIdList ,@Param("tenantId")Integer tenantId);

    ElectricityCabinetOrder selectLatestByUid(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);

    List<ElectricityCabinetOrder> selectTodayExchangeOrder(@Param("eid") Integer eid, @Param("todayStartTimeStamp") long todayStartTimeStamp, @Param("todayEndTimeStamp") long todayEndTimeStamp, @Param("tenantId") Integer tenantId);

    List<ElectricityCabinetOrder> selectMonthExchangeOrders(@Param("eid") Integer eid, @Param("startTimeStamp") long startTimeStamp, @Param("endTimeStamp") long endTimeStamp, @Param("tenantId") Integer tenantId);

}
