package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.CouponActivityPackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/28 10:50
 * @Description:
 */
public interface CouponActivityPackageMapper extends BaseMapper<CouponActivityPackage> {

    Integer insertOne(CouponActivityPackage couponActivityPackage);

    Integer updateOne(CouponActivityPackage couponActivityPackage);

    List<CouponActivityPackage> selectByQuery(CouponActivityPackage couponActivityPackage);
    Integer batchInsertCouponPackages(List<CouponActivityPackage> couponActivityPackages);
    Integer deleteCouponPackage(@Param("id") Long id);
    List<CouponActivityPackage> selectActivityPackagesByCouponId(@Param("couponId") Long couponId);

}
