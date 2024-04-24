package com.xiliulou.electricity.service.impl.enterprise;

import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecordDetail;
import com.xiliulou.electricity.mapper.enterprise.CloudBeanUseRecordDetailMapper;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/4/17 17:23
 * @desc
 */

@Service("cloudBeanUseRecordDetailService")
@Slf4j
public class CloudBeanUseRecordDetailServiceImpl implements CloudBeanUseRecordDetailService {
    @Resource
    private CloudBeanUseRecordDetailMapper cloudBeanUseRecordDetailMapper;
    
    
    @Override
    public int batchInsert(List<CloudBeanUseRecordDetail> cloudBeanUseRecordDetailList) {
        return cloudBeanUseRecordDetailMapper.batchInsert(cloudBeanUseRecordDetailList);
    }
}
