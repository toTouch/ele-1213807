package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.DepositProtocol;

import java.util.List;

/**
 * (Faq)表数据库访问层
 *
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface DepositProtocolMapper extends BaseMapper<DepositProtocol> {
    
    Integer update(DepositProtocol depositProtocol);

    List<DepositProtocol> selectByQuery(DepositProtocol depositProtocol);

}
