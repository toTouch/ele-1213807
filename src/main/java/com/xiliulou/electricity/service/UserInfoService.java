package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserMoveHistory;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.vo.HomePageUserByWeekDayVo;
import com.xiliulou.electricity.vo.UserInfoDetailVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

/**
 * 用户列表(TUserInfo)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserInfo queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Deprecated
    UserInfo selectUserByUid(Long id);

    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    UserInfo insert(UserInfo userInfo);

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    Integer update(UserInfo userInfo);

    R queryList(UserInfoQuery userInfoQuery);

    R updateStatus(Long uid, Integer usableStatus);

    UserInfo queryByUidFromCache(Long uid);

    Integer homeOne(Long first, Long now, Integer tenantId);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, Integer tenantId);

    R getMemberCardInfo(Long uid);

    R getRentCarMemberCardInfo(Long uid);

    @Deprecated
    R queryUserInfo();

    R verifyAuth(Long id, Integer authStatus);

    R updateAuth(UserInfo userInfo);

    R queryUserAuthInfo(UserInfoQuery userInfoQuery);

    R queryCount(UserInfoQuery userInfoQuery);

    R queryAuthenticationCount(UserInfoQuery userInfoQuery);

    Integer querySumCount(UserInfoQuery userInfoQuery);

    R webBindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate);

    R webUnBindBattery(Long uid);

    Integer deleteByUid(Long uid);

    R queryUserBelongFranchisee(Long franchiseeId,Integer tenantId);

    R queryUserAllConsumption(Long id);

    UserInfo queryUserInfoByPhone(String phone, Integer tenantId);

    Integer queryAuthenticationUserCount(Integer tenantId);

    List<HomePageUserByWeekDayVo>  queryUserAnalysisForAuthUser(Integer tenantId, Long beginTime, Long endTime);

    List<HomePageUserByWeekDayVo> queryUserAnalysisByUserStatus(Integer tenantId, Integer userStatus, Long beginTime, Long endTime);

    UserInfoDetailVO selectUserInfoDetail();
    
    void exportExcel(UserInfoQuery userInfoQuery, HttpServletResponse response);

    R deleteUserInfo(Long uid);

    Integer updateByUid(UserInfo userInfo);
    
    Triple<Boolean, String, Object> updateRentStatus(Long uid, Integer rentStatus);
    
    int selectCountByFranchiseeId(Long id);

    Triple<Boolean, String, Object> selectUserInfoDetailV2();
}
