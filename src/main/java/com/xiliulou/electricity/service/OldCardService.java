package com.xiliulou.electricity.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.electricity.entity.OldCard;

/**
 * 用户列表(LoginInfo)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface OldCardService extends IService<OldCard> {

	OldCard queryByOldCard(Integer cardId);
}
