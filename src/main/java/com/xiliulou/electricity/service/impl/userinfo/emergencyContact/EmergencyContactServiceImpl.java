package com.xiliulou.electricity.service.impl.userinfo.emergencyContact;

import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userinfo.EmergencyContact;
import com.xiliulou.electricity.mapper.userinfo.emergencyContact.EmergencyContactMapper;
import com.xiliulou.electricity.request.userinfo.emergencyContact.EmergencyContactRequest;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.userinfo.emergencyContact.EmergencyContactService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ValidList;
import com.xiliulou.electricity.vo.userinfo.emergencyContact.EmergencyContactVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @date 2024/11/11 10:53:32
 */
@Slf4j
@Service
public class EmergencyContactServiceImpl implements EmergencyContactService {
    
    @Resource
    private EmergencyContactMapper emergencyContactMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Override
    public List<EmergencyContact> listByUidFromCache(Long uid) {
        List<EmergencyContact> emergencyContactListCache = redisService.getWithList(CacheConstant.CACHE_EMERGENCY_CONTACT_LIST + uid, EmergencyContact.class);
        if (CollectionUtil.isNotEmpty(emergencyContactListCache)) {
            return emergencyContactListCache;
        }
        
        List<EmergencyContact> emergencyContactList = this.listByUid(uid);
        if (CollectionUtil.isEmpty(emergencyContactList)) {
            return Collections.emptyList();
        }
        
        redisService.saveWithList(CacheConstant.CACHE_EMERGENCY_CONTACT_LIST + uid, emergencyContactList);
        
        return emergencyContactList;
    }
    
    @Slave
    @Override
    public List<EmergencyContact> listByUid(Long uid) {
        return emergencyContactMapper.selectListByUid(TenantContextHolder.getTenantId(), uid);
    }
    
    @Override
    public List<EmergencyContactVO> listVOByUid(Long uid) {
        List<EmergencyContact> emergencyContactList = this.listByUidFromCache(uid);
        if (CollectionUtils.isEmpty(emergencyContactList)) {
            return Collections.emptyList();
        }
        
        return emergencyContactList.stream().map(emergencyContact -> {
            EmergencyContactVO emergencyContactVO = new EmergencyContactVO();
            BeanUtils.copyProperties(emergencyContact, emergencyContactVO);
            return emergencyContactVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Triple<Boolean, String, Object> checkEmergencyContact(List<EmergencyContactRequest> emergencyContactList, UserInfo mainUserInfo) {
        Long uid = mainUserInfo.getUid();
        if (CollectionUtil.isEmpty(emergencyContactList)) {
            log.warn("EmergencyContact preCheck warn, emergencyContactList is empty! uid={}", uid);
            return Triple.of(false, "紧急联系人不能为空", "120127");
        }
    
        emergencyContactList = emergencyContactList.stream()
                .filter(contact -> StringUtils.isNotBlank(contact.getEmergencyName()) && StringUtils.isNotBlank(contact.getEmergencyPhone()) && Objects.nonNull(
                        contact.getRelation())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(emergencyContactList)) {
            log.warn("EmergencyContact preCheck warn, filter emergencyContactList is empty! uid={}", uid);
            return Triple.of(false, "紧急联系人不能为空", "120127");
        }
        
        Set<String> phoneSet = emergencyContactList.stream().map(EmergencyContactRequest::getEmergencyPhone).collect(Collectors.toSet());
        if (!Objects.equals(phoneSet.size(), emergencyContactList.size())) {
            log.warn("EmergencyContact preCheck warn, EmergencyContact phone can not repeat, uid={}", uid);
            return Triple.of(false, "紧急联系人手机号不能重复", "120128");
        }
        
        if (phoneSet.contains(mainUserInfo.getPhone())) {
            log.warn("EmergencyContact preCheck warn, EmergencyContact phone must be different with mainUserInfo phone, uid={}", uid);
            return Triple.of(false, "紧急联系人手机号不能与用户手机号相同", "120129");
        }
        
        return Triple.of(true, null, emergencyContactList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchSave(List<EmergencyContactRequest> emergencyContactList) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Long uid = SecurityUtils.getUid();
        
        // 二次审核时，先删除再写入
        List<EmergencyContact> exist = this.listByUid(uid);
        if (CollectionUtils.isNotEmpty(exist)) {
            emergencyContactMapper.deleteByUid(uid, tenantId);
        }
        
        List<EmergencyContact> list = emergencyContactList.stream()
                .map(emergencyContactRequest -> EmergencyContact.builder().uid(uid).emergencyName(emergencyContactRequest.getEmergencyName())
                        .emergencyPhone(emergencyContactRequest.getEmergencyPhone()).relation(emergencyContactRequest.getRelation()).tenantId(tenantId)
                        .delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build()).collect(Collectors.toList());
        
        Integer insert = 0;
        if (CollectionUtil.isNotEmpty(list)) {
            insert = emergencyContactMapper.batchInsert(list);
        }
        
        return insert;
    }
    
    @Override
    public R insertOrUpdate(ValidList<EmergencyContactRequest> emergencyContactList) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("EmergencyContact insertOrUpdate WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return R.fail("000100", "未找到用户!");
        }
    
        Triple<Boolean, String, Object> checkResult = this.checkEmergencyContact(emergencyContactList, userInfo);
        if (!checkResult.getLeft()) {
            return R.fail(checkResult.getRight().toString(), checkResult.getMiddle());
        }
        
        List<EmergencyContact> insertList = new ArrayList<>();
        emergencyContactList.forEach(emergencyContactRequest -> {
            Long id = emergencyContactRequest.getId();
            if (Objects.isNull(id)) {
                EmergencyContact emergencyContact = EmergencyContact.builder().uid(SecurityUtils.getUid()).emergencyName(emergencyContactRequest.getEmergencyName())
                        .emergencyPhone(emergencyContactRequest.getEmergencyPhone()).relation(emergencyContactRequest.getRelation()).tenantId(TenantContextHolder.getTenantId())
                        .delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
                
                insertList.add(emergencyContact);
            } else {
                EmergencyContact emergencyContact = EmergencyContact.builder().id(id).emergencyName(emergencyContactRequest.getEmergencyName())
                        .emergencyPhone(emergencyContactRequest.getEmergencyPhone()).relation(emergencyContactRequest.getRelation()).tenantId(TenantContextHolder.getTenantId())
                        .updateTime(System.currentTimeMillis()).build();
                
                emergencyContactMapper.updateById(emergencyContact);
            }
        });
        
        Integer insert = 0;
        if (CollectionUtil.isNotEmpty(insertList)) {
            insert = emergencyContactMapper.batchInsert(insertList);
        }
        
        return R.ok(insert);
    }
}
