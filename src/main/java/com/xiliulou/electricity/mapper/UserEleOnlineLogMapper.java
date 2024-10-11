package com.xiliulou.electricity.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserEleOnlineLog;
import com.xiliulou.electricity.vo.ELeOnlineLogVO;



public interface UserEleOnlineLogMapper extends BaseMapper<UserEleOnlineLog>{

    /**
     * 插入用户电力在线日志记录
     * 
     * @param userEleOnlineLog 用户电力在线日志实体
     * @return 插入成功的记录数
     */
    int insert(UserEleOnlineLog userEleOnlineLog);

    
    @Select("SELECT eid, status FROM t_user_ele_online_log WHERE eid = #{id} ORDER BY id DESC LIMIT 1")
    UserEleOnlineLog queryLastLog(Integer id);


    List<UserEleOnlineLog> queryOnlineLogList(@Param("size") Integer size, @Param("offset") Integer offset,
            @Param("type") String type, @Param("eleId") Integer eleId, @Param("tenantId") Integer tenantId);


    Integer queryOnlineLogCount(@Param("type") String type, @Param("eleId") Integer eleId);


}