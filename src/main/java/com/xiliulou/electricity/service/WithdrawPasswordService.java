package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.WithdrawPassword;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
public interface WithdrawPasswordService {

	WithdrawPassword queryFromCache(Integer tenantId);

	R update(WithdrawPassword withdrawPassword);


}

