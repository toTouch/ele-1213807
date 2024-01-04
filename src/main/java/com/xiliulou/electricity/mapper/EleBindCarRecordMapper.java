package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleBindCarRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.EleBindCarRecordQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.BorrowExpireBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜电池表(EleBindCarRecord)表数据库访问层
 *
 * @author makejava
 * @since 2022-06-16 14:44:12
 */
public interface EleBindCarRecordMapper extends BaseMapper<EleBindCarRecord> {

    List<EleBindCarRecord> queryList(@Param("query") EleBindCarRecordQuery eleBindCarRecordQuery);

    Integer queryCount(@Param("query") EleBindCarRecordQuery eleBindCarRecordQuery);
    
    /**
     * 根据更换手机号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
}
