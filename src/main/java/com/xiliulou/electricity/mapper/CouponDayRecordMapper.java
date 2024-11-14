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
    
    Integer queryDaysByUidAndPackageId(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("packageId") Long packageId, @Param("scope") Integer scope);
    
    void cleanDaysByUidAndPackageId(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("packageId") Long packageId, @Param("scope") Integer scope);
    
}
