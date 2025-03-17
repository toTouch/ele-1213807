package com.xiliulou.electricity.service.impl.userinfo;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.UserInfoGroupConstant;
import com.xiliulou.electricity.dto.UserDelStatusDTO;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserDelRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.lostuser.LostUserFirstEnum;
import com.xiliulou.electricity.mapper.userinfo.UserDelRecordMapper;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @date 2025/1/10 15:46:03
 */
@Slf4j
@Service
public class UserDelRecordServiceImpl implements UserDelRecordService {
    
    private final TtlXllThreadPoolExecutorServiceWrapper executorWrapper = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("USER-DEL-RECOVER-THREAD-POOL", 4, "userDelRecoverThread:"));
    
    @Resource
    private UserDelRecordMapper userDelRecordMapper;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private UserInfoGroupDetailHistoryService userInfoGroupDetailHistoryService;
    
    @Resource
    private UserInfoGroupService userInfoGroupService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Slave
    @Override
    public Boolean existsByDelPhone(String phone, Integer tenantId) {
        return Objects.nonNull(userDelRecordMapper.existsByDelPhone(phone, tenantId));
    }
    
    @Slave
    @Override
    public Boolean existsByDelIdNumber(String idNumber, Integer tenantId) {
        return Objects.nonNull(userDelRecordMapper.existsByDelIdNumber(idNumber, tenantId));
    }
    
    @Slave
    @Override
    public Boolean existsByDelPhoneAndDelIdNumber(String phone, String idNumber, Integer tenantId) {
        return Objects.nonNull(userDelRecordMapper.existsByDelPhoneAndDelIdNumber(phone, idNumber, tenantId));
    }
    
    @Override
    public UserDelRecord getRemarkPhoneAndIdNumber(UserInfo userInfo, Integer tenantId) {
        String phone = userInfo.getPhone();
        String idNumber = userInfo.getIdNumber();
        String delPhone = StringUtils.EMPTY;
        String delIdNumber = StringUtils.EMPTY;
        
        //如果本次删除/注销时payCount>0，则需将本次打删除标记
        if (userInfo.getPayCount() > 0) {
            delPhone = phone;
            delIdNumber = idNumber;
        } else {
            // 本次注销时payCount=0，则需判断曾经是否打过删除标记，如果曾经打过，则需将本次打删除标记
            if (this.existsByDelPhone(phone, tenantId)) {
                delPhone = phone;
            }
            if (this.existsByDelIdNumber(idNumber, tenantId)) {
                delIdNumber = idNumber;
            }
        }
        
        return UserDelRecord.builder().delPhone(delPhone).delIdNumber(delIdNumber).build();
    }
    
    @Slave
    @Override
    public UserDelRecord queryByUidAndStatus(Long uid, List<Integer> statusList) {
        return userDelRecordMapper.selectByUidAndStatus(uid, statusList);
    }
    
    @Override
    public Integer insert(Long uid, String delPhone, String delIdNumber, Integer status, Integer tenantId, Long franchiseeId, Integer delayDay, Long userLastPayTime) {
        UserDelRecord userDelRecord = UserDelRecord.builder().uid(uid).delPhone(Objects.isNull(delPhone) ? StringUtils.EMPTY : delPhone)
                .delIdNumber(Objects.isNull(delIdNumber) ? StringUtils.EMPTY : delIdNumber).delTime(System.currentTimeMillis()).delayDay(delayDay).status(status)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).franchiseeId(franchiseeId).tenantId(tenantId).purchaseTime(userLastPayTime).build();
    
        return userDelRecordMapper.insert(userDelRecord);
    }
    
    @Override
    public void asyncRecoverUserInfoGroup(Long uid) {
        log.info("asyncRecoverUserInfoGroup after auth, uid={}", uid);
        
        executorWrapper.execute(() -> {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("asyncRecoverUserInfoGroup after auth, userInfo is null, uid={}", uid);
                return;
            }
    
            //未实名认证
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("asyncRecoverUserInfoGroup after auth, user not auth, uid={}", uid);
                return;
            }
        
            Integer tenantId = userInfo.getTenantId();
            String phone = userInfo.getPhone();
            String idNumber = userInfo.getIdNumber();
            if (StringUtils.isBlank(phone) && StringUtils.isBlank(idNumber)) {
                log.warn("asyncRecoverUserInfoGroup after auth, phone and idNumber is blank, uid={}", uid);
                return;
            }
        
            // 判断用户是否曾被删除/注销过
            Boolean exists = this.existsByDelPhoneAndDelIdNumber(phone, idNumber, tenantId);
            if (!exists) {
                log.warn("asyncRecoverUserInfoGroup after auth, never deleted, uid={}", uid);
                return;
            }
    
            // 根据身份证号查询曾被删除过的uid
            UserDelRecord userDelRecord = applicationContext.getBean(UserDelRecordService.class).queryDelUidByDelIdNumber(idNumber, tenantId);
            if (Objects.isNull(userDelRecord)) {
                log.warn("asyncRecoverUserInfoGroup after auth, delUid is null, uid={}", uid);
                return;
            }
            
            // 恢复流失用户标记
            recoverLostUserMark(userInfo, userDelRecord);
    
            // 1.若当前uid已存在用户分组,则不恢复历史分组
            Integer existsGroup = userInfoGroupDetailService.existsByUid(uid);
            if (Objects.nonNull(existsGroup)) {
                log.warn("asyncRecoverUserInfoGroup after auth, exists Group, uid={}", uid);
                return;
            }
        
            // 2.按加盟商查询最新分组信息（非退押的分组信息，因为退押后分组信息加盟商为0。 说明：用户可以绑定多个加盟商分组信息这个功能上线后，退押不再删除分组信息）
            List<UserInfoGroupDetailHistory> franchiseeHistoryList = userInfoGroupDetailHistoryService.listFranchiseeLatestHistory(userDelRecord.getUid(), tenantId);
            if (CollectionUtils.isEmpty(franchiseeHistoryList)) {
                log.warn("asyncRecoverUserInfoGroup after auth, list group by franchisee is empty, uid={}", uid);
                return;
            }
    
            // 最新分组如果为空，则无需恢复
            franchiseeHistoryList = franchiseeHistoryList.stream().filter(h -> Objects.nonNull(h) && StringUtils.isNotBlank(h.getNewGroupIds())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(franchiseeHistoryList)) {
                log.warn("asyncRecoverUserInfoGroup after auth, franchiseeHistoryList is empty, uid={}", uid);
                return;
            }
    
            Map<Long, String>franchiseeGroupIdMap = franchiseeHistoryList.stream()
                    .collect(Collectors.toMap(UserInfoGroupDetailHistory::getFranchiseeId, UserInfoGroupDetailHistory::getNewGroupIds, (item1, item2) -> item2));
            if (MapUtils.isEmpty(franchiseeGroupIdMap)) {
                log.warn("asyncRecoverUserInfoGroup after auth, franchiseeGroupIdMap is empty, uid={}", uid);
                return;
            }
        
            handleRecoverUserInfoGroup(uid, tenantId, franchiseeGroupIdMap);
        });
    }
    
    private void recoverLostUserMark(UserInfo userInfo, UserDelRecord userDelRecord) {
        // 判断被删前是否为流失用户
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(userDelRecord.getUid());
        if (Objects.isNull(userInfoExtra)) {
            return;
        }
        
        // 恢复流失用户标记
        if (Objects.equals(userInfoExtra.getLostUserStatus(), YesNoEnum.YES.getCode())) {
            userInfoExtraService.updateByUid(UserInfoExtra.builder().lostUserStatus(YesNoEnum.YES.getCode()).uid(userInfo.getUid()).build());
        }

        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.info("recoverLostUserMark Info! electricityConfig is null, tenantId is {}", userInfo.getTenantId());
            return;
        }

        // 流失用户拉新 是否关闭
        if (Objects.equals(electricityConfig.getLostUserFirst(), LostUserFirstEnum.CLOSE.getCode())) {
            log.info("recoverLostUserMark Info! lostUserFirst is close, uid is {}", userInfo.getUid());
            return;
        }

        if (Objects.isNull(electricityConfig.getLostUserDays())) {
            log.info("recoverLostUserMark Info! lostUserDays is close, uid is {}", userInfo.getUid());
            return;
        }

        if (Objects.isNull(userDelRecord.getPurchaseTime())) {
            log.info("recoverLostUserMark Info! purchaseTime is null, uid is {}", userDelRecord.getUid());
            return;
        }
        long timeMillis = System.currentTimeMillis() - userDelRecord.getPurchaseTime();
        log.info("recoverLostUserMark Info! timeMillis is {}, lostUserDays is {}, uid is {}, delUid is {}", timeMillis, electricityConfig.getLostUserDays(), userInfo.getUid(), userDelRecord.getUid());
        if (timeMillis > (electricityConfig.getLostUserDays() * TimeConstant.DAY_MILLISECOND)) {
            userInfoExtraService.updateByUid(UserInfoExtra.builder().lostUserStatus(YesNoEnum.YES.getCode()).uid(userInfo.getUid()).build());
        }
    }
    
    private void handleRecoverUserInfoGroup(Long uid, Integer tenantId, Map<Long, String> franchiseeGroupIdMap) {
        List<UserInfoGroupDetail> list = new ArrayList<>();
        Map<Long, List<String>> groupIdsMap = new HashMap<>();
        
        franchiseeGroupIdMap.forEach((franchiseeId, groupIdsStr) -> {
            String[] split = groupIdsStr.split(CommonConstant.STR_COMMA);
            if (Objects.equals(split.length, 0)) {
                return;
            }
            
            Arrays.stream(split).filter(StringUtils::isNotBlank).forEach(groupIdStr -> {
                if (StringUtils.isBlank(groupIdStr)) {
                    return;
                }
                
                Long groupId = Long.parseLong(groupIdStr);
                UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
                if (Objects.isNull(userInfoGroup)) {
                    return;
                }
    
                UserInfoGroupDetail detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(franchiseeId).tenantId(tenantId)
                        .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).operator(UserInfoGroupConstant.USER_GROUP_OPERATOR_SYSTEM).build();
                
                list.add(detail);
                
                List<String> groupIdList = groupIdsMap.computeIfAbsent(franchiseeId, k -> new ArrayList<>());
                groupIdList.add(groupIdStr);
            });
        });
        
        if (CollectionUtils.isNotEmpty(list)) {
            Integer insert = userInfoGroupDetailService.batchInsert(list);
            if (insert > 0) {
                // 新增历史记录
                List<UserInfoGroupDetailHistory> historyList = new ArrayList<>();
                groupIdsMap.forEach((franchiseeId, groupIds) -> {
                    UserInfoGroupDetailHistory detailHistory = userInfoGroupDetailService.assembleDetailHistory(uid, "", StringUtils.join(groupIds, CommonConstant.STR_COMMA),
                            UserInfoGroupConstant.USER_GROUP_OPERATOR_SYSTEM, franchiseeId, tenantId, UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_SYSTEM);
                    
                    historyList.add(detailHistory);
                });
                
                if (CollectionUtils.isNotEmpty(historyList)) {
                    userInfoGroupDetailHistoryService.batchInsert(historyList);
                }
            }
        }
    }
    
    @Override
    public Integer updateStatusById(Long id, Integer status, Long updateTime) {
        return userDelRecordMapper.updateStatusById(id, status, updateTime);
    }
    
    @Override
    public void asyncRecoverCommonUser(Long uid, Integer type) {
        if (!Objects.equals(type, TokenUser.TYPE_USER)) {
            return;
        }
    
        log.info("asyncRecoverCommonUser after login, uid={}", uid);
        
        executorWrapper.execute(() -> {
            UserDelRecord userDelRecord = this.queryByUidAndStatus(uid, List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
            if (Objects.nonNull(userDelRecord)) {
                userDelRecordMapper.deleteById(userDelRecord.getId());
            }
        });
    
    }
    
    @Slave
    @Override
    public Map<Long, UserDelStatusDTO> listUserStatus(List<Long> uidList, List<Integer> status) {
        if (CollectionUtils.isEmpty(uidList) || CollectionUtils.isEmpty(status)) {
            return null;
        }
        List<UserDelRecord> userDelRecordList = userDelRecordMapper.selectListByUidListAndStatus(uidList, status);
        Map<Long, UserDelRecord> uidRecordMap = null;
        if (CollectionUtils.isNotEmpty(userDelRecordList)) {
            uidRecordMap = userDelRecordList.stream().collect(Collectors.toMap(UserDelRecord::getUid, Function.identity(), (k1, k2) -> k1));
        }
    
        Map<Long, UserDelStatusDTO> map = new HashMap<>(uidList.size());
        Map<Long, UserDelRecord> finalUidRecordMap = uidRecordMap;
    
        for (Long uid : uidList) {
            UserDelStatusDTO userDelStatusDTO = new UserDelStatusDTO();
        
            if (MapUtils.isEmpty(finalUidRecordMap) || !finalUidRecordMap.containsKey(uid)) {
                userDelStatusDTO.setUserStatus(UserStatusEnum.USER_STATUS_VO_COMMON.getCode());
                map.put(uid, userDelStatusDTO);
                continue;
            }
        
            UserDelRecord userDelRecord = finalUidRecordMap.get(uid);
            if (Objects.isNull(userDelRecord)) {
                userDelStatusDTO.setUserStatus(UserStatusEnum.USER_STATUS_VO_COMMON.getCode());
                map.put(uid, userDelStatusDTO);
                continue;
            }
        
            userDelStatusDTO.setDelTime(userDelRecord.getDelTime());
            if (Objects.equals(userDelRecord.getStatus(), UserStatusEnum.USER_STATUS_DELETED.getCode())) {
                userDelStatusDTO.setUserStatus(UserStatusEnum.USER_STATUS_VO_DELETED.getCode());
            } else if (Objects.equals(userDelRecord.getStatus(), UserStatusEnum.USER_STATUS_CANCELLED.getCode())) {
                userDelStatusDTO.setUserStatus(UserStatusEnum.USER_STATUS_VO_CANCELLED.getCode());
            }
        
            map.put(uid, userDelStatusDTO);
        }
    
        return map;
    }
    
    @Override
    public Integer getUserStatus(Long uid, Map<Long, UserDelStatusDTO> userStatusMap) {
        Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
        if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(uid)) {
            UserDelStatusDTO userDelStatusDTO = userStatusMap.get(uid);
            if (Objects.nonNull(userDelStatusDTO)) {
                userStatus = userDelStatusDTO.getUserStatus();
            }
        }
    
        return userStatus;
    }
    
    @Slave
    @Override
    public UserDelRecord queryDelUidByDelIdNumber(String idNumber, Integer tenantId) {
        return userDelRecordMapper.selectDelUidByDelIdNumber(idNumber, tenantId);
    }
    
}
