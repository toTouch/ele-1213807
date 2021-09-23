package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.StoreGoods;
import com.xiliulou.electricity.mapper.StoreGoodsMapper;
import com.xiliulou.electricity.query.StoreShopsQuery;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
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
import java.util.List;
import java.util.Objects;

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

	@Override
	public R insert(StoreGoods storeGoods) {
		Store store = storeService.queryByIdFromCache(storeGoods.getStoreId());
		if (Objects.isNull(store)) {
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		storeGoods.setCreateTime(System.currentTimeMillis());
		storeGoods.setUpdateTime(System.currentTimeMillis());
		storeGoods.setDelFlag(StoreGoods.DEL_NORMAL);
		storeGoods.setTenantId(tenantId);
		return R.ok(storeGoodsMapper.insert(storeGoods));
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
		List<StoreGoods> storeGoodsList = storeGoodsMapper.queryList(storeShopsQuery);
		if (ObjectUtil.isEmpty(storeGoodsList)) {
			return R.ok(storeGoodsList);
		}

		List<StoreGoodsVO> storeGoodsVOList = new ArrayList<>();
		for (StoreGoods storeGoods : storeGoodsList) {
			StoreGoodsVO storeGoodsVO = new StoreGoodsVO();
			BeanUtils.copyProperties(storeGoods, storeGoodsVO);

			//图片显示
			List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(storeGoodsVO.getId(), ElectricityCabinetFile.TYPE_STORE_GOODS, storageConfig.getIsUseOSS());
			if (ObjectUtil.isNotEmpty(electricityCabinetFileList)) {
				List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
				for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList) {
					if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
						electricityCabinetFile.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), electricityCabinetFile.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
					}
					electricityCabinetFiles.add(electricityCabinetFile);
				}
				storeGoodsVO.setElectricityCabinetFiles(electricityCabinetFiles);
			}

			storeGoodsVOList.add(storeGoodsVO);
		}

		return R.ok(storeGoodsVOList);
	}

	@Override
	public R queryCount(StoreShopsQuery storeShopsQuery) {
		return R.ok(storeGoodsMapper.queryCount(storeShopsQuery));
	}
}
