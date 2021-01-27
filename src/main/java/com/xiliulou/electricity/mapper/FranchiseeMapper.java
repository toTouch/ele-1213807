package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.FranchiseeQuery;
import org.apache.ibatis.annotations.Param;

/**
 * (ElectricityBatteryBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeMapper extends BaseMapper<Franchisee> {

    IPage queryList(Page page, @Param("query") FranchiseeQuery franchiseeQuery);
}