package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanRecord;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRecordQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseCloudBeanRecordVO;

import java.util.List;

/**
 * 企业云豆操作记录表(EnterpriseCloudBeanRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-09-14 10:16:20
 */
public interface EnterpriseCloudBeanRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseCloudBeanRecord queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseCloudBeanRecord queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanRecord 实例对象
     * @return 实例对象
     */
    Integer update(EnterpriseCloudBeanRecord enterpriseCloudBeanRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer insert(EnterpriseCloudBeanRecord enterpriseCloudBeanRecord);

    List<EnterpriseCloudBeanRecordVO> selectByPage(EnterpriseCloudBeanRecordQuery query);

    Integer selectByPageCount(EnterpriseCloudBeanRecordQuery query);
}
