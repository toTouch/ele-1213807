package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.PayTransferRecord;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/9/1 16:34
 * @Description:
 */
public interface PayTransferRecordMapper extends BaseMapper<PayTransferRecord> {

	List<PayTransferRecord> handlerTransferPayQuery();
}
