package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionEmployeeDetailSpecificsVO;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
import com.xiliulou.electricity.vo.merchant.RebateRecordVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * (RebateRecord)表服务接口
 *
 * @author zzlong
 * @since 2024-02-20 14:31:51
 */
public interface RebateRecordService {
    
    RebateRecord queryById(Long id);
    
    RebateRecord queryByOrderId(String orderId);

    List<RebateRecord> queryByOriginalOrderId(String originalOrderId);
    RebateRecord queryLatestByOriginalOrderId(String originalOrderId);
    
    RebateRecord insert(RebateRecord rebateRecord);
    
    Integer updateById(RebateRecord rebateRecord);
    
    Integer deleteById(Long id);
    
    List<RebateRecordVO> listByPage(RebateRecordRequest query);
    
    Integer countByPage(RebateRecordRequest query);
    
    /**
     * 返利结算定时任务
     */
    void settleRebateRecordTask();
    
    void handleRebate(RebateRecord rebateRecord);
    
    List<RebateRecord> listCurrentMonthRebateRecord( Long merchantId, long startTime, long endTime, int offset, int size);
    
    BigDecimal sumByStatus(MerchantPromotionFeeQueryModel merchantPromotionFeeQueryModel);
    
    Integer countByTime(MerchantPromotionRenewalQueryModel merchantPromotionRenewalQueryModel);
    
    List<MerchantPromotionEmployeeDetailSpecificsVO> selectListPromotionDetail(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel);
    
    /**
     * 获取比当前商户等级小的返利记录
     */
    List<RebateRecord> listRebatedByUid(Long uid, Long memberCardId, Long merchantId, String currentLevel);
    
    Integer existsExpireRebateRecordByOriginalOrderId(String originalOrderId);
    
    List<MerchantStatisticsUserVO> listRenewal(MerchantPromotionRenewalQueryModel renewalQueryModel);

    boolean existsRebateRecord(String messageId);
}
