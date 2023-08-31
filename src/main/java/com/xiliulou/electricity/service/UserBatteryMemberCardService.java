package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.BatteryMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * (UserBatteryMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2022-12-06 13:38:52
 */
public interface UserBatteryMemberCardService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryMemberCard selectByUidFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryMemberCard selectByUidFromCache(Long uid);
    
    /**
     * 新增数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    UserBatteryMemberCard insert(UserBatteryMemberCard userBatteryMemberCard);

    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserBatteryMemberCard userBatteryMemberCard);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long id);
    
    /**
     * 解绑用户套餐信息
     * @param uid
     * @return
     */
    Integer unbindMembercardInfoByUid(Long uid);

    /**
     * 用户套餐减次数
     * @param
     * @return
     */
    Integer minCount(UserBatteryMemberCard userBatteryMemberCard);

    /**
     * 离线换电扣除套餐
     * @param
     * @return
     */
    Integer minCountForOffLineEle(UserBatteryMemberCard userBatteryMemberCard);

    Integer plusCount(Long id);

    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    Integer updateByUidForDisableCard(UserBatteryMemberCard userBatteryMemberCard);

    List<UserBatteryMemberCard> selectByMemberCardId(Integer id, Integer tenantId);

    List<BatteryMemberCardExpiringSoonQuery> batteryMemberCardExpire(Integer offset, Integer size, Long firstTime,
                                                                     Long lastTime);

    List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(Integer offset, Integer size, Long firstTime,
                                                             Long lastTime);

    List<FailureMemberCardVo> queryMemberCardExpireUser(Integer offset, Integer size, Long nowTime);

    Integer checkUserByMembercardId(Long id);

    Integer deductionExpireTime(Long uid, Long time,Long updateTime);

    List<UserBatteryMemberCard> selectList(int offset, int size);

    List<String> selectUserBatteryMemberCardOrder(Long uid);

    List<UserBatteryMemberCard> selectUseableList(int offset, int size);

    Boolean verifyUserBatteryMembercardEffective(BatteryMemberCard batteryMemberCard, UserBatteryMemberCard userBatteryMemberCard);

    void batteryMembercardExpireUpdateStatusTask();

    Long transforRemainingTime(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard);

}
