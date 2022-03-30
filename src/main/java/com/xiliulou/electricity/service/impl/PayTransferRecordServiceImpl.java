package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.PayTransferRecord;
import com.xiliulou.electricity.mapper.PayTransferRecordMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.PayTransferRecordService;
import com.xiliulou.electricity.service.WithdrawRecordService;
import com.xiliulou.electricity.utils.BigDecimalUtil;
import com.xiliulou.pay.weixin.query.PayTransferQuery;
import com.xiliulou.pay.weixin.transferPay.QueryTransferResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/9/1 16:33
 * @Description:
 */
@Service
@Slf4j
public class PayTransferRecordServiceImpl implements PayTransferRecordService {

	@Resource
	PayTransferRecordMapper payTransferRecordMapper;

	@Autowired
	QueryTransferResultService queryTransferResultService;

	@Autowired
	WechatConfig wechatConfig;

	@Autowired
	WithdrawRecordService withdrawRecordService;
	@Autowired
	ElectricityPayParamsService electricityPayParamsService;

	@Override
	public void insert(PayTransferRecord payTransferRecord) {
		payTransferRecordMapper.insert(payTransferRecord);
	}

	@Override
	public void update(PayTransferRecord payTransferRecord) {
		payTransferRecordMapper.updateById(payTransferRecord);
	}

	@Override
	public void handlerTransferPayQuery() {
		//查询提现表，状态为提现中
		List<PayTransferRecord> payTransferRecordList = payTransferRecordMapper.handlerTransferPayQuery();
		if (ObjectUtil.isEmpty(payTransferRecordList)) {
			return;
		}
		log.info("-----payTransferRecordList>>>>>{}", payTransferRecordList);
		for (PayTransferRecord payTransferRecord : payTransferRecordList) {
			ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(payTransferRecord.getTenantId());
			if (ObjectUtil.isEmpty(electricityPayParams)){
				log.error("pay query not payParams tenantId:{}",payTransferRecord.getTenantId());
				return;
			}
			PayTransferQuery payTransferQuery = PayTransferQuery.builder()
					.mchId(electricityPayParams.getWechatMerchantId())
					.partnerOrderNo(payTransferRecord.getOrderId())
					.appId(electricityPayParams.getMerchantMinProAppId())
					.patternedKey(electricityPayParams.getPaternerKey())
					.apiName(electricityPayParams.getApiName()).build();
			Map<String, String> resultMap = queryTransferResultService.queryBankTransferResultService(payTransferQuery);
			log.info("-----resultMap>>>>>{}", resultMap);
			handlerTransferResultMap(resultMap, payTransferRecord);
		}
	}

	private void handlerTransferResultMap(Map<String, String> resultMap, PayTransferRecord payTransferRecord) {
		log.info("-----payTransferRecord>>>>>{}", payTransferRecord);
		PayTransferRecord updatePayTransferRecord = new PayTransferRecord();
		updatePayTransferRecord.setId(payTransferRecord.getId());
		updatePayTransferRecord.setUpdateTime(System.currentTimeMillis());

		//通信失败
		if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_FAIL, resultMap.get("return_code"))) {
			String errmsg = resultMap.get("return_msg");
			if (StringUtils.isEmpty(errmsg)) {
				errmsg = "未知错误!";
			}
			String errCode = resultMap.get("result_code");
			updatePayTransferRecord.setErrorCode(ObjectUtil.isEmpty(errCode) ? "未知错误码" : errCode);
			updatePayTransferRecord.setErrorMsg(errmsg);
			update(updatePayTransferRecord);

		}

		if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_SUCCESS, resultMap.get("return_code"))) {
			//业务结果  成功
			if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_SUCCESS, resultMap.get("result_code"))) {
				//转账状态
				if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_STATUS_SUCCESS, resultMap.get("status"))) {
					//真正转账成功
					updatePayTransferRecord.setChannelOrderNo(resultMap.get("detail_id"));
					updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_OK);
					updatePayTransferRecord.setActualAmount(Long.valueOf(resultMap.get("amount")));
					updatePayTransferRecord.setCommissionAmount(Long.valueOf(resultMap.get("cmms_amt")));
					update(updatePayTransferRecord);

					withdrawRecordService.updateStatus(payTransferRecord.getOrderId(),
							BigDecimalUtil.transformLongToDouble(Long.valueOf(resultMap.get("amount"))),
							BigDecimalUtil.transformLongToDouble(Long.valueOf(resultMap.get("cmms_amt"))),
							true, resultMap.get("pay_succ_time"));

				}
				//真正转账失败
				else if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_STATUS_FAIL, resultMap.get("status")) ||
						ObjectUtil.equal("BANK_FAIL", resultMap.get("status"))) {
					updatePayTransferRecord.setErrorCode("...");
					String errmsg = ObjectUtil.isEmpty(resultMap.get("reason"))
							? "转账失败,未知原因" : resultMap.get("reason");
					updatePayTransferRecord.setErrorMsg(errmsg);
					updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_FAILED);
					update(updatePayTransferRecord);

					//回调
					withdrawRecordService.updateStatus(payTransferRecord.getOrderId(), null,
							null, false, "");

				} else {
					//处理中,或其他
					return;
				}

			}
			if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_FAIL, resultMap.get("result_code"))) {
				String errmsg = resultMap.get("err_code_des");
				if (StringUtils.isEmpty(errmsg)) {
					errmsg = "未知错误!";
				}
				String errCode = resultMap.get("err_code");
				updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_FAILED);
				updatePayTransferRecord.setErrorCode(ObjectUtil.isEmpty(errCode) ? "未知错误码" : errCode);
				updatePayTransferRecord.setErrorMsg(errmsg);
				update(updatePayTransferRecord);

			}
		}
	}
}


