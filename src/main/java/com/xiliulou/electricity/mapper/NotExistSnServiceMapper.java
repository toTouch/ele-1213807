package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.query.NotExistSnQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * (Faq)表数据库访问层
 *
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface NotExistSnServiceMapper extends BaseMapper<NotExistSn> {

	List<NotExistSn> queryList(NotExistSnQuery notExistSnQuery);

	Integer queryCount(NotExistSnQuery notExistSnQuery);
}
