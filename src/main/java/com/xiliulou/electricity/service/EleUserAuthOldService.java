package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleUserAuthOld;

import java.util.List;

/**
 * 实名认证信息(TEleUserAuth)表服务接口
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
public interface EleUserAuthOldService {

	List<EleUserAuthOld> queryByUid(Long uid);
}
