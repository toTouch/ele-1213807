package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;

import java.util.List;

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
    
    RebateRecord selectByOrderId(String orderId);
    
    Integer selectByPageCount(RebateRecordRequest query);
    
    List<RebateRecord> selectByPage(RebateRecordRequest query);
}
