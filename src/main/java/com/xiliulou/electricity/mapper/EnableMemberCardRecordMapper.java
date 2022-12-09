package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.EnableMemberCardRecordQuery;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
public interface EnableMemberCardRecordMapper extends BaseMapper<EnableMemberCardRecord> {


    List<EnableMemberCardRecord> queryList(EnableMemberCardRecordQuery enableMemberCardRecordQuery);

    Integer queryCount(EnableMemberCardRecordQuery enableMemberCardRecordQuery);

}
