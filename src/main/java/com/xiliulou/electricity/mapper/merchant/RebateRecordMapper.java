package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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
    
    RebateRecord selectByOriginalOrderId(String originalOrderId);
    
    List<RebateRecord> selectNotSettleListByLimit(@Param("startTime") long startTime, @Param("endTime") long endTime, @Param("offset") int offset, @Param("size") int size);
    
    List<RebateRecord> selectCurrentMonthRebateRecord(@Param("startTime") long startTime, @Param("endTime") long endTime, @Param("offset") int offset, @Param("size") int size);
    
    BigDecimal sumByStatus(MerchantPromotionFeeQueryModel merchantPromotionFeeQueryModel);
    
    Integer countByTime(MerchantPromotionRenewalQueryModel queryModel);
    
    List<RebateRecord> selectListPromotionDetail(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel);
}
