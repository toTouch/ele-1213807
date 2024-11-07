package com.xiliulou.electricity.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
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
    
    List<RebateRecord> selectByOriginalOrderId(String originalOrderId);
    
    RebateRecord selectLatestByOriginalOrderId(String originalOrderId);
    
    List<RebateRecord> selectNotSettleListByLimit(@Param("startTime") long startTime, @Param("endTime") long endTime, @Param("offset") int offset, @Param("size") int size);
    
    List<RebateRecord> selectCurrentMonthRebateRecord(@Param("merchantId") Long merchantId, @Param("startTime") long startTime,
            @Param("endTime") long endTime, @Param("offset") int offset, @Param("size") int size);
    
    BigDecimal sumMerchantIncomeByStatus(MerchantPromotionFeeQueryModel merchantPromotionFeeQueryModel);
    
    BigDecimal sumEmployeeIncomeByStatus(MerchantPromotionFeeQueryModel merchantPromotionFeeQueryModel);
    
    Integer countByTime(MerchantPromotionRenewalQueryModel queryModel);
    
    List<RebateRecord> selectListPromotionDetail(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel);
    
    List<RebateRecord> selectRebatedByUid(@Param("uid") Long uid, @Param("memberCardId") Long memberCardId, @Param("merchantId") Long merchantId, @Param("currentLevel") String currentLevel);
    
    Integer existsExpireRebateRecordByOriginalOrderId(String originalOrderId);
    
    List<MerchantStatisticsUserVO> selectListRenewal(MerchantPromotionRenewalQueryModel renewalQueryModel);
}
