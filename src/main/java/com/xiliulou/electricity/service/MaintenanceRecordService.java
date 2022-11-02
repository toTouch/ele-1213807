package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.MaintenanceRecord;
import com.xiliulou.electricity.query.MaintenanceRecordHandleQuery;
import com.xiliulou.electricity.query.MaintenanceRecordListQuery;
import com.xiliulou.electricity.query.UserMaintenanceQuery;
import org.apache.commons.lang3.tuple.Triple;


/**
 * (MaintenanceRecord)表服务接口
 *
 * @author makejava
 * @since 2021-09-26 14:07:39
 */
public interface MaintenanceRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    MaintenanceRecord queryByIdFromDB(Long id,Integer tenantId);



    /**
     * 新增数据
     *
     * @param maintenanceRecord 实例对象
     * @return 实例对象
     */
    MaintenanceRecord insert(MaintenanceRecord maintenanceRecord);

    /**
     * 修改数据
     *
     * @param maintenanceRecord 实例对象
     * @return 实例对象
     */
    Integer update(MaintenanceRecord maintenanceRecord);


    Triple<Boolean, String, Object> saveSubmitRecord(UserMaintenanceQuery userMaintenanceQuery);

    Triple<Boolean, String, Object> queryListForUser(MaintenanceRecordListQuery query);

    Triple<Boolean, String, Object> queryListForAdmin(MaintenanceRecordListQuery query);

    Triple<Boolean, String, Object> handleMaintenanceRecord(MaintenanceRecordHandleQuery maintenanceRecordHandleQuery);

	R queryCountForUser(MaintenanceRecordListQuery query);

    R queryCountForAdmin(MaintenanceRecordListQuery query);
}
