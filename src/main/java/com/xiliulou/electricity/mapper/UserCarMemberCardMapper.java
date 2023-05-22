package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserCarMemberCard;

import java.util.List;

import com.xiliulou.electricity.query.CarMemberCardExpireBreakPowerQuery;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserCarMemberCard)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-07 17:36:11
 */
public interface UserCarMemberCardMapper extends BaseMapper<UserCarMemberCard> {

    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCarMemberCard selectByUid(@Param("uid") Long uid);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param userCarMemberCard 实例对象
     * @return 对象列表
     */
    List<UserCarMemberCard> selectByQuery(UserCarMemberCard userCarMemberCard);

    /**
     * 新增数据
     *
     * @param userCarMemberCard 实例对象
     * @return 影响行数
     */
    int insertOne(UserCarMemberCard userCarMemberCard);

    /**
     * 修改数据
     *
     * @param userCarMemberCard 实例对象
     * @return 影响行数
     */
    int updateByUid(UserCarMemberCard userCarMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(@Param("uid") Long uid);

    int insertOrUpdate(UserCarMemberCard userCarMemberCard);

    List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(@Param("offset") Integer offset, @Param("size") Integer size,@Param("firstTime") Long firstTime,@Param("lastTime") Long lastTime);

    List<FailureMemberCardVo > queryMemberCardExpireUser(@Param("offset") Integer offset, @Param("size") Integer size, @Param("nowTime") Long nowTime);
    
    List<CarMemberCardExpiringSoonQuery> selectCarMemberCardExpire(@Param("offset") Integer offset, @Param("size") Integer size, @Param("firstTime") Long firstTime, @Param("lastTime") Long lastTime);
    
    List<CarMemberCardExpireBreakPowerQuery> carMemberCardExpireBreakPower(@Param("offset") Integer offset,
            @Param("size") Integer size, @Param("now") Long now);
}
