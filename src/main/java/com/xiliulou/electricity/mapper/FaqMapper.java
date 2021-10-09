package com.xiliulou.electricity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Faq;
import com.xiliulou.electricity.service.FaqService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Faq)表数据库访问层
 *
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface FaqMapper extends BaseMapper<Faq> {


    /**
     * 通过主键删除数据
     *
     * @return 影响行数
     */

    List<FaqService> queryList(@Param("size") Integer size, @Param("offset") Integer offset);

    int deleteById(@Param("id") Integer id);

    Integer queryCount();
}
