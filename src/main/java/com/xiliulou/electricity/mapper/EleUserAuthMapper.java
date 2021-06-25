package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleUserAuth;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 实名认证信息(TEleUserAuth)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
public interface EleUserAuthMapper extends BaseMapper<EleUserAuth>{




    void updateByUid(@Param("uid") Long uid, @Param("authStatus") Integer authStatus,@Param("updateTime") long updateTime);
}
