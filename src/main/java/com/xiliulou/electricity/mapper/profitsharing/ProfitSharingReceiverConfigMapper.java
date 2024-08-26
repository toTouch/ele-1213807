package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingReceiverConfigModel;
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
     * 新增数据
     *
     * @param profitSharingReceiverConfig 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingReceiverConfig profitSharingReceiverConfig);
    
    
    /**
     * 修改数据
     *
     * @param profitSharingReceiverConfig 实例对象
     * @return 影响行数
     */
    int update(ProfitSharingReceiverConfig profitSharingReceiverConfig);
    
    
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
    
    
    /**
     * 根据id查询
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/26 11:30
     */
    ProfitSharingReceiverConfig selectById(@Param("tenantId") Integer tenantId, @Param("id") Long id);
    
    /**
     * 更新状态
     *
     * @param receiverConfig
     * @author caobotao.cbt
     * @date 2024/8/26 13:53
     */
    int updateStatus(ProfitSharingReceiverConfig receiverConfig);
    
    /**
     * 逻辑删除
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/26 14:00
     */
    void removeById(@Param("tenantId") Integer tenantId, @Param("id") Long id);
    
    
    /**
     * 分页
     *
     * @param configModel
     * @author caobotao.cbt
     * @date 2024/8/26 14:38
     */
    List<ProfitSharingReceiverConfig> selectPage(ProfitSharingReceiverConfigModel configModel);
    
    
    /**
     * 分页
     *
     * @param configModel
     * @author caobotao.cbt
     * @date 2024/8/26 14:38
     */
    Integer count(ProfitSharingReceiverConfigModel configModel);
}

