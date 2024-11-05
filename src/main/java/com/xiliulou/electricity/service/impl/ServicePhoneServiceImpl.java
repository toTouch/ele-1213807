package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.ServicePhoneDTO;
import com.xiliulou.electricity.entity.ServicePhone;
import com.xiliulou.electricity.mapper.ServicePhoneMapper;
import com.xiliulou.electricity.request.ServicePhoneRequest;
import com.xiliulou.electricity.service.ServicePhoneService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.ServicePhoneVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @date 2024/10/24 17:40:54
 */
@Slf4j
@Service
public class ServicePhoneServiceImpl implements ServicePhoneService {
    
    @Resource
    private ServicePhoneMapper servicePhoneMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Override
    public R insertOrUpdate(List<ServicePhoneRequest> requestPhoneList) {
        Integer tenantId = TenantContextHolder.getTenantId();
        boolean result = redisService.setNx(CacheConstant.SERVICE_PHONE_LOCK_KEY + tenantId, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            boolean flag = false;
            ServicePhone servicePhoneExist = this.queryByTenantIdFromCache(tenantId);
            if (CollectionUtils.isEmpty(requestPhoneList)) {
                if (Objects.nonNull(servicePhoneExist)) {
                    // 删除
                    Integer delete = servicePhoneMapper.deleteById(servicePhoneExist.getId());
                    if (delete > 0) {
                        flag = true;
                    }
                    
                    this.sendOperateRecordForDelete(servicePhoneExist.getPhoneContent());
                }
            } else {
                if (requestPhoneList.size() > ServicePhone.LIMIT_NUM) {
                    log.warn("InsertOrUpdate servicePhone warn! phone number exceed limit");
                    return R.fail("120149", "客服电话最多设置5个");
                }
                
                List<ServicePhoneDTO> requestPhoneDTOList = requestPhoneList.stream()
                        .map(requestPhone -> ServicePhoneDTO.builder().phone(requestPhone.getPhone()).remark(requestPhone.getRemark()).build()).collect(Collectors.toList());
                
                if (Objects.nonNull(servicePhoneExist)) {
                    // 更新
                    Integer update = servicePhoneMapper.update(
                            ServicePhone.builder().id(servicePhoneExist.getId()).phoneContent(JsonUtil.toJson(requestPhoneDTOList)).updateTime(System.currentTimeMillis()).build());
                    if (update > 0) {
                        flag = true;
                    }
                    
                    this.sendOperateRecordForUpdate(servicePhoneExist.getPhoneContent(), requestPhoneDTOList);
                } else {
                    // 新增
                    servicePhoneMapper.insertOne(ServicePhone.builder().phoneContent(JsonUtil.toJson(requestPhoneDTOList)).tenantId(tenantId).delFlag(CommonConstant.DEL_N)
                            .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build());
                    
                    this.sendOperateRecordForInsert(requestPhoneDTOList);
                }
            }
            
            if (flag) {
                // 清除新缓存
                redisService.delete(CacheConstant.SERVICE_PHONE + tenantId);
                // 清除旧缓存
                redisService.delete(CacheConstant.CACHE_SERVICE_PHONE + tenantId);
            }
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.SERVICE_PHONE_LOCK_KEY + tenantId);
        }
    }
    
    private void sendOperateRecordForDelete(String phoneContent) {
        if (StringUtils.isBlank(phoneContent)) {
            return;
        }
        
        List<ServicePhoneDTO> servicePhoneList = JsonUtil.fromJsonArray(phoneContent, ServicePhoneDTO.class);
        if (CollectionUtils.isNotEmpty(servicePhoneList)) {
            for (ServicePhoneDTO servicePhoneDTO : servicePhoneList) {
                String oldPhone = servicePhoneDTO.getPhone();
                String oldRemark = servicePhoneDTO.getRemark();
                
                this.sendOperateRecord(oldPhone, oldRemark, null, null);
            }
        }
    }
    
    private void sendOperateRecordForUpdate(String phoneContent, List<ServicePhoneDTO> requestPhoneDTOList) {
        if (StringUtils.isBlank(phoneContent)) {
            this.sendOperateRecordForInsert(requestPhoneDTOList);
        } else {
            if (CollectionUtils.isEmpty(requestPhoneDTOList)) {
                this.sendOperateRecordForDelete(phoneContent);
            } else {
                List<ServicePhoneDTO> oldPhoneList = JsonUtil.fromJsonArray(phoneContent, ServicePhoneDTO.class);
                // 比如：有5个旧手机号，有3个新手机号
                if (oldPhoneList.size() > requestPhoneDTOList.size()) {
                    // 处理前3个旧手机号
                    for (int i = 0; i < requestPhoneDTOList.size(); i++) {
                        ServicePhoneDTO oldServicePhone = oldPhoneList.get(i);
                        ServicePhoneDTO newServicePhone = requestPhoneDTOList.get(i);
                        this.sendOperateRecord(oldServicePhone.getPhone(), oldServicePhone.getRemark(), newServicePhone.getPhone(), newServicePhone.getRemark());
                    }
                    
                    // 处理后2个旧手机号
                    for (int i = requestPhoneDTOList.size(); i < oldPhoneList.size(); i++) {
                        ServicePhoneDTO oldServicePhone = oldPhoneList.get(i);
                        this.sendOperateRecord(oldServicePhone.getPhone(), oldServicePhone.getRemark(), null, null);
                    }
                } else if (oldPhoneList.size() < requestPhoneDTOList.size()) {
                    // 比如：有5个新手机号，有3个旧手机号
                    // 处理前3个新手机号
                    for (int i = 0; i < oldPhoneList.size(); i++) {
                        ServicePhoneDTO oldServicePhone = oldPhoneList.get(i);
                        ServicePhoneDTO newServicePhone = requestPhoneDTOList.get(i);
                        this.sendOperateRecord(oldServicePhone.getPhone(), oldServicePhone.getRemark(), newServicePhone.getPhone(), newServicePhone.getRemark());
                    }
                    
                    // 处理后2个新手机号
                    for (int i = oldPhoneList.size(); i < requestPhoneDTOList.size(); i++) {
                        ServicePhoneDTO newServicePhone = requestPhoneDTOList.get(i);
                        this.sendOperateRecord(null, null, newServicePhone.getPhone(), newServicePhone.getRemark());
                    }
                } else {
                    // 比如：有5个新手机号，有5个旧手机号
                    for (int i = 0; i < oldPhoneList.size(); i++) {
                        ServicePhoneDTO oldServicePhone = oldPhoneList.get(i);
                        ServicePhoneDTO newServicePhone = requestPhoneDTOList.get(i);
                        this.sendOperateRecord(oldServicePhone.getPhone(), oldServicePhone.getRemark(), newServicePhone.getPhone(), newServicePhone.getRemark());
                    }
                }
            }
        }
    }
    
    private void sendOperateRecordForInsert(List<ServicePhoneDTO> requestPhoneDTOList) {
        if (CollectionUtils.isEmpty(requestPhoneDTOList)) {
            return;
        }
        
        for (ServicePhoneDTO servicePhoneDTO : requestPhoneDTOList) {
            String newPhone = servicePhoneDTO.getPhone();
            String newRemark = servicePhoneDTO.getRemark();
            
            this.sendOperateRecord(null, null, newPhone, newRemark);
        }
    }
    
    private void sendOperateRecord(String oldPhone, String oldRemark, String newPhone, String newRemark) {
        Map<String, String> oldMap = new HashMap<>(2);
        oldMap.put("phone", StringUtils.isBlank(oldPhone) ? "空" : oldPhone);
        oldMap.put("remark", StringUtils.isBlank(oldRemark) ? "空" : oldRemark);
        Map<String, String> newMap = new HashMap<>(2);
        newMap.put("phone", StringUtils.isBlank(newPhone) ? "空" : newPhone);
        newMap.put("remark", StringUtils.isBlank(newRemark) ? "空" : newRemark);
        
        if (!Objects.equals(oldPhone, newPhone) || !Objects.equals(oldRemark, newRemark)) {
            operateRecordUtil.record(oldMap, newMap);
        }
    }
    
    @Override
    public ServicePhone queryByTenantIdFromCache(Integer tenantId) {
        ServicePhone servicePhoneCache = redisService.getWithHash(CacheConstant.SERVICE_PHONE + tenantId, ServicePhone.class);
        if (Objects.nonNull(servicePhoneCache)) {
            return servicePhoneCache;
        }
        
        ServicePhone servicePhone = servicePhoneMapper.selectByTenantId(tenantId);
        if (Objects.isNull(servicePhone)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.SERVICE_PHONE + tenantId, servicePhone);
        return servicePhone;
    }
    
    @Slave
    @Override
    public List<ServicePhoneVO> listPhones(Integer tenantId) {
        ServicePhone servicePhone = this.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(servicePhone) || StringUtils.isBlank(servicePhone.getPhoneContent())) {
            return Collections.emptyList();
        }
        
        return JsonUtil.fromJsonArray(servicePhone.getPhoneContent(), ServicePhoneVO.class);
    }
    
}
