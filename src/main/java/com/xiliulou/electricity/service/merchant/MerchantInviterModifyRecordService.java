package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.request.merchant.MerchantInviterModifyRecordRequest;
import com.xiliulou.electricity.vo.merchant.MerchantInviterModifyRecordVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 邀请人修改记录
 * @date 2024/3/28 09:34:11
 */
public interface MerchantInviterModifyRecordService {
    
    Integer insertOne(MerchantInviterModifyRecord record);
    
    List<MerchantInviterModifyRecordVO> listByPage(MerchantInviterModifyRecordRequest request);
    
    Integer countTotal(MerchantInviterModifyRecordRequest request);
    
    boolean existsModifyRecordByUid(Long uid);
    
}
