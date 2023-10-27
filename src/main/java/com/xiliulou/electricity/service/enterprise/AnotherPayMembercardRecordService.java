package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;

import java.util.List;

/**
 * 代付记录表(AnotherPayMembercardRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-10-10 15:07:39
 */
public interface AnotherPayMembercardRecordService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    AnotherPayMembercardRecord queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    AnotherPayMembercardRecord queryByIdFromCache(Long id);
    
    /**
     * 修改数据
     *
     * @param anotherPayMembercardRecord 实例对象
     * @return 实例对象
     */
    Integer update(AnotherPayMembercardRecord anotherPayMembercardRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    int saveAnotherPayMembercardRecord(Long uid, String orderId, Integer tenantId);
    
    List<AnotherPayMembercardRecord> selectByUid(Long uid);
    
    int deleteByUid(Long uid);
    
    List<AnotherPayMembercardRecord> selectListByEnterpriseId(Long enterpriseId);
    
    AnotherPayMembercardRecord selectByOrderId(String orderId);
}
