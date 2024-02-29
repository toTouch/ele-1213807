package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlacePageRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceSaveRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceMapService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceMapVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUpdateShowVO;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    
    @Resource
    private MerchantPlaceMapper merchantPlaceMapper;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantPlaceMapService merchantPlaceMapService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private MerchantAreaService merchantAreaService;
    
    @Slave
    @Override
    public Integer existsByAreaId(Long areaId) {
        return merchantMapper.existsByAreaId(areaId);
    }
    
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
            return Triple.of(false, "120217", "场地名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant place save error, franchisee is null name={}, franchiseeId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getFranchiseeId());
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        if (Objects.nonNull(merchantPlaceSaveRequest.getMerchantAreaId())) {
            MerchantArea merchantArea = merchantAreaService.queryById(merchantPlaceSaveRequest.getMerchantAreaId());
            
            if (Objects.isNull(merchantArea) || !Objects.equals(merchantArea.getTenantId(), tenantId)) {
                log.error("merchant place save error, area is null name={}, merchantAreaId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getMerchantAreaId());
                return Triple.of(false, "120218", "区域不存在");
            }
        }
        
        // 保存场地
        MerchantPlace merchantPlace = new MerchantPlace();
        BeanUtils.copyProperties(merchantPlaceSaveRequest, merchantPlace);
        
        long timeMillis = System.currentTimeMillis();
        merchantPlace.setCreateTime(timeMillis);
        merchantPlace.setUpdateTime(timeMillis);
        merchantPlace.setTenantId(tenantId);
        merchantPlace.setDelFlag(MerchantPlaceConstant.DEL_NORMAL);
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
            return Triple.of(false, "120209", "场地不存在");
        }
        
        if (Objects.nonNull(merchantPlaceSaveRequest.getMerchantAreaId())) {
            MerchantArea merchantArea = merchantAreaService.queryById(merchantPlaceSaveRequest.getMerchantAreaId());
            
            if (Objects.isNull(merchantArea) || !Objects.equals(merchantArea.getTenantId(), tenantId)) {
                log.error("merchant place save error, area is null name={}, merchantAreaId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getMerchantAreaId());
                return Triple.of(false, "120218", "区域不存在");
            }
        }
        
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
            return Triple.of(false, "120203", "加盟商不存在");
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
    public Triple<Boolean, String, Object> remove(Long id) {
        // 检测场地是否存在
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_DELETE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(id);
        MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).build();
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryList(placeMapQueryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            List<Long> merchantIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getMerchantId).collect(Collectors.toList());
            log.error("merchant place remove is bind, placeId={}, merchantIdList={}", id, merchantIdList);
            return Triple.of(false, "120219", "场地被商户绑定，请解绑后操作");
        }
        
        // 检测场地是否存在
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(id);
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "120209", "场地不存在");
        }
        
        // 检测场地是否存在绑定的换电柜
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().status(MerchantPlaceConstant.BIND).placeIdList(placeIdList).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryList(queryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            return Triple.of(false, "120220", "请先解绑换电柜后操作");
        }
        
        // 删除场地
        long currentTimeMillis = System.currentTimeMillis();
        
        MerchantPlace merchantPlaceDel = MerchantPlace.builder().id(id).updateTime(currentTimeMillis).delFlag(MerchantPlaceConstant.DEL_DEL).build();
        merchantPlaceMapper.remove(merchantPlaceDel);
        
        merchantPlaceCabinetBindService.removeByPlaceId(id, currentTimeMillis, MerchantPlaceConstant.DEL_DEL);
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
        
        MerchantPlaceQueryModel merchantPlaceQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantPlaceQueryModel);
        List<MerchantPlace> merchantPlaceList = this.merchantPlaceMapper.selectListByPage(merchantPlaceQueryModel);
        
        if (ObjectUtils.isEmpty(merchantPlaceList)) {
            return Collections.emptyList();
        }
        
        List<MerchantPlaceVO> resList = new ArrayList<>();
        
        List<Long> areaIdList = new ArrayList<>();
        List<Long> idList = new ArrayList<>();
        merchantPlaceList.stream().forEach(item -> {
            areaIdList.add(item.getMerchantAreaId());
            idList.add(item.getId());
        });
        
        // 查询场地绑定的商户
        MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().placeIdList(idList).build();
        List<MerchantPlaceMapVO> merchantPlaceMaps = merchantPlaceMapService.queryBindMerchantName(placeMapQueryModel);
        
        Map<Long, String> merchantNameMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            merchantNameMap = merchantPlaceMaps.stream().collect(Collectors.toMap(MerchantPlaceMapVO::getPlaceId, MerchantPlaceMapVO::getMerchantName, (key, key1) -> key1));
        }
        
        Map<Long, String> areaNameMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(areaIdList)) {
            MerchantAreaRequest areaQuery = MerchantAreaRequest.builder().idList(areaIdList).build();
            List<MerchantArea> merchantAreaList = merchantAreaService.queryList(areaQuery);
            
            if (ObjectUtils.isNotEmpty(merchantAreaList)) {
                areaNameMap = merchantAreaList.stream().collect(Collectors.toMap(MerchantArea::getId, MerchantArea::getName, (key, key1) -> key1));
            }
        }
        
        // 查询场地绑定的柜机
        MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel = MerchantPlaceCabinetBindQueryModel.builder().placeIdList(idList).status(MerchantPlaceConstant.BIND).build();
        List<MerchantPlaceCabinetBindVO> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryBindCabinetName(placeCabinetBindQueryModel);
        
        Map<Long, List<MerchantPlaceCabinetBindVO>> bindCabinetMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            bindCabinetMap = merchantPlaceCabinetBinds.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBindVO::getPlaceId));
        }
        
        for (MerchantPlace merchantPlace : merchantPlaceList) {
            MerchantPlaceVO merchantPlaceVO = new MerchantPlaceVO();
            BeanUtils.copyProperties(merchantPlace, merchantPlaceVO);
            List<MerchantPlaceCabinetBindVO> merchantPlaceCabinetBindVos = bindCabinetMap.get(merchantPlace.getId());
            
            // 柜机
            if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBindVos)) {
                merchantPlaceVO.setCabinetList(merchantPlaceCabinetBindVos);
            }
            
            // 区域名称
            if (ObjectUtils.isNotEmpty(areaNameMap.get(merchantPlace.getMerchantAreaId()))) {
                merchantPlaceVO.setMerchantAreaName(areaNameMap.get(merchantPlace.getMerchantAreaId()));
            }
            
            // 商户名称
            if (ObjectUtils.isNotEmpty(merchantNameMap.get(merchantPlace.getId()))) {
                merchantPlaceVO.setMerchantName(merchantNameMap.get(merchantPlace.getId()));
            }
            
            // 查询
            resList.add(merchantPlaceVO);
        }
        
        return resList;
    }
    
    @Slave
    @Override
    public MerchantPlace queryByIdFromCache(Long placeId) {
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
    public Triple<Boolean, String, Object> queryListCabinet(MerchantPlacePageRequest merchantPlacePageRequest) {
        // 判断场地id是否存在
        MerchantPlace merchantPlace = this.queryByIdFromCache(merchantPlacePageRequest.getPlaceId());
        
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), merchantPlacePageRequest.getTenantId())) {
            log.error("place cabinet error, place is not exists, placeId={}, tenantId={}, curTenantId={}", merchantPlace.getTenantId(), merchantPlacePageRequest.getTenantId());
            return Triple.of(false, "120209", "场地不存在");
        }
        
        merchantPlacePageRequest.setFranchiseeId(merchantPlace.getFranchiseeId());
        
        MerchantPlaceQueryModel queryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, queryModel);
        
        // 查询加盟上下的柜机的信息
        List<MerchantPlaceCabinetVO> merchantPlaceCabinetVOS = merchantPlaceMapper.selectCabinetList(queryModel);
        
        merchantPlaceCabinetVOS = merchantPlaceCabinetVOS.stream().filter(item -> Objects.nonNull(item)).collect(Collectors.toList());
        
        if (ObjectUtils.isEmpty(merchantPlaceCabinetVOS)) {
            return Triple.of(true, null, merchantPlaceCabinetVOS);
        }
    
        merchantPlaceCabinetVOS.forEach(item -> {
            if (Objects.nonNull(item.getPlaceId())) {
                item.setDisable(MerchantPlaceCabinetVO.YES);
            } else {
                item.setDisable(MerchantPlaceCabinetVO.NO);
            }
        });
        
        return Triple.of(true, null, merchantPlaceCabinetVOS);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceVO> queryListPlace(MerchantPlacePageRequest merchantPlacePageRequest) {
        MerchantPlaceQueryModel merchantQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantQueryModel);
        
        List<MerchantPlace> merchantPlaceList = merchantPlaceMapper.selectListByPage(merchantQueryModel);
        if (ObjectUtils.isEmpty(merchantPlaceList)) {
            return Collections.EMPTY_LIST;
        }
        
        // 查询场地是否已经被其他商户给绑定了
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryListForBind(merchantPlacePageRequest.getMerchantId(), merchantPlacePageRequest.getFranchiseeId());
        
        List<Long> placeIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            placeIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getPlaceId).distinct().collect(Collectors.toList());
        }
        
        List<MerchantPlaceVO> list = new ArrayList<>();
        
        for (MerchantPlace item : merchantPlaceList) {
            MerchantPlaceVO vo = new MerchantPlaceVO();
            BeanUtils.copyProperties(item, vo);
            
            // 如果是存在绑定关系的则禁用不能选择
            if (placeIdList.contains(item.getId())) {
                vo.setDisable(MerchantPlaceConstant.DISABLE);
            } else {
                vo.setDisable(MerchantPlaceConstant.ENABLE);
            }
            
            list.add(vo);
        }
        
        return list;
    }
    
    /**
     * 根据id 获取编辑信息
     *
     * @param id
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> queryById(Long id) {
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(id);
        if (Objects.isNull(merchantPlace)) {
            return Triple.of(false, "", "场地不存在");
        }
        
        MerchantPlaceUpdateShowVO merchantPlaceUpdateShowVO = new MerchantPlaceUpdateShowVO();
        BeanUtils.copyProperties(merchantPlace, merchantPlaceUpdateShowVO);
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceUpdateShowVO.getFranchiseeId());
        if (Objects.nonNull(franchisee)) {
            merchantPlaceUpdateShowVO.setFranchiseeName(franchisee.getName());
        }
        
        if (Objects.nonNull(merchantPlace.getMerchantAreaId())) {
            MerchantArea merchantArea = merchantAreaService.queryById(merchantPlace.getMerchantAreaId());
            
            Optional.ofNullable(merchantArea).ifPresent(i -> {
                merchantPlaceUpdateShowVO.setMerchantAreaName(merchantArea.getName());
            });
        }
        
        return Triple.of(true, "", merchantPlaceUpdateShowVO);
    }
    
}
