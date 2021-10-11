package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.OldCard;
import com.xiliulou.electricity.mapper.OldCardMapper;
import com.xiliulou.electricity.service.OldCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户列表(LoginInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service
@Slf4j
public class OldCardServiceImpl extends ServiceImpl<OldCardMapper, OldCard> implements OldCardService {

	@Resource
	OldCardMapper oldCardMapper;

	@Override
	public OldCard queryByOldCard(Integer cardId) {
		return 	oldCardMapper.selectOne(new LambdaQueryWrapper<OldCard>().eq(OldCard::getOldCard,cardId));
	}
}
