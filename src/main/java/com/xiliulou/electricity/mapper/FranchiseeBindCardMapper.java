package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeBindCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 加盟商套餐绑定表(TFranchiseeBindCard)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-16 15:12:51
 */
public interface FranchiseeBindCardMapper extends BaseMapper<FranchiseeBindCard>{


    /**
     * 查询指定行数据
     *
     */
    List<FranchiseeBindCard> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param franchiseeBindCard 实例对象
     * @return 对象列表
     */
    List<FranchiseeBindCard> queryAll(FranchiseeBindCard franchiseeBindCard);


}
