package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecordDetail;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/4/15 17:32
 * @desc
 */
public interface EnterpriseRentRecordDetailService {
    
    int batchInsert(List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList);
    
    List<EnterpriseRentRecordDetail> queryListByUid(Long uid);
    
    int removeByUid(Long uid);
}
