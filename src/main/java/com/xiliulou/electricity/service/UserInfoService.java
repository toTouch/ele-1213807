package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.user.UserInfoBO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.request.user.BindBatteryRequest;
import com.xiliulou.electricity.request.user.UnbindOpenIdRequest;
import com.xiliulou.electricity.request.user.UpdateUserPhoneRequest;
import com.xiliulou.electricity.vo.HomePageUserByWeekDayVo;
import com.xiliulou.electricity.vo.userinfo.UserAccountInfoVO;
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
     *
     * @param userInfoQuery 查询条件
     * @return 用户集
     */
    List<UserInfo> page(UserInfoQuery userInfoQuery);
    
    List<UserInfoBO> pageV2(UserInfoQuery userInfoQuery);
    
    /**
     * 查询总数
     *
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
    
    R queryCarRentalListForPro(UserInfoQuery userInfoQuery);
    
    R queryCarRentalCount(UserInfoQuery userInfoQuery);
    
    R updateStatus(Long uid, Integer usableStatus);
    
    UserInfo queryByUidFromCache(Long uid);
    
    UserInfo queryByUidFromDbIncludeDelUser(Long uid);
    
    Integer homeOne(Long first, Long now, Integer tenantId);
    
    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, Integer tenantId);
    
    R getMemberCardInfo(Long uid);
    
    R verifyAuth(Long id, Integer authStatus, String msg);
    
    R updateAuth(UserInfo userInfo);
    
    R queryUserAuthInfo(UserInfoQuery userInfoQuery);
    
    R queryCount(UserInfoQuery userInfoQuery);
    
    R queryAuthenticationCount(UserInfoQuery userInfoQuery);
    
    Integer querySumCount(UserInfoQuery userInfoQuery);
    
    R webBindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate);
    
    R webUnBindBattery(Long uid);
    
    Integer deleteByUid(Long uid);
    
    R queryUserBelongFranchisee(Long franchiseeId, Integer tenantId);
    
    UserInfo queryUserInfoByPhone(String phone, Integer tenantId);
    
    UserInfo queryUserByPhoneAndFranchisee(String phone, Integer franchiseeId, Integer tenantId);
    
    Integer queryAuthenticationUserCount(Integer tenantId);
    
    List<HomePageUserByWeekDayVo> queryUserAnalysisForAuthUser(Integer tenantId, Long beginTime, Long endTime);
    
    List<HomePageUserByWeekDayVo> queryUserAnalysisByUserStatus(Integer tenantId, Integer userStatus, Long beginTime, Long endTime);
    
    void exportExcel(UserInfoQuery userInfoQuery, HttpServletResponse response);
    
    void exportCarRentalExcel(UserInfoQuery userInfoQuery, HttpServletResponse response);
    
    R deleteUserInfo(Long uid);
    
    Integer updateByUid(UserInfo userInfo);
    
    Triple<Boolean, String, Object> updateRentBatteryStatus(Long uid, Integer rentStatus);
    
    int selectCountByFranchiseeId(Long id);
    
    Triple<Boolean, String, Object> selectUserInfoStatus();
    
    Triple<Boolean, String, Object> selectUserInfoStatusV2();
    
    void unBindUserFranchiseeId(Long uid);
    
    Integer isFranchiseeBindUser(Long id, Integer tenantId);
    
    List<UserInfo> queryByIdNumber(String value);
    
    Integer existsByIdNumber(String idNumber, Integer tenantId);
    
    R queryDetailsBasicInfo(Long uid);
    
    R unbindOpenId(UnbindOpenIdRequest unbindOpenIdRequest);
    
    R updateUserPhone(UpdateUserPhoneRequest updateUserPhoneRequest);
    
    R queryDetailsBatteryInfo(Long uid);
    
    R userInfoSearch(Long size, Long offset, String name, String keyWords);
    
    R queryEleList(UserInfoQuery userInfoQuery);
    
    R queryEleListForPro(UserInfoQuery userInfoQuery);
    
    R queryEleListCount(UserInfoQuery userInfoQuery);
    
    void deleteCache(Long uid);
    
    List<UserInfo> listByUidList(List<Long> uidList);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    /**
     * 查询用户的邀请人名称
     *
     * @param uid
     * @return
     */
    String queryFinalInviterUserName(Long uid);
    
    /**
     * <p>
     * Description: 强制用户下线
     * </p>
     *
     * @param userOauthBinds userOauthBinds
     */
    void clearUserOauthBindToken(List<UserOauthBind> userOauthBinds, String clientId);
    
    List<UserInfo> listByUids(List<Long> uidList, Integer tenantId);
    
    List<UserInfo> queryListUserInfoByPhone(String phone);
    
    UserAccountInfoVO selectAccountInfo();
    
    /**
     * 检查用户与换电套餐的分组是否匹配
     * @param userInfo 用户信息
     * @param batteryMemberCard 套餐信息
     * @return 校验结果
     */
    Triple<Boolean, String, String> checkMemberCardGroup(UserInfo userInfo, BatteryMemberCard batteryMemberCard);
    
    R bindBattery(BindBatteryRequest bindBatteryRequest);
    
    Integer updatePayCountByUid(UserInfo userInfo);

    UserInfo queryByUidFromDB(Long uid);
    
    Long queryDelUidByIdNumber(String idNumber, Integer tenantId);
    
    R deleteAccountPreCheck();
    
    R deleteAccount();
    
    /**
     * 0-正常,1-已删除, 2-已注销
     */
    Integer queryUserDelStatus(Long uid);
}
