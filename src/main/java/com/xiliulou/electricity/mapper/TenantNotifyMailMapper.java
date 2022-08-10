package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.TenantNotifyMail;

import java.util.List;

import com.xiliulou.electricity.query.UpgradeNotifyMailQuery;
import com.xiliulou.electricity.vo.TenantNotifyMailVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UpgradeNotifyMail)表数据库访问层
 *
 * @author zzlong
 * @since 2022-08-08 15:30:14
 */
public interface TenantNotifyMailMapper extends BaseMapper<TenantNotifyMail> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TenantNotifyMail selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<TenantNotifyMailVO> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param tenantNotifyMail 实例对象
     * @return 对象列表
     */
    List<TenantNotifyMail> selectByQuery(TenantNotifyMail tenantNotifyMail);

    /**
     * 新增数据
     *
     * @param tenantNotifyMail 实例对象
     * @return 影响行数
     */
    int insertOne(TenantNotifyMail tenantNotifyMail);

    /**
     * 修改数据
     *
     * @param tenantNotifyMail 实例对象
     * @return 影响行数
     */
    int update(TenantNotifyMail tenantNotifyMail);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * @param upgradeNotifyMailQuery
     * @return
     */
    int insertOrUpdate(UpgradeNotifyMailQuery upgradeNotifyMailQuery);

    int deleteByTenantId(Integer tenantId);

    int batchInsert(List<TenantNotifyMail> list);

    List<TenantNotifyMailVO> selectByTenantId(@Param("tenantId") Long tenantId);
}
