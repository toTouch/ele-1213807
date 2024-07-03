package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.query.UserSourceQuery;
import com.xiliulou.electricity.vo.UserSearchVO;
import com.xiliulou.electricity.vo.UserSourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (User)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-27 11:19:50
 */
public interface UserMapper extends BaseMapper<User> {

    User selectById(@Param("uid") Long uid);

    List<User> queryListUserByCriteria(@Param("uid") Long uid, @Param("size") Long size, @Param("offset") Long offset, @Param("name") String name, @Param("phone") String phone, @Param("type") Integer type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("tenantId") Integer tenantId);

	Integer queryCount(@Param("uid") Long uid, @Param("name") String name, @Param("phone") String phone, @Param("type") Integer type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("tenantId") Integer tenantId);

    int updateUserByUid(User updateUser);

    Integer updateUserSource(User user);

    List<UserSourceVO> selectUserSourceByPage(UserSourceQuery userSourceQuery);

    Integer selectUserSourcePageCount(UserSourceQuery userSourceQuery);

    User queryByUserName(@Param("username") String username);
    
    List<UserSearchVO> search(UserInfoQuery query);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone, @Param("updateTime") Long updateTime);
    
    User checkMerchantExist(@Param("name") String name,@Param("phone") String phone,@Param("userType") Integer userType,@Param("tenantId") Integer tenantId,@Param("uid") Long uid);
    
    Integer updateMerchantUser(User updateUser);
    
    Integer removeById(@Param("uid") Long uid,@Param("updateTime") Long updateTime);
    
    Integer batchRemoveByUidList(@Param("uidList") List<Long> uidList,@Param("updateTime") long updateTime);
    
    List<User> queryListByUidList(@Param("uidList") List<Long> uidList,@Param("tenantId") Integer tenantId);
    
    User selectByUid(Long uid);
    
    List<User> selectListByPhones(@Param("phoneList") List<String> phoneList, @Param("tenantId") Integer tenantId, @Param("type") Integer type);
    
    List<UserSearchVO> selectListForSearch(UserInfoQuery query);
}
