package com.xiliulou.electricity.service.impl;


import com.xiliulou.electricity.entity.CouponPackageItem;
import com.xiliulou.electricity.mapper.CouponPackageItemMapper;
import com.xiliulou.electricity.service.CouponPackageItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : renhang
 * @description CouponPackageItemServiceImpl
 * @date : 2025-01-16 15:03
 **/
@Service
public class CouponPackageItemServiceImpl implements CouponPackageItemService {

    @Resource
    private CouponPackageItemMapper couponPackageItemMapper;

    @Override
    public Integer existsCouponBindPackage(Long couponId) {
        return couponPackageItemMapper.existsCouponBindPackage(couponId);
    }

    @Override
    public void batchSavePackItem(List<CouponPackageItem> itemList) {
        couponPackageItemMapper.savePackItemBatch(itemList);
    }

    @Override
    public void deletePackItemByPackageId(Long packageId) {
        couponPackageItemMapper.deletePackItemByPackageId(packageId);
    }
}
