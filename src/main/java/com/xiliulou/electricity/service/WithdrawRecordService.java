package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.WithdrawRecord;
import com.xiliulou.electricity.query.CheckQuery;
import com.xiliulou.electricity.query.HandleWithdrawQuery;
import com.xiliulou.electricity.query.WithdrawQuery;
import com.xiliulou.electricity.query.WithdrawRecordQuery;
import com.xiliulou.security.bean.TokenUser;

import java.math.BigDecimal;

/**
 * 提现表(TPartnerWithdraw)表服务接口
 *
 * @author makejava
 * @since 2020-07-13 16:21:39
 */
public interface WithdrawRecordService {


	R withdraw(WithdrawQuery query);

	R queryList(WithdrawRecordQuery withdrawRecordQuery);

	R queryCount(WithdrawRecordQuery withdrawRecordQuery);

	R handleWithdraw(HandleWithdrawQuery handleWithdrawQuery);

	R updateStatus(String orderId, Double amount, Double handlingFee, Boolean result, String arriveTime);

	R check(CheckQuery query);

	WithdrawRecord selectById(Long oid);

	R getWithdrawCount(Long uid);
}
