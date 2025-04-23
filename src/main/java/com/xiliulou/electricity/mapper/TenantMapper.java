package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Tenant;

import java.util.List;

import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.vo.TenantVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租户表(Tenant)表数据库访问层
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
public interface TenantMapper extends BaseMapper<Tenant> {
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param tenantQuery 实例对象
     * @return 对象列表
     */
    List<TenantVO> queryAll(TenantQuery tenantQuery);
    
    Integer queryCount(TenantQuery tenantQuery);
    
    /**
     * 查询租户id
     *
     * @param startId
     * @param size
     * @author caobotao.cbt
     * @date 2024/8/26 16:37
     */
    List<Integer> selectIdListByStartId(@Param("startId") Integer startId, @Param("size") Integer size);


    List<Tenant> selectListByIds(@Param("idList") List<Integer> tenantIdList);


    List<Tenant> selectListAll();
}
