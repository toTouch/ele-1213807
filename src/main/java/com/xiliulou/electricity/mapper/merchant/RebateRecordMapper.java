package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (RebateRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2024-02-20 14:31:51
 */
public interface RebateRecordMapper extends BaseMapper<RebateRecord> {

    RebateRecord selectById(Long id);

    int insertOne(RebateRecord rebateRecord);

    int update(RebateRecord rebateRecord);

    int deleteById(Long id);
    
}
