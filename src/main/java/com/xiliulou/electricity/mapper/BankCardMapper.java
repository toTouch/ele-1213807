package com.xiliulou.electricity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.BankCard;
import com.xiliulou.electricity.query.BankCardQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 *
 * @author Hardy
 * @email ${email}
 * @date 2021-05-24 13:58:23
 */
@Mapper
public interface BankCardMapper extends BaseMapper<BankCard> {

	List<BankCard> queryList(@Param("query") BankCardQuery bankCardQuery);

	Integer queryCount(@Param("query") BankCardQuery bankCardQuery);
}
