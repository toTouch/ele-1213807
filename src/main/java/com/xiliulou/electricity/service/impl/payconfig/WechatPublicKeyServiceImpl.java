package com.xiliulou.electricity.service.impl.payconfig;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.pay.WechatPublicKeyBO;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.payparams.WechatPublicKeyEntity;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.WechatPublicKeyMapper;
import com.xiliulou.electricity.queryModel.WechatPublicKeyQueryModel;
import com.xiliulou.electricity.request.payparams.WechatPublicKeyRequest;
import com.xiliulou.electricity.service.pay.WechatPublicKeyService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.WX_PUBLIC_KEY_CACHE_KEY;
import static com.xiliulou.electricity.constant.CacheConstant.WX_PUBLIC_KEY_CACHE_KEY_TENANT;
import static com.xiliulou.electricity.constant.CacheConstant.WX_PUBLIC_KEY_LOCK_KEY_FRANCHISEE;
import static com.xiliulou.electricity.constant.StringConstant.SPACES;

/**
 * <p>
 * Description: This class is WechatPublicKeyServiceImpl!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/1
 **/
@Slf4j
@Service
public class WechatPublicKeyServiceImpl implements WechatPublicKeyService {
    
    private final RedisService redisService;
    
    private final WechatPublicKeyMapper wechatPublicKeyMapper;
    
    public WechatPublicKeyServiceImpl(RedisService redisService, WechatPublicKeyMapper wechatPublicKeyMapper) {
        this.redisService = redisService;
        this.wechatPublicKeyMapper = wechatPublicKeyMapper;
    }
    
    @Override
    public WechatPublicKeyBO queryByIdFromCache(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        if (redisService.hasKey(String.format(WX_PUBLIC_KEY_CACHE_KEY, id))) {
            return JsonUtil.fromJson(redisService.get(String.format(WX_PUBLIC_KEY_CACHE_KEY, id)), WechatPublicKeyBO.class);
        }
        WechatPublicKeyBO wechatPublicKeyBO = queryByIdFromDB(id);
        if (Objects.nonNull(wechatPublicKeyBO)) {
            redisService.set(String.format(WX_PUBLIC_KEY_CACHE_KEY, id), JsonUtil.toJson(wechatPublicKeyBO));
            return wechatPublicKeyBO;
        }
        return null;
    }
    
    @Override
    public WechatPublicKeyBO queryByIdFromDB(Long id) {
        WechatPublicKeyEntity entity = this.wechatPublicKeyMapper.selectByQueryModel(WechatPublicKeyQueryModel.builder().id(id).build());
        if (Objects.isNull(entity)) {
            return null;
        }
        return convertBO(entity);
    }
    
    @Override
    public WechatPublicKeyBO queryByTenantIdFromCache(Long tenantId, Long franchiseeId) {
        if (Objects.isNull(tenantId)) {
            return null;
        }
        if (redisService.hasKey(String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, tenantId, ObjectUtils.defaultIfNull(franchiseeId, "")))) {
            return JsonUtil.fromJson(redisService.get(String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, tenantId, ObjectUtils.defaultIfNull(franchiseeId, ""))),
                    WechatPublicKeyBO.class);
        }
        
        WechatPublicKeyBO wechatPublicKeyBO = queryByTenantIdFromDB(tenantId, franchiseeId);
        
        if (Objects.nonNull(wechatPublicKeyBO)) {
            redisService.set(String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, tenantId, ObjectUtils.defaultIfNull(franchiseeId, "")), JsonUtil.toJson(wechatPublicKeyBO));
            return wechatPublicKeyBO;
        }
        return null;
    }
    
    @Override
    public WechatPublicKeyBO queryByTenantIdFromDB(Long tenantId, Long franchiseeId) {
        WechatPublicKeyEntity entity = this.wechatPublicKeyMapper.selectByQueryModel(WechatPublicKeyQueryModel.builder().tenantId(tenantId).franchiseeId(franchiseeId).build());
        if (Objects.isNull(entity)) {
            return null;
        }
        return convertBO(entity);
    }
    
    @Override
    public List<WechatPublicKeyBO> queryListByTenantIdFromDB(Long tenantId, List<Long> franchiseeIds) {
        if (Objects.isNull(tenantId)) {
            return List.of();
        }
        List<WechatPublicKeyEntity> entityList = this.wechatPublicKeyMapper.selectListByQueryModel(
                WechatPublicKeyQueryModel.builder().tenantId(tenantId).franchiseeIds(franchiseeIds).build());
        if (Objects.nonNull(entityList)) {
            return entityList.stream().map(this::convertBO).collect(Collectors.toList());
        }
        return List.of();
    }
    
    @Override
    public List<WechatPublicKeyBO> queryListByTenantIdFromCache(Long tenantId, List<Long> franchiseeIds) {
        if (Objects.isNull(tenantId)) {
            return List.of();
        }
        List<String> keys = builderCacheKey(tenantId, franchiseeIds);
        List<WechatPublicKeyBO> bos = redisService.multiJsonGet(keys, WechatPublicKeyBO.class);
        if (CollectionUtils.isEmpty(bos)) {
            List<WechatPublicKeyBO> entities = this.queryListByTenantIdFromDB(tenantId, franchiseeIds);
            if (CollectionUtils.isEmpty(entities)) {
                return List.of();
            }
            Map<String, String> collect = entities.stream().collect(
                    Collectors.toMap(wechatPublicKeyBO -> String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, wechatPublicKeyBO.getTenantId(), wechatPublicKeyBO.getFranchiseeId()),
                            JsonUtil::toJson));
            redisService.multiSet(collect);
            return entities;
        }
        
        if (CollectionUtils.isNotEmpty(bos)) {
            franchiseeIds.removeAll(bos.stream().map(WechatPublicKeyBO::getFranchiseeId).collect(Collectors.toSet()));
            List<WechatPublicKeyBO> wechatPublicKeyBOS = this.queryListByTenantIdFromDB(tenantId, franchiseeIds);
            if (CollectionUtils.isNotEmpty(wechatPublicKeyBOS)) {
                for (WechatPublicKeyBO bo : wechatPublicKeyBOS) {
                    redisService.set(String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, tenantId, ObjectUtils.defaultIfNull(bo.getFranchiseeId(), "")), JsonUtil.toJson(bo));
                    bos.add(bo);
                }
            }
        }
        return bos;
    }
    
    @Override
    public void clearCache(Long tenantId, Long franchiseeId) {
        if (Objects.isNull(tenantId)) {
            return;
        }
        
        String cacheKeyByTenantId = String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, tenantId, ObjectUtils.defaultIfNull(franchiseeId, ""));
        if (!redisService.hasKey(cacheKeyByTenantId)) {
            return;
        }
        
        WechatPublicKeyBO bo = JsonUtil.fromJson(redisService.get(cacheKeyByTenantId),WechatPublicKeyBO.class);
        if (Objects.isNull(bo)) {
            return;
        }
        
        String cacheKeyById = String.format(WX_PUBLIC_KEY_CACHE_KEY, bo.getId());
        
        redisService.delete(cacheKeyByTenantId);
        redisService.delete(cacheKeyById);
    }
    
    @Override
    public int save(WechatPublicKeyBO wechatPublicKeyBO) {
        if (Objects.isNull(wechatPublicKeyBO)) {
            return 0;
        }
        WechatPublicKeyEntity entity = convertDO(wechatPublicKeyBO);
        return this.wechatPublicKeyMapper.insert(entity);
    }
    
    @Override
    public int update(WechatPublicKeyBO wechatPublicKeyBO) {
        if (Objects.isNull(wechatPublicKeyBO)) {
            return 0;
        }
        WechatPublicKeyEntity entity = convertDO(wechatPublicKeyBO);
        int result = this.wechatPublicKeyMapper.update(entity);
        if (result > 0) {
            clearCache(wechatPublicKeyBO.getTenantId(), wechatPublicKeyBO.getFranchiseeId());
        }
        return result;
    }
    
    @Override
    public int delete(Long id) {
        if (Objects.isNull(id)) {
            return 0;
        }
        WechatPublicKeyBO wechatPublicKeyBO = this.queryByIdFromCache(id);
        if (Objects.isNull(wechatPublicKeyBO)) {
            return 0;
        }
        int result = this.wechatPublicKeyMapper.delete(id);
        
        if (result > 0) {
            clearCache(wechatPublicKeyBO.getTenantId(), wechatPublicKeyBO.getFranchiseeId());
        }
        return result;
    }
    
    @Override
    public R<?> uploadFile(MultipartFile file, Long franchiseeId) {
        Long tenantId = TenantContextHolder.getTenantId().longValue();
        if (Objects.isNull(file)) {
            return R.failMsg("上传公钥不能为空");
        }
        if (Objects.isNull(franchiseeId)) {
            franchiseeId = MultiFranchiseeConstant.DEFAULT_FRANCHISEE;
        }
        
        if (!redisService.setNx(String.format(WX_PUBLIC_KEY_LOCK_KEY_FRANCHISEE, tenantId, franchiseeId), StringConstant.EMPTY, NumberConstant.FIVE * NumberConstant.A_THOUSAND,
                false)) {
            return R.failMsg("操作频繁!");
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String pubKeyStr = br.lines().collect(Collectors.joining(StringConstant.CHANGE_ROW));
            WechatPublicKeyBO publicKeyBO = this.queryByTenantIdFromCache(tenantId, franchiseeId);
            if (Objects.isNull(publicKeyBO)) {
                WechatPublicKeyBO insert = WechatPublicKeyBO.builder().pubKey(pubKeyStr).uploadTime(System.currentTimeMillis()).pubKeyId(StringConstant.EMPTY)
                        .payParamsId(NumberConstant.NEGATIVE_ONE).franchiseeId(franchiseeId).tenantId(tenantId).build();
                return this.save(insert) > NumberConstant.ZERO ? R.ok() : R.fail("证书上传失败，请重试！");
            }
            
            publicKeyBO.setPubKey(pubKeyStr);
            publicKeyBO.setUploadTime(System.currentTimeMillis());
            return this.update(publicKeyBO) > NumberConstant.ZERO ? R.ok() : R.fail("证书上传失败，请重试！");
        } catch (Exception e) {
            log.error("certificate get error, tenantId={}", tenantId , e);
            return R.fail("证书内容获取失败，请重试！");
        } finally {
            redisService.remove(String.format(WX_PUBLIC_KEY_LOCK_KEY_FRANCHISEE, tenantId, franchiseeId));
        }
    }
    
    @Override
    public R<?> saveOrUpdate(WechatPublicKeyBO request) {
        if (!redisService.setNx(String.format(WX_PUBLIC_KEY_LOCK_KEY_FRANCHISEE, request.getTenantId(), request.getFranchiseeId()), StringConstant.EMPTY, NumberConstant.FIVE * NumberConstant.A_THOUSAND, false)){
            return R.failMsg("操作频繁!");
        }
        try {
            if (!request.getPubKey().startsWith(StringConstant.PREFIX_PUBLIC_KEY) || !request.getPubKey().endsWith(StringConstant.SUFFIX_PUBLIC_KEY)){
                return R.failMsg("请上传正确的证书!");
            }
            String pubKey = formatPublicKey(request.getPubKey());
            if (Objects.isNull(request.getFranchiseeId())){
                request.setFranchiseeId(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
            }
            
            request.setPubKey(pubKey);
            WechatPublicKeyBO publicKeyBO = this.queryByTenantIdFromCache(request.getTenantId(), request.getFranchiseeId());
            if (Objects.isNull(request.getId())){
                if (Objects.nonNull(publicKeyBO)){
                    return R.failMsg("当前商户已存在证书，请勿继续新增！");
                }
                return this.save(request) > NumberConstant.ZERO ? R.ok() : R.fail("证书保存失败，请重试！");
            }
            publicKeyBO = this.queryByIdFromCache(request.getId());
            if (Objects.isNull(publicKeyBO)){
                return R.failMsg("证书不存在，请先上传证书！");
            }
            publicKeyBO.setPubKeyId(request.getPubKeyId());
            publicKeyBO.setPubKey(request.getPubKey());
            publicKeyBO.setUploadTime(System.currentTimeMillis());
            return this.update(publicKeyBO) > NumberConstant.ZERO ? R.ok() : R.fail("证书保存失败，请重试！");
        }finally {
            redisService.remove(String.format(WX_PUBLIC_KEY_LOCK_KEY_FRANCHISEE, request.getTenantId(), request.getFranchiseeId()));
        }
        
    }
    
    
    private WechatPublicKeyBO convertBO(WechatPublicKeyEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        return WechatPublicKeyBO.builder().id(entity.getId()).pubKeyId(entity.getPubKeyId()).pubKey(entity.getPubKey()).payParamsId(entity.getPayParamsId())
                .createTime(entity.getCreateTime()).updateTime(entity.getUpdateTime()).franchiseeId(entity.getFranchiseeId()).uploadTime(entity.getUploadTime())
                .tenantId(entity.getTenantId()).build();
    }
    
    private WechatPublicKeyEntity convertDO(WechatPublicKeyBO bo) {
        if (Objects.isNull(bo)) {
            return null;
        }
        WechatPublicKeyEntity entity = new WechatPublicKeyEntity();
        entity.setId(bo.getId());
        entity.setPubKeyId(bo.getPubKeyId());
        entity.setPubKey(bo.getPubKey());
        entity.setPayParamsId(bo.getPayParamsId());
        entity.setCreateTime(bo.getCreateTime());
        entity.setUpdateTime(bo.getUpdateTime());
        entity.setFranchiseeId(bo.getFranchiseeId());
        entity.setUploadTime(bo.getUploadTime());
        entity.setTenantId(bo.getTenantId());
        return entity;
    }
    
    private List<String> builderCacheKey(Long tenantId, List<Long> franchiseeIds) {
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            return Collections.emptyList();
        }
        return franchiseeIds.stream().map(franchiseeId -> String.format(WX_PUBLIC_KEY_CACHE_KEY_TENANT, tenantId, franchiseeId)).collect(Collectors.toList());
    }
    
    private String formatPublicKey(String publicKey) {
        return publicKey.replace(StringConstant.PREFIX_PUBLIC_KEY, StringConstant.EMPTY)
                .replace(StringConstant.SUFFIX_PUBLIC_KEY, StringConstant.EMPTY)
                .replaceAll(SPACES, StringConstant.EMPTY);
    }
}
