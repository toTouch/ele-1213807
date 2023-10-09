package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import com.xiliulou.electricity.vo.enterprise.CloudBeanUseRecordVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 云豆使用记录表(CloudBeanUseRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-09-18 10:35:12
 */
public interface CloudBeanUseRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    CloudBeanUseRecord queryByIdFromDB(Long id);

    Integer insert(CloudBeanUseRecord cloudBeanUseRecord);

    /**
     * 修改数据
     *
     * @param cloudBeanUseRecord 实例对象
     * @return 实例对象
     */
    Integer update(CloudBeanUseRecord cloudBeanUseRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Double selectCloudBeanByUidAndType(Long uid, Integer type);

    List<CloudBeanUseRecordVO> selectByUserPage(CloudBeanUseRecordQuery query);

    CloudBeanUseRecordVO cloudBeanUseStatisticsByUid(CloudBeanUseRecordQuery query);

    Triple<Boolean, String, Object> recycleDepositMembercard(Long uid);
    
    void recycleCloudBeanTask();
    
    List<CloudBeanUseRecord> selectByEnterpriseIdAndType(Long id, Integer typePayMembercard);
    
    List<CloudBeanUseRecord> selectCanRecycleRecord(Long id, long currentTimeMillis);
    
    Triple<Boolean, String, Object> cloudBeanOrderDownload(Long beginTime, Long endTime);
}
