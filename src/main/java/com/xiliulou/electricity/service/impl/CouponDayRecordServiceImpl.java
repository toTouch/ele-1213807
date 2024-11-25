package com.xiliulou.electricity.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.CouponDayRecordEntity;
import com.xiliulou.electricity.mapper.CouponDayRecordMapper;
import com.xiliulou.electricity.service.CouponDayRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 * Description: This class is CouponDayRecordServiceImpl!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/14
 **/
@Slf4j
@Service
public class CouponDayRecordServiceImpl extends ServiceImpl<CouponDayRecordMapper, CouponDayRecordEntity> implements CouponDayRecordService {
    
    @Override
    public Integer queryDaysByUidAndPackageOrderNo(Integer tenantId, Long uid, String orderNo, Integer scope) {
        
        Integer count = this.baseMapper.queryDaysByUidAndPackageOrderNo(tenantId, uid, orderNo , scope);
        if (Objects.nonNull(count)){
            return count;
        }
        return 0;
    }
    
    @Override
    public void cleanDaysByUidAndPackageOrderNo(Integer tenantId, Long uid, String orderNo, Integer scope) {
        this.baseMapper.cleanDaysByUidAndPackageOrderNo(tenantId, uid, orderNo , scope);
    }
}
