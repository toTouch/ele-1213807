package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecordDetail;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/4/17 17:22
 * @desc
 */
public interface CloudBeanUseRecordDetailService {
    
    int batchInsert(List<CloudBeanUseRecordDetail> cloudBeanUseRecordDetailList);
}
