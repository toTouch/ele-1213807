package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.StoreGoodsMapper;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.query.StoreShopsQuery;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.StoreGoodsService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.StoreGoodsVO;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: Miss.Li
 * @Date: 2021/9/18 17:31
 * @Description:
 */
@Service("storeGoodsService")
public class StoreGoodsServiceImpl implements StoreGoodsService {

    @Resource
    StoreGoodsMapper storeGoodsMapper;

    @Autowired
    StoreService storeService;

    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;

    @Autowired
    StorageConfig storageConfig;

    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;
    @Autowired
    ElectricityCarService electricityCarService;

    @Override
    public R insert(StoreGoods storeGoods) {
        Store store = storeService.queryByIdFromCache(storeGoods.getStoreId());
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        StoreGoods oleStoreGoods = queryByStoreIdAndCarModelId(storeGoods.getStoreId(), storeGoods.getCarModelId());
        if (Objects.nonNull(oleStoreGoods)) {
            return R.fail("100010", "已添加此型号车辆押金");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        storeGoods.setCreateTime(System.currentTimeMillis());
        storeGoods.setUpdateTime(System.currentTimeMillis());
        storeGoods.setDelFlag(StoreGoods.DEL_NORMAL);
        storeGoods.setTenantId(tenantId);
        storeGoodsMapper.insert(storeGoods);
        return R.ok(storeGoods.getId());
    }

    @Override
    public R update(StoreGoods storeGoods) {
        if (Objects.nonNull(storeGoods.getStoreId())) {
            Store store = storeService.queryByIdFromCache(storeGoods.getStoreId());
            if (Objects.isNull(store)) {
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
        }

        storeGoods.setUpdateTime(System.currentTimeMillis());
        return R.ok(storeGoodsMapper.updateById(storeGoods));
    }

    @Override
    public R delete(Long id) {
        StoreGoods storeGoods = storeGoodsMapper.selectById(id);
        if (Objects.isNull(storeGoods)) {
            R.fail("ELECTRICITY.00109", "未找到门店商品");
        }

        storeGoods.setId(id);
        storeGoods.setUpdateTime(System.currentTimeMillis());
        storeGoods.setDelFlag(StoreGoods.DEL_DEL);
        return R.ok(storeGoodsMapper.updateById(storeGoods));
    }

    @Override
    public R queryList(StoreShopsQuery storeShopsQuery) {
        List<StoreGoodsVO> storeGoodsList = storeGoodsMapper.queryList(storeShopsQuery);
        if (!DataUtil.collectionIsUsable(storeGoodsList)) {
            return R.ok(Collections.emptyList());
        }

        List<StoreGoodsVO> result = storeGoodsList.parallelStream().map(e -> {
            StoreGoodsVO storeGoodsVO = new StoreGoodsVO();
            BeanUtils.copyProperties(e, storeGoodsVO);

            ElectricityCarQuery electricityCarQuery = ElectricityCarQuery.builder()
                    .tenantId(e.getTenantId())
                    .storeId(e.getStoreId())
                    .model(e.getCarModel())
                    .status(ElectricityCar.CAR_NOT_RENT).build();
            Integer carInventory = (Integer) electricityCarService.queryCount(electricityCarQuery).getData();
            storeGoodsVO.setCarInventory(carInventory);

            //图片显示
            List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(e.getId(), ElectricityCabinetFile.TYPE_STORE_GOODS, storageConfig.getIsUseOSS());
            if (!DataUtil.collectionIsUsable(electricityCabinetFileList)) {
                return storeGoodsVO;
            }
            List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
            electricityCabinetFileList.stream().forEach(auth -> {
                if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
                    auth.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), auth.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
                    electricityCabinetFiles.add(auth);
                }
                storeGoodsVO.setElectricityCabinetFiles(electricityCabinetFiles);
            });
            return storeGoodsVO;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    @Override
    public R queryCount(StoreShopsQuery storeShopsQuery) {
        return R.ok(storeGoodsMapper.queryCount(storeShopsQuery));
    }

    @Override
    public StoreGoods queryByStoreIdAndCarModelId(Long storeId, Integer carModelId) {
        return storeGoodsMapper.selectOne(new LambdaQueryWrapper<StoreGoods>().eq(StoreGoods::getStoreId, storeId).eq(StoreGoods::getCarModelId, carModelId));
    }
}
