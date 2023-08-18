package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoDataEntity;
import com.xiliulou.electricity.query.UserInfoDataQuery;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.vo.HomePageUserByWeekDayVo;
import com.xiliulou.electricity.vo.UserBatteryInfoVO;
import com.xiliulou.electricity.vo.UserInfoSearchVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

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
     * 分页查询
     * @param userInfoQuery 查询条件
     * @return 用户集
     */
    List<UserInfo> page(UserInfoQuery userInfoQuery);

    /**
     * 查询总数
     * @param userInfoQuery 查询条件
     * @return 总数
     */
    Integer count(UserInfoQuery userInfoQuery);

    /**
     * 查询指定行数据
     */
    List<UserInfo> queryList( @Param("query") UserInfoQuery userInfoQuery);

    List<UserBatteryInfoVO> queryListForBatteryService(@Param("query") UserInfoQuery userInfoQuery);

    List<UserBatteryInfoVO> queryListByMemberCardExpireTime(@Param("query") UserInfoQuery userInfoQuery);

    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("tenantId")Integer tenantId);


    Integer homeOne(@Param("first") Long first, @Param("now") Long now,@Param("tenantId")Integer tenantId);


	Integer queryCount( @Param("query") UserInfoQuery userInfoQuery);

	Integer queryCountForBatteryService( @Param("query") UserInfoQuery userInfoQuery);
	Integer queryCountByMemberCardExpireTime( @Param("query") UserInfoQuery userInfoQuery);

	Integer queryAuthenticationCount( @Param("query") UserInfoQuery userInfoQuery);

	Integer queryAuthenticationUserCount(@Param("tenantId") Integer tenantId);

	List<HomePageUserByWeekDayVo> queryUserAnalysisForAuthUser(@Param("tenantId") Integer tenantId,  @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);

    List<HomePageUserByWeekDayVo> queryUserAnalysisByUserStatus(@Param("tenantId") Integer tenantId, @Param("userType") Integer userType,  @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);

    Integer updateByUid(UserInfo userInfo);

    Integer update(UserInfo userInfo);

    Integer isFranchiseeBindUser(@Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);
    
    List<UserInfo> queryByIdNumber(@Param("idNumber") String idNumber, @Param("tenantId") Integer tenantId);

    Integer verifyIdNumberExist(@Param("idNumber") String idNumber, @Param("tenantId") Integer tenantId);
    
    UserInfo queryDetailsUserInfo(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    List<UserInfoSearchVo> userInfoSearch(@Param("size") Long size, @Param("offset") Long offset,
            @Param("name") String name, @Param("tenantId") Integer tenantId);

    List<UserBatteryInfoVO> queryListByCarMemberCardExpireTime(@Param("query") UserInfoQuery userInfoQuery);

    Integer queryCountByCarMemberCardExpireTime(@Param("query") UserInfoQuery userInfoQuery);

    /**
     * 查询所有用户信息
     * @param userInfoDataQuery
     * @return
     */
    List<UserInfoDataEntity>  queryAllUserInfoDataByParam(@Param("query") UserInfoDataQuery userInfoDataQuery);

    /**
     * 统计用户信息
     * @param userInfoDataQuery
     * @return
     */
    Integer  queryAllUserInfoDataCount(@Param("query") UserInfoDataQuery userInfoDataQuery);

    /**
     * 套餐未过期用户
     * @param userInfoDataQuery
     * @return
     */
    List<UserInfoDataEntity>  queryEffectiveUserInfoDataByParam(@Param("query") UserInfoDataQuery userInfoDataQuery);

    /**
     * 套餐未过期用户总数
     * @param userInfoDataQuery
     * @return
     */
    Integer  queryEffectiveUserInfoDataCount(@Param("query") UserInfoDataQuery userInfoDataQuery);



    /**
     * 在租用户
     * @param userInfoDataQuery
     * @return
     */
    List<UserInfoDataEntity>  queryRentUserInfoDataByParam(@Param("query") UserInfoDataQuery userInfoDataQuery);

    /**
     * 在租用户总数
     * @param userInfoDataQuery
     * @return
     */
    Integer  queryRentUserInfoDataCount(@Param("query") UserInfoDataQuery userInfoDataQuery);


    /**
     * 逾期用户
     * @param userInfoDataQuery
     * @return
     */
    List<UserInfoDataEntity>  queryOverdueUserInfoDataByParam(@Param("query") UserInfoDataQuery userInfoDataQuery);

    /**
     * 逾期用户count
     * @param userInfoDataQuery
     * @return
     */
    Integer  queryOverdueUserInfoDataCount(@Param("query") UserInfoDataQuery userInfoDataQuery);


    /**
     * 静默用户
     * @param userInfoDataQuery
     * @return
     */
    List<UserInfoDataEntity> querySilentUserInfoDataByParam(@Param("query") UserInfoDataQuery userInfoDataQuery);

    /**
     * 静默用户count
     * @param userInfoDataQuery
     * @return
     */
    Integer querySilentUserInfoDataCount(@Param("query") UserInfoDataQuery userInfoDataQuery);


}
