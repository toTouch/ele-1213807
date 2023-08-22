package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoCarAddAndUpdate;
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

    R queryCarRentalList(UserInfoQuery userInfoQuery);

    R queryCarRentalCount(UserInfoQuery userInfoQuery);

    R updateStatus(Long uid, Integer usableStatus);

    UserInfo queryByUidFromCache(Long uid);
    
    UserInfo queryByUidFromDb(Long uid);

    Integer homeOne(Long first, Long now, Integer tenantId);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, Integer tenantId);

    R getMemberCardInfo(Long uid);

    R getRentCarMemberCardInfo(Long uid);

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
    
    Triple<Boolean, String, Object> updateRentBatteryStatus(Long uid, Integer rentStatus);

    Triple<Boolean, String, Object> updateRentCarStatus(Long uid, Integer carRentStatus);
    
    int selectCountByFranchiseeId(Long id);

    Triple<Boolean, String, Object> selectUserInfoStatus();

    void unBindUserFranchiseeId(Long uid);

    Integer isFranchiseeBindUser(Long id, Integer tenantId);
    
    List<UserInfo> queryByIdNumber(String value);

    Integer verifyIdNumberExist(String idNumber, Integer tenantId);
    
    R queryDetailsBasicInfo(Long uid);
    
    R queryDetailsBatteryInfo(Long uid);
    
    R queryDetailsCarInfo(Long uid);
    
    R webBindCar(UserInfoCarAddAndUpdate userInfoCarAddAndUpdate);
    
    R webUnBindCar(Long uid);
    
    R userInfoSearch(Long size, Long offset, String name);

    R queryEleList(UserInfoQuery userInfoQuery);
    R queryEleListCount(UserInfoQuery userInfoQuery);
}
