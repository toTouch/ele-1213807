package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleCabinetUsedRecord;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.query.EleCabinetUsedRecordQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.vo.RentBatteryOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租电池记录(TRentBatteryOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
public interface RentBatteryOrderMapper extends BaseMapper<RentBatteryOrder> {


    /**
     * 查询指定行数据
     */
    List<RentBatteryOrderVO> queryList(@Param("query") RentBatteryOrderQuery rentBatteryOrderQuery);

	Integer queryCount(@Param("query") RentBatteryOrderQuery rentBatteryOrderQuery);

    RentBatteryOrder selectLatestByUid(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);

    List<EleCabinetUsedRecord> selectEleCabinetUsedRecords(@Param("query") EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery);

    Integer selectUsedRecordsTotalCount(@Param("query") EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery);
    
    List<RentBatteryOrder> selectByUidAndTime(@Param("uid") Long uid, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
    
    Integer existSameCabinetCellSameTimeOpenReturnOrder(@Param("createTime") Long createTime, @Param("electricityCabinetId") Integer electricityCabinetId,
            @Param("oldCellNo") Integer oldCellNo);
}
