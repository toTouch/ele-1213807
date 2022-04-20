package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 缴纳押金订单表(EleBatteryServiceFeeOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleBatteryServiceFeeOrderMapper extends BaseMapper<EleBatteryServiceFeeOrder>{


}
