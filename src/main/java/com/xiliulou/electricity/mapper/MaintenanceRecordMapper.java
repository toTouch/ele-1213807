package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.MaintenanceRecord;
import com.xiliulou.electricity.query.MaintenanceRecordListQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (MaintenanceRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-09-26 14:07:39
 */
public interface MaintenanceRecordMapper extends BaseMapper<MaintenanceRecord>{


    List<MaintenanceRecord> queryList(MaintenanceRecordListQuery query);

	Integer queryCount(MaintenanceRecordListQuery query);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
}
