package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.WithdrawRecord;
import com.xiliulou.electricity.query.WithdrawRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
public interface WithdrawRecordMapper extends BaseMapper<WithdrawRecord> {

	List<WithdrawRecord> queryList(@Param("query") WithdrawRecordQuery withdrawRecordQuery);

	Integer queryCount(@Param("query") WithdrawRecordQuery withdrawRecordQuery);
}
