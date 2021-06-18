package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.vo.FranchiseeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (ElectricityBatteryBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeMapper extends BaseMapper<Franchisee> {

    List<FranchiseeVO> queryList( @Param("query") FranchiseeQuery franchiseeQuery);
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Franchisee queryById(Integer id,Integer tenantId);
}
