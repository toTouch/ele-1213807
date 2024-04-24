package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecordDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/4/15 16:13
 * @desc
 */
public interface EnterpriseRentRecordDetailMapper {
    
    int batchInsert(@Param("list") List<EnterpriseRentRecordDetail> list);
    
    List<EnterpriseRentRecordDetail> selectListByUid(@Param("uid") Long uid);
}
