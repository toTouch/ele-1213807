package com.xiliulou.electricity.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstants;
import com.xiliulou.electricity.entity.WithdrawPassword;
import com.xiliulou.electricity.mapper.WithdrawPasswordMapper;
import com.xiliulou.electricity.service.WithdrawPasswordService;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/10/8 13:42
 * @Description:
 */
@Slf4j
@Service
public class WithdrawPasswordServiceImpl implements WithdrawPasswordService {


	@Resource
	WithdrawPasswordMapper withdrawPasswordMapper;

	@Autowired
	RedisService redisService;


	@Value("${security.encode.key:xiliu&lo@u%12345}")
	private String encodeKey;

	@Autowired
	CustomPasswordEncoder customPasswordEncoder;


	@Override
	public WithdrawPassword queryFromCache() {
		//先查缓存
		WithdrawPassword cacheWithdrawPassword = redisService.getWithHash(CommonConstants.CACHE_WITHDRAW_PASSWORD, WithdrawPassword.class);
		if (Objects.nonNull(cacheWithdrawPassword)) {
			return cacheWithdrawPassword;
		}
		//缓存没有再查数据库
		WithdrawPassword withdrawPassword = withdrawPasswordMapper.selectOne(new LambdaQueryWrapper<>());
		if (Objects.isNull(withdrawPassword)) {
			return null;
		}
		//放入缓存
		redisService.saveWithHash(CommonConstants.CACHE_WITHDRAW_PASSWORD, withdrawPassword);
		return withdrawPassword;
	}


	@Override
	public R update(WithdrawPassword withdrawPassword) {

		String decryptPassword = null;
		String encryptPassword = withdrawPassword.getPassword();
		if (StrUtil.isNotEmpty(encryptPassword)) {
			//解密密码
			decryptPassword = decryptPassword(encryptPassword);
			if (StrUtil.isEmpty(decryptPassword)) {
				log.error("update withdraw passsword ERROR! decryptPassword error! password={}", withdrawPassword.getPassword());
				return R.fail("系统错误!");
			}
		}

		WithdrawPassword oldWithdrawPassword=queryFromCache();
		if(Objects.isNull(oldWithdrawPassword)){
			withdrawPassword.setPassword(customPasswordEncoder.encode(decryptPassword));
			withdrawPassword.setCreateTime(System.currentTimeMillis());
			withdrawPassword.setUpdateTime(System.currentTimeMillis());
			withdrawPasswordMapper.insert(withdrawPassword);
		}else {
			withdrawPassword.setId(oldWithdrawPassword.getId());
			withdrawPassword.setPassword(customPasswordEncoder.encode(decryptPassword));
			withdrawPassword.setUpdateTime(System.currentTimeMillis());
			withdrawPasswordMapper.updateById(withdrawPassword);
			redisService.delete(CommonConstants.CACHE_WITHDRAW_PASSWORD);
		}
		return R.ok();
	}


	private String decryptPassword(String encryptPassword) {
		AES aes = new AES(Mode.CBC, Padding.ZeroPadding, new SecretKeySpec(encodeKey.getBytes(), "AES"),
				new IvParameterSpec(encodeKey.getBytes()));

		return new String(aes.decrypt(Base64.decode(encryptPassword.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
	}


}
