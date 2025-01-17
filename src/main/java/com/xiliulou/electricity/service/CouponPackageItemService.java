package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.CouponPackageItemBO;
import com.xiliulou.electricity.entity.CouponPackageItem;

import java.util.List;

/**
 * @Description: CouponPackageService
 * @Author: renhang
 * @Date: 2025/01/16
 */

public interface CouponPackageItemService {

    /**
     * 是否存在优惠券绑定的优惠券包
     *
     * @param couponId couponId
     * @return: Integer
     */

    Integer existsCouponBindPackage(Long couponId);

    /**
     * 批量插入
     *
     * @param itemList CouponPackageItem
     */

    void batchSavePackItem(List<CouponPackageItem> itemList);

    /**
     * 删除优惠券包下的优惠券
     *
     * @param packageId 优惠券包id
     * @return:
     */

    void deletePackItemByPackageId(Long packageId);


    /**
     * 获取优惠券包下的优惠券
     *
     * @param packageId packageId
     * @return: @return {@link List }<{@link CouponPackageItem }>
     */

    List<CouponPackageItem> listCouponPackageItemByPackageId(Long packageId);


    /**
     * 获取优惠券包下的优惠券
     *
     * @param packageIds packageIdList
     * @return: @return {@link List }<{@link CouponPackageItemBO }>
     */

    List<CouponPackageItemBO> listCouponPackageItemByPackageIds(List<Long> packageIds);
}
