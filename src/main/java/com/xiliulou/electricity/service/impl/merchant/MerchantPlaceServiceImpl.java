package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPlacePageRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceSaveRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:46
 * @desc
 */
@Service("merchantPlaceService")
@Slf4j
public class MerchantPlaceServiceImpl implements MerchantPlaceService {
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Slave
    @Override
    public Integer existsByAreaId(Long areaId) {
        return merchantMapper.existsByAreaId(areaId);
    }
    private MerchantPlaceMapper merchantPlaceMapper;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Slave
    @Override
    public List<MerchantPlace> queryList(MerchantPlaceQueryModel placeQueryModel) {
        return merchantPlaceMapper.list(placeQueryModel);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> save(MerchantPlaceSaveRequest merchantPlaceSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        MerchantPlaceQueryModel queryModel = MerchantPlaceQueryModel.builder().name(merchantPlaceSaveRequest.getName()).tenantId(tenantId).build();
        
        // 检测场地名称是否重复
        Integer count = merchantPlaceMapper.checkIsExists(queryModel);
        if (count > 0) {
            return Triple.of(false, "", "场地名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant save error, franchisee is null name={}, franchiseeId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getFranchiseeId());
            return Triple.of(false, "", "加盟商不存在");
        }
        
        // 检测区域是否存在
        
        // 保存场地
        MerchantPlace merchantPlace = new MerchantPlace();
        BeanUtils.copyProperties(merchantPlaceSaveRequest, merchantPlace);
        long timeMillis = System.currentTimeMillis();
        merchantPlace.setCreateTime(timeMillis);
        merchantPlace.setUpdateTime(timeMillis);
        merchantPlace.setDelFlag(MerchantPlace.DEL_NORMAL);
        merchantPlaceMapper.insert(merchantPlace);
        
        return Triple.of(true, null, merchantPlace);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> update(MerchantPlaceSaveRequest merchantPlaceSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 检测场地是否存在
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(merchantPlaceSaveRequest.getId());
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "", "场地不存在");
        }
        
        // 检测区域是否存在
        
        MerchantPlaceQueryModel queryModel = MerchantPlaceQueryModel.builder().name(merchantPlaceSaveRequest.getName()).tenantId(tenantId).nqId(merchantPlaceSaveRequest.getId())
                .build();
        
        // 检测场地名称是否重复
        Integer count = merchantPlaceMapper.checkIsExists(queryModel);
        if (count > 0) {
            return Triple.of(false, "", "场地名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant save error, franchisee is null name={}, franchiseeId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getFranchiseeId());
            return Triple.of(false, "", "加盟商不存在");
        }
        
        // 修改场地信息
        MerchantPlace merchantPlaceUpdate = new MerchantPlace();
        BeanUtils.copyProperties(merchantPlaceSaveRequest, merchantPlaceUpdate);
        merchantPlaceUpdate.setUpdateTime(System.currentTimeMillis());
        merchantPlaceMapper.update(merchantPlaceUpdate);
        
        return Triple.of(true, null, merchantPlaceUpdate);
    }
    
    @Override
    public void deleteCache(MerchantPlace merchantPlace) {
        redisService.delete(CacheConstant.CACHE_MERCHANT_PLACE + merchantPlace.getId());
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        // 检测场地是否存在
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_DELETE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 检测场地是否存在
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(id);
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "", "场地不存在");
        }
        
        // 检测场地是否存在绑定的换电柜
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(id);
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().status(MerchantPlaceCabinetBind.BIND).placeIdList(placeIdList).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryList(queryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            return Triple.of(false, "", "请先解绑换电柜后操作");
        }
        
        // 删除场地
        MerchantPlace merchantPlaceDel = MerchantPlace.builder().id(id).updateTime(System.currentTimeMillis()).delFlag(MerchantPlace.DEL_DEL).build();
        merchantPlaceMapper.delete(merchantPlaceDel);
        
        return Triple.of(true, "", merchantPlace);
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantPlacePageRequest merchantPlacePageRequest) {
        MerchantPlaceQueryModel merchantQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantQueryModel);
        
        return merchantPlaceMapper.countTotal(merchantQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceVO> listByPage(MerchantPlacePageRequest merchantPlacePageRequest) {
        
        MerchantPlaceQueryModel merchantQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantQueryModel);
        List<MerchantPlace> merchantPlaceList = this.merchantPlaceMapper.selectListByPage(merchantQueryModel);
        
        if (ObjectUtils.isEmpty(merchantPlaceList)) {
            return Collections.EMPTY_LIST;
        }
    
        List<MerchantPlaceVO> resList = new ArrayList<>();
    
        Set<Long> areaIdList = new HashSet<>();
        List<Long> idList = new ArrayList<>();
        merchantPlaceList.stream().forEach(item -> {
            areaIdList.add(item.getMerchantAreaId());
            idList.add(item.getId());
        });
        
        // 批量查询区域
        
        // 查询场地绑定的柜机
        MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel = MerchantPlaceCabinetBindQueryModel.builder().placeIdList(idList).status(MerchantPlaceCabinetBind.BIND).build();
        List<MerchantPlaceCabinetBindVO> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryBindCabinetName(placeCabinetBindQueryModel);
        Map<Long, List<MerchantPlaceCabinetBindVO>> bindCabinetMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            bindCabinetMap = merchantPlaceCabinetBinds.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBindVO::getPlaceId));
        }
        
        for (MerchantPlace merchantPlace : merchantPlaceList) {
            MerchantPlaceVO merchantPlaceVO = new MerchantPlaceVO();
            BeanUtils.copyProperties(merchantPlace, merchantPlaceVO);
            
            if (ObjectUtils.isNotEmpty(bindCabinetMap.get(merchantPlace.getId()))) {
                List<MerchantPlaceCabinetBindVO> merchantPlaceCabinetBindVos = bindCabinetMap.get(merchantPlace.getId());
                String cabinetName = merchantPlaceCabinetBindVos.stream().map(MerchantPlaceCabinetBindVO::getCabinetName).collect(Collectors.joining(StringConstant.COMMA_EN));
                merchantPlaceVO.setCabinetName(cabinetName);
            }
            
            // 查询
            resList.add(merchantPlaceVO);
        }
        
        return resList;
    }
    
    @Slave
    @Override
    public MerchantPlace queryFromCacheById(Long placeId) {
        MerchantPlace merchantPlace = null;
        merchantPlace = redisService.getWithHash(CacheConstant.CACHE_MERCHANT_PLACE + placeId, MerchantPlace.class);
        if (Objects.isNull(merchantPlace)) {
            merchantPlace = merchantPlaceMapper.selectById(placeId);
            if (Objects.nonNull(merchantPlace)) {
                redisService.saveWithHash(CacheConstant.CACHE_MERCHANT_PLACE + placeId, merchantPlace);
            }
        }
        
        return merchantPlace;
    }
    
    /**
     * 场地柜机下拉框数据
     *
     * @param merchantPlacePageRequest
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> getCabinetList(MerchantPlacePageRequest merchantPlacePageRequest) {
        // 判断场地id是否存在
        MerchantPlace merchantPlace = this.queryFromCacheById(merchantPlacePageRequest.getPlaceId());
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), merchantPlacePageRequest.getTenantId())) {
            log.error("place cabinet error, place is not exists, placeId={}, tenantId={}, curTenantId={}", merchantPlace.getTenantId(), merchantPlacePageRequest.getTenantId());
            return Triple.of(false, "", "场地不存在");
        }
    
        merchantPlacePageRequest.setFranchiseeId(merchantPlace.getFranchiseeId());
        
        MerchantPlaceQueryModel queryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlace, queryModel);
        
        // 查询加盟上下的柜机的信息
        List<MerchantPlaceCabinetVO> merchantPlaceCabinetVOS = merchantPlaceMapper.selectCabinetList(queryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetVOS)) {
            merchantPlaceCabinetVOS.forEach(item -> {
                if (Objects.nonNull(item.getPlaceId())) {
                    item.setDisable(MerchantPlaceCabinetVO.YES);
                } else {
                    item.setDisable(MerchantPlaceCabinetVO.NO);
                }
            });
        }
        
        return Triple.of(true, null , merchantPlaceCabinetVOS);
    }
}
