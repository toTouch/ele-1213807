package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.electricity.vo.HomePageUserByWeekDayVo;
import com.xiliulou.electricity.vo.UserBatteryInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 用户列表(TUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {


    /**
     * 查询指定行数据
     */
    List<UserInfo> queryList( @Param("query") UserInfoQuery userInfoQuery);

    List<UserBatteryInfoVO> queryListForBatteryService(@Param("query") UserInfoQuery userInfoQuery);


    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("tenantId")Integer tenantId);


    Integer homeOne(@Param("first") Long first, @Param("now") Long now,@Param("tenantId")Integer tenantId);


	Integer queryCount( @Param("query") UserInfoQuery userInfoQuery);

	Integer queryCountForBatteryService( @Param("query") UserInfoQuery userInfoQuery);

	Integer queryAuthenticationCount( @Param("query") UserInfoQuery userInfoQuery);

	Integer queryAuthenticationUserCount(@Param("tenantId") Integer tenantId);

	List<HomePageUserByWeekDayVo> queryUserAnalysisForAuthUser(@Param("tenantId") Integer tenantId,  @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);

    List<HomePageUserByWeekDayVo> queryUserAnalysisByUserStatus(@Param("tenantId") Integer tenantId, @Param("userType") Integer userType,  @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);

    Integer updateByUid(UserInfo userInfo);

    Integer update(UserInfo userInfo);

    Integer isFranchiseeBindUser(@Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);
    
    List<UserInfo> queryByIdNumber(@Param("idNumber") String idNumber, @Param("tenantId") Integer tenantId);
}
