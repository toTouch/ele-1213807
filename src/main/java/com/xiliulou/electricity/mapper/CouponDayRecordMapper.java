package com.xiliulou.electricity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.CouponDayRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Description: This interface is CouponDayRecordMapper!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/14
 **/
@Mapper
public interface CouponDayRecordMapper extends BaseMapper<CouponDayRecordEntity> {
    
    Integer queryDaysByUidAndPackageOrderNo(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("orderNo")  String orderNo, @Param("scope") Integer scope);
    
    void cleanDaysByUidAndPackageOrderNo(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("orderNo") String orderNo, @Param("scope") Integer scope);
    
}
