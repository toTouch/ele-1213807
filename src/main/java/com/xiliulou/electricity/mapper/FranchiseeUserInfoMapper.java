package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 用户绑定列表(FranchiseeUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
public interface FranchiseeUserInfoMapper  extends BaseMapper<FranchiseeUserInfo>{

    Integer unBind(FranchiseeUserInfo franchiseeUserInfo);

	Integer minCount(Long id);
}
