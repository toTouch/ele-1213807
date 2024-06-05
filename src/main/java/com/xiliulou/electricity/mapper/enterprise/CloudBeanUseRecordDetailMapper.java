package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecordDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/4/17 17:25
 * @desc
 */
public interface CloudBeanUseRecordDetailMapper {
    
    int batchInsert(@Param("list") List<CloudBeanUseRecordDetail> cloudBeanUseRecordDetailList);
}
