package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserAmount;
import com.xiliulou.electricity.query.UserAmountQuery;
import com.xiliulou.electricity.vo.UserAmountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (AgentAmount)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface UserAmountMapper extends BaseMapper<UserAmount> {

	List<UserAmountVO> queryList(UserAmountQuery userAmountQuery);

	Integer queryCount(UserAmountQuery userAmountQuery);
}
