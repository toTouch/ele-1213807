package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.bo.CouponPackageItemBO;
import com.xiliulou.electricity.entity.CouponPackageItem;

import java.util.List;

/**
 * @Description: CouponPackageMapper
 * @Author: renhang
 * @Date: 2025/01/16
 */

public interface CouponPackageItemMapper {

    Integer existsCouponBindPackage(Long couponId);

    void savePackItemBatch(List<CouponPackageItem> itemList);

    void deletePackItemByPackageId(Long packageId);

    List<CouponPackageItem> selectListCouponPackageItemByPackageId(Long packageId);

    List<CouponPackageItemBO> selectListCouponPackageItemByPackageIds(List<Long> packageIds);
}
