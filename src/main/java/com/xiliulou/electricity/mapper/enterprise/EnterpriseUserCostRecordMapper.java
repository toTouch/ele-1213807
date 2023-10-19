package com.xiliulou.electricity.mapper.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseUserCostRecord;
import com.xiliulou.electricity.query.enterprise.EnterpriseUserCostRecordQuery;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 9:37
 */
public interface EnterpriseUserCostRecordMapper extends BaseMapper<EnterpriseRentRecord> {
    
    /**
     * 新增数据
     *
     * @param enterpriseUserCostRecord 实例对象
     * @return 影响行数
     */
    int insertOne(EnterpriseUserCostRecord enterpriseUserCostRecord);
    
    /**
     * 根据ID查找消费详情信息
     * @param id
     * @return
     */
    EnterpriseUserCostRecord selectById(Long id);
    
    /**
     * 根据UID查询消费详情列表
     * @param uid
     * @return
     */
    List<EnterpriseUserCostRecord> selectByUid(Long uid);
    
    /**
     * 查询用户消费详情列表信息
     * @param query
     * @return
     */
    List<EnterpriseUserCostRecord> selectUserCostList(EnterpriseUserCostRecordQuery query);
    
}
