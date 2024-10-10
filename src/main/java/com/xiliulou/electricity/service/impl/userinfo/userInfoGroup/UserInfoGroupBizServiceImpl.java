package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userinfo.userInfoGroup.UserInfoGroupDetailMapper;
import com.xiliulou.electricity.mapper.userinfo.userInfoGroup.UserInfoGroupMapper;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupBizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/8 14:39
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoGroupBizServiceImpl implements UserInfoGroupBizService {
    
    private final UserInfoGroupDetailMapper userInfoGroupDetailMapper;
    
    private final UserInfoGroupMapper userInfoGroupMapper;
    
    private final RedisService redisService;
    
    @Override
    public Integer deleteGroupDetailByUid(Long uid, List<String> groupNoList) {
        return userInfoGroupDetailMapper.deleteByUid(uid, groupNoList);
    }
    
    @Override
    public Integer deleteGroupDetailByGroupNo(String groupNo, Integer tenantId) {
        return userInfoGroupDetailMapper.deleteByGroupNo(groupNo, tenantId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesBO> listGroupByUidList(List<Long> uidList) {
        return userInfoGroupDetailMapper.selectListGroupByUidList(uidList);
    }
    
    @Override
    public Integer batchInsertGroupDetail(List<UserInfoGroupDetail> detailList) {
        return userInfoGroupDetailMapper.batchInsert(detailList);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupBO> listUserInfoGroupByIds(List<Long> ids) {
        return userInfoGroupMapper.selectListByIds(ids);
    }
    
    @Override
    public UserInfoGroup queryUserInfoGroupByIdFromCache(Long id) {
        // 先查缓存
        UserInfoGroup cacheUserInfoGroup = redisService.getWithHash(CacheConstant.CACHE_USER_GROUP + id, UserInfoGroup.class);
        if (Objects.nonNull(cacheUserInfoGroup)) {
            return cacheUserInfoGroup;
        }
        // 缓存没有再查数据库
        UserInfoGroup userInfoGroup = userInfoGroupMapper.selectById(id);
        if (Objects.isNull(userInfoGroup)) {
            return null;
        }
        
        // 放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_USER_GROUP + id, userInfoGroup);
        return userInfoGroup;
    }
}
