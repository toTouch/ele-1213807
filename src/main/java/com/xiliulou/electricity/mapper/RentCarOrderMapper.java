package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.RentCarOrder;

import java.util.List;

import com.xiliulou.electricity.query.RentCarOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租车记录(TRentCarOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
public interface RentCarOrderMapper extends BaseMapper<RentCarOrder> {

    /**
     * 查询指定行数据
     */
    IPage queryList(Page page, @Param("query") RentCarOrderQuery rentCarOrderQuery);


}