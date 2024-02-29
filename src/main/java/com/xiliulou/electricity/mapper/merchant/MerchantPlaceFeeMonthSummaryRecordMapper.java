package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthSummaryRecord;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeMonthSummaryRecordQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeMonthSummaryRecordVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName : MerchantPlaceFeeMonthSummaryRecordMapper
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */
@Repository
public interface MerchantPlaceFeeMonthSummaryRecordMapper {
    Integer save(MerchantPlaceFeeMonthSummaryRecord record);
    
    List<MerchantPlaceFeeMonthSummaryRecordVO> selectListByCondition(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel);
    
    Integer pageCountByCondition(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel);
}
