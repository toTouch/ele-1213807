package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.WithdrawPassword;

/**
 * @author: Miss.Li
 * @Date: 2021/10/8 13:43
 * @Description:
 */
public interface WithdrawPasswordMapper extends BaseMapper<WithdrawPassword> {

    int updateByIdAndTenantId(WithdrawPassword withdrawPassword);
}
