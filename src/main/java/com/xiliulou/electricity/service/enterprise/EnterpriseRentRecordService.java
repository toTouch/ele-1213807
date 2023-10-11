package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;

import java.util.List;

/**
 * 用户使用记录表(EnterpriseRentRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-10-10 20:03:40
 */
public interface EnterpriseRentRecordService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseRentRecord queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseRentRecord queryByIdFromCache(Long id);
    
    /**
     * 修改数据
     *
     * @param enterpriseRentRecord 实例对象
     * @return 实例对象
     */
    Integer update(EnterpriseRentRecord enterpriseRentRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    void saveEnterpriseRentRecord(Long uid);
    
    void saveEnterpriseReturnRecord(Long uid);
    
    List<EnterpriseRentRecord> selectByUidAndTime(Long uid, long startTime);
}
