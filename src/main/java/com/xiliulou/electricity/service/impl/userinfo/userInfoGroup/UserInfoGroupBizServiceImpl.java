package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userinfo.userInfoGroup.UserInfoGroupDetailMapper;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupBizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
