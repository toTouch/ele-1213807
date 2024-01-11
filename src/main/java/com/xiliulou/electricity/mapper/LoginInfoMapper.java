package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.LoginInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 用户列表(LoginInfo)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface LoginInfoMapper extends BaseMapper<LoginInfo>{
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
}