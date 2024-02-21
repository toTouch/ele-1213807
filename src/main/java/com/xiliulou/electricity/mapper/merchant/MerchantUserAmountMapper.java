package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.query.merchant.MerchantUserAmountQueryMode;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 19:32
 * @desc
 */
public interface MerchantUserAmountMapper {
    
    Integer insert(MerchantUserAmount merchantUserAmount);
    
    List<MerchantUserAmount> queryList(MerchantUserAmountQueryMode joinRecordQueryMode);
    
    Integer addAmountByUid(@Param("income") BigDecimal income, @Param("uid") Long uid, @Param("tenantId") Long tenantId);
    
    Integer reduceAmountByUid(@Param("income") BigDecimal income, @Param("uid") Long uid, @Param("tenantId") Long tenantId);
}
