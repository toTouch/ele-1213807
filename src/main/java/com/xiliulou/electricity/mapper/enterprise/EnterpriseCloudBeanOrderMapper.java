package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanOrderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业云豆充值订单表(EnterpriseCloudBeanOrder)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-15 09:29:15
 */
public interface EnterpriseCloudBeanOrderMapper extends BaseMapper<EnterpriseCloudBeanOrder> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseCloudBeanOrder queryById(Long id);

    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanOrder 实例对象
     * @return 影响行数
     */
    int update(EnterpriseCloudBeanOrder enterpriseCloudBeanOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectByPageCount(EnterpriseCloudBeanOrderQuery query);

    List<EnterpriseCloudBeanOrder> selectByPage(EnterpriseCloudBeanOrderQuery query);
}
