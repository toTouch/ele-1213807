package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.EleUserAuthOld;
import com.xiliulou.electricity.mapper.EleUserAuthOldMapper;
import com.xiliulou.electricity.service.EleUserAuthOldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 实名认证信息(TEleUserAuth)表服务实现类
 *
 * @author makejava
 * @since 2021-02-20 13:37:
 */
@Service("eleUserAuthOldService")
@Slf4j
public class EleUserAuthOldServiceImpl implements EleUserAuthOldService {

	@Resource
	EleUserAuthOldMapper eleUserAuthOldMapper;

	@Override
	public List<EleUserAuthOld> queryByUid(Long uid) {
		return eleUserAuthOldMapper.selectList(new LambdaQueryWrapper<EleUserAuthOld>().eq(EleUserAuthOld::getUid, uid)
				.eq(EleUserAuthOld::getDelFlag, EleUserAuthOld.DEL_NORMAL));
	}
}
