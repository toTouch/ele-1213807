package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜保险订单(InsuranceOrder)表数据库访问层
 *
 * @author makejava
 * @since 2022-11-03 14:44:12
 */
public interface InsuranceOrderMapper extends BaseMapper<InsuranceOrder> {

    List<InsuranceOrderVO> queryList(@Param("query") InsuranceOrderQuery insuranceOrderQuery);

    Integer queryCount(@Param("query") InsuranceOrderQuery insuranceOrderQuery);
}
