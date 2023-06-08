package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:36
 **/
@Service
@Slf4j
public class ElectricityPayParamsServiceImpl extends ServiceImpl<ElectricityPayParamsMapper, ElectricityPayParams> implements ElectricityPayParamsService {

	@Autowired
	RedisService redisService;

	@Autowired
	WechatConfig config;
	
	@Autowired
	private WechatPaymentCertificateService wechatPaymentCertificateService;
	
	@Autowired
	private WechatWithdrawalCertificateService wechatWithdrawalCertificateService;
	
	/**
	 * 新增或修改
	 *
	 * @param electricityPayParams
	 * @return
	 */
	@Override
	public R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams) {
		Integer tenantId = TenantContextHolder.getTenantId();
		//加锁
		Boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId,
				String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
		if (!getLockerSuccess) {
			return R.failMsg("操作频繁!");
		}

		ElectricityPayParams oldElectricityPayParams1 = queryFromCache(tenantId);

		ElectricityPayParams oldElectricityPayParams2 = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, electricityPayParams.getMerchantMinProAppId()));

		if (Objects.nonNull(oldElectricityPayParams2)) {
			if (Objects.isNull(oldElectricityPayParams1) || !Objects.equals(oldElectricityPayParams1.getId(), oldElectricityPayParams2.getId())) {
				return R.failMsg("该小程序appId已被使用，请勿重复使用!");
			}
		}

		electricityPayParams.setUpdateTime(System.currentTimeMillis());
		if (Objects.isNull(oldElectricityPayParams1)) {
			electricityPayParams.setCreateTime(System.currentTimeMillis());
			electricityPayParams.setTenantId(tenantId);
			baseMapper.insert(electricityPayParams);
		} else {
			if (ObjectUtil.notEqual(oldElectricityPayParams1.getId(), electricityPayParams.getId())) {
				return R.fail("请求参数id,不合法!");
			}
			redisService.delete(CacheConstant.CACHE_PAY_PARAMS + tenantId);
			baseMapper.updateById(electricityPayParams);
		}
		redisService.delete(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId);
		return R.ok();
	}

	/**
	 * 获取支付参数
	 * valid_days
	 *
	 * @return
	 */
	@Override
	public ElectricityPayParams queryFromCache(Integer tenantId) {
		ElectricityPayParams electricityPayParams = redisService.getWithHash(CacheConstant.CACHE_PAY_PARAMS + tenantId, ElectricityPayParams.class);
		if (Objects.isNull(electricityPayParams)) {
			electricityPayParams = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getTenantId, tenantId));
			if (Objects.nonNull(electricityPayParams)) {
				redisService.saveWithHash(CacheConstant.CACHE_PAY_PARAMS + tenantId, electricityPayParams);
			}
		}
		return electricityPayParams;
	}

	@Override
	public R uploadFile(MultipartFile file, Integer type) {
		Integer tenantId = TenantContextHolder.getTenantId();
		
		//加锁
		boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId,
				String.valueOf(System.currentTimeMillis()), 3 * 1000L, true);
		if (!getLockerSuccess) {
			return R.failMsg("操作频繁!");
		}
		try {
			ElectricityPayParams oldElectricityPayParams = queryFromCache(tenantId);
			if (Objects.isNull(oldElectricityPayParams)) {
				return R.fail("找不到支付配置");
			}
			
			ElectricityPayParams electricityPayParams = new ElectricityPayParams();
			electricityPayParams.setId(oldElectricityPayParams.getId());
			electricityPayParams.setTenantId(tenantId);
			electricityPayParams.setUpdateTime(System.currentTimeMillis());
			if (Objects.isNull(type) || Objects.equals(type, ElectricityPayParams.TYPE_MERCHANT_PATH)) {
				wechatPaymentCertificateService.handleCertificateFile(file, tenantId);
			} else {
				wechatWithdrawalCertificateService.handleCertificateFile(file, tenantId);
			}
			//更新支付参数
			updateElectricityPayParams(electricityPayParams);
		}catch (Exception e) {
			log.error("certificate get error, tenantId={}", tenantId);
			return R.fail("证书内容获取失败，请重试！");
		} finally {
			//解锁
			redisService.remove(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId);
		}
		return R.ok();

	}

	@Override
	public R getTenantId(String appId) {
		ElectricityPayParams electricityPayParams = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
		if (Objects.isNull(electricityPayParams)) {
			return R.fail("ELECTRICITY.00101", "找不到租户");
		}
		return R.ok(electricityPayParams.getTenantId());
	}

	@Override
	public ElectricityPayParams selectTenantId(String appId) {
		return baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
	}

	@Override
	public ElectricityPayParams queryByTenantId(Integer tenantId) {
		return baseMapper.queryByTenantId(tenantId);
	}
	
	/**
	 * 更新支付参数
	 * @param electricityPayParams electricityPayParams
	 */
	private void updateElectricityPayParams(ElectricityPayParams electricityPayParams) {
		baseMapper.updateById(electricityPayParams);
		redisService.delete(CacheConstant.CACHE_PAY_PARAMS + electricityPayParams.getTenantId());
	}
}
