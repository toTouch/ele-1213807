package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeBind;
import org.apache.ibatis.annotations.Delete;

/**
 * (FranchiseeBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeBindMapper extends BaseMapper<FranchiseeBind> {

    @Delete("delete  FROM t_franchisee_bind  WHERE franchisee_id = #{franchiseeId}")
    void deleteByFranchiseeId(Integer franchiseeId);
}