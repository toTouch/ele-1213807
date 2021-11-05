package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.concurrent.AsyncExeUtils;
import com.xiliulou.electricity.entity.TenantCallingApiStats;
import com.xiliulou.electricity.entity.ThirdAccessRecord;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.handler.eleapi.impl.EleApiHandlerManager;
import com.xiliulou.electricity.query.ApiRequestQuery;
import com.xiliulou.electricity.service.ApiService;
import com.xiliulou.electricity.service.TenantCallingApiStatsService;
import com.xiliulou.electricity.service.ThirdAccessRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:21
 * @Description:
 */
@Service
@Slf4j
public class EleApiServiceImpl implements ApiService {

	@Autowired
	EleApiHandlerManager eleApiHandlerManager;

	@Autowired
	ThirdAccessRecordService thirdAccessRecordService;

	@Autowired
	TenantCallingApiStatsService tenantCallingApiStatsService;


	@Override
	public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {
		EleApiHandler eleApiHandler = eleApiHandlerManager.getInstance(apiRequestQuery.getCommand());
		if (Objects.isNull(eleApiHandler)) {
			return Triple.of(false, "API.00001", "api命令不存在！");
		}

		Triple<Boolean, String, Object> handleResult = eleApiHandler.handleCommand(apiRequestQuery);

		ThirdAccessRecord thirdAccessRecord = ThirdAccessRecord
				.builder()
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis())
				.operateType(apiRequestQuery.getCommand())
				.requestId(apiRequestQuery.getRequestId())
				.requestTime(System.currentTimeMillis())
				.tenantId(TenantContextHolder.getTenantId())
				.attrMsg(apiRequestQuery.getData())
				.build();

		TenantCallingApiStats apiStats = TenantCallingApiStats.builder()
				.tenantId(TenantContextHolder.getTenantId())
				.createTime(System.currentTimeMillis())
				.apiCallStats(1L)
				.updateTime(System.currentTimeMillis())
				.build();
		AsyncExeUtils.asyncExecTx(() -> thirdAccessRecordService.insert(thirdAccessRecord)).exceptionally(e -> {
			log.error("CUPBOARD API ERROR! save thirdAccessRecordError! appId={}", apiRequestQuery.getAppId(), e);
			return null;
		});

		AsyncExeUtils.asyncExecTx(() -> tenantCallingApiStatsService.insert(apiStats)).exceptionally(e -> {
			log.error("CUPBOARD API ERROR! save apiStats! appId={}", apiRequestQuery.getAppId(), e);
			return null;
		});

		return handleResult;
	}
}
