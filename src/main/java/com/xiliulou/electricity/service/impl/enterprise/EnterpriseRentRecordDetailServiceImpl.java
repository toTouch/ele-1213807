package com.xiliulou.electricity.service.impl.enterprise;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecordDetail;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseRentRecordDetailMapper;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/4/15 17:33
 * @desc
 */
@Service("enterpriseRentRecordDetailService")
@Slf4j
public class EnterpriseRentRecordDetailServiceImpl implements EnterpriseRentRecordDetailService {
    @Resource
    private EnterpriseRentRecordDetailMapper enterpriseRentRecordDetailMapper;
    
    @Override
    public int batchInsert(List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList) {
        return enterpriseRentRecordDetailMapper.batchInsert(enterpriseRentRecordDetailList);
    }
    
    @Slave
    @Override
    public List<EnterpriseRentRecordDetail> queryListByUid(Long uid) {
        return enterpriseRentRecordDetailMapper.selectListByUid(uid);
    }
}
