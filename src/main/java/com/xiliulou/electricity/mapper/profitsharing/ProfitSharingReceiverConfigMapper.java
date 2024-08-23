package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 分账接收方配置表(TProfitSharingReceiverConfig)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-22 17:27:52
 */
public interface ProfitSharingReceiverConfigMapper {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ProfitSharingReceiverConfig queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param profitSharingReceiverConfig 查询条件
     * @param pageable                    分页对象
     * @return 对象列表
     */
    List<ProfitSharingReceiverConfig> queryAllByLimit(ProfitSharingReceiverConfig profitSharingReceiverConfig, @Param("pageable") Pageable pageable);
    
    /**
     * 统计总行数
     *
     * @param profitSharingReceiverConfig 查询条件
     * @return 总行数
     */
    long count(ProfitSharingReceiverConfig profitSharingReceiverConfig);
    
    /**
     * 新增数据
     *
     * @param profitSharingReceiverConfig 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingReceiverConfig profitSharingReceiverConfig);
    
    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TProfitSharingReceiverConfig> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<ProfitSharingReceiverConfig> entities);
    
    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TProfitSharingReceiverConfig> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<ProfitSharingReceiverConfig> entities);
    
    /**
     * 修改数据
     *
     * @param profitSharingReceiverConfig 实例对象
     * @return 影响行数
     */
    int update(ProfitSharingReceiverConfig profitSharingReceiverConfig);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 根据租户id+主表id
     *
     * @param tenantId
     * @param profitSharingConfigId
     * @author caobotao.cbt
     * @date 2024/8/23 14:43
     */
    List<ProfitSharingReceiverConfig> selectListByTenantIdAndProfitSharingConfigId(@Param("tenantId") Integer tenantId, @Param("profitSharingConfigId") Long profitSharingConfigId);
    
    /**
     * 逻辑删除
     *
     * @param tenantId
     * @param receiverConfigIds
     * @param updateTime
     * @author caobotao.cbt
     * @date 2024/8/23 15:27
     */
    int removeByIds(@Param("tenantId") Integer tenantId, @Param("receiverConfigIds") List<Long> receiverConfigIds, @Param("updateTime") long updateTime);
}

