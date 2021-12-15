package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.PayTransferRecord;

/**
 * @author: Miss.Li
 * @Date: 2021/9/1 16:32
 * @Description:
 */
public interface PayTransferRecordService {

	void insert(PayTransferRecord payTransferRecord);

	void update(PayTransferRecord payTransferRecord);

	void handlerTransferPayQuery();
}
