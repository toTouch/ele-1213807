package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.faq.FaqV2BO;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.faq.FaqV2;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.mapper.faq.FaqV2Mapper;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.reqparam.faq.AdminFaqChangeTypeReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqUpDownReq;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqListVos;
import com.xiliulou.electricity.vo.faq.FaqVo;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 常见问题Service接口实现类
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Service
@AllArgsConstructor
public class FaqV2ServiceImpl implements FaqV2Service {
    
    private final FaqV2Mapper faqV2Mapper;
    
    private final FaqCategoryV2Mapper faqCategoryV2Mapper;
    
    private final ElectricityConfigMapper electricityConfigMapper;
    
    @Override
    public R saveFaqQuery(AdminFaqReq faqReq) {
        FaqV2 faq = BeanUtil.toBean(faqReq, FaqV2.class);
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqReq.getTypeId());
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        
        faq.setTenantId(TenantContextHolder.getTenantId());
        faq.setOpUser(SecurityUtils.getUid());
        faq.setOnShelf(UpDownEnum.DOWN.getCode());
        faq.setCreateTime(System.currentTimeMillis());
        faq.setUpdateTime(System.currentTimeMillis());
        faqV2Mapper.insert(faq);
        return R.ok();
    }
    
    @Override
    public R updateFaqReq(AdminFaqReq faqReq) {
        FaqV2 faq = this.queryEntity(faqReq.getId());
        if (Objects.isNull(faq)) {
            return null;
        }
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqReq.getTypeId());
        
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        BeanUtil.copyProperties(faqReq, faq);
        faq.setOpUser(SecurityUtils.getUid());
        faq.setUpdateTime(System.currentTimeMillis());
        faqV2Mapper.updateByPrimaryKeySelective(faq);
        return null;
    }
    
    @Override
    public void removeByCategoryId(Long id) {
        FaqV2 faqV2 = new FaqV2();
        faqV2.setTypeId(id);
        List<FaqV2> faqs = faqV2Mapper.selectListByParams(faqV2);
        if (CollectionUtil.isNotEmpty(faqs)) {
            faqV2Mapper.removeByIds(faqs.stream().map(FaqV2::getId).collect(Collectors.toList()));
        }
        faqCategoryV2Mapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public void upDownBatch(AdminFaqUpDownReq faqUpDownReq) {
        if (CollectionUtil.isEmpty(faqUpDownReq.getIds())) {
            return;
        }
        
        ArrayList<FaqV2> faqV2s = new ArrayList<>();
        long updateTime = System.currentTimeMillis();
        faqUpDownReq.getIds().forEach(id -> {
            FaqV2 faqV2 = new FaqV2();
            faqV2.setId(id);
            faqV2.setOnShelf(faqUpDownReq.getOnShelf());
            faqV2.setUpdateTime(updateTime);
            faqV2s.add(faqV2);
        });
        faqV2Mapper.batchUpdateByIds(faqV2s);
        
    }
    
    @Override
    public R changeTypeBatch(AdminFaqChangeTypeReq faqChangeTypeReq) {
        List<Long> ids = faqChangeTypeReq.getIds();
        if (CollectionUtil.isEmpty(ids)) {
            return R.ok();
        }
        
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqChangeTypeReq.getTypeId());
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId + ids.size()) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        
        ArrayList<FaqV2> faqV2s = new ArrayList<>();
        long updateTime = System.currentTimeMillis();
        ids.forEach(id -> {
            FaqV2 faqV2 = new FaqV2();
            faqV2.setId(id);
            faqV2.setTypeId(faqChangeTypeReq.getTypeId());
            faqV2.setUpdateTime(updateTime);
            faqV2s.add(faqV2);
        });
        faqV2Mapper.batchUpdateByIds(faqV2s);
        return R.ok();
    }
    
    @Override
    public void removeByIds(List<Long> ids) {
        faqV2Mapper.removeByIds(ids);
    }
    
    /**
     * 根据问题标题、租户查询常见问题
     */
    @Override
    public Map<Long, List<FaqV2BO>> listFaqQuery(AdminFaqQuery faqQuery) {
        faqQuery.setTenantId(TenantContextHolder.getTenantId());
        FaqV2 faqV2 = new FaqV2();
        BeanUtils.copyProperties(faqQuery, faqV2);
        List<FaqV2BO> faqVos = faqV2Mapper.selectLeftJoinByParams(faqV2);
        if (CollectionUtil.isEmpty(faqVos)) {
            return null;
        }
        
        // 按问题分类分组，对问题分组内排序 升序
        return faqVos.stream().sorted(Comparator.comparing(FaqV2BO::getSort)).collect(Collectors.groupingBy(FaqV2BO::getTypeId));
    }
    
    /**
     * 后台查看常见问题
     */
    @Override
    public List<FaqListVos> listFaqQueryForBackstage(AdminFaqQuery faqQuery) {
        Map<Long, List<FaqV2BO>> listMap = listFaqQuery(faqQuery);
        if (CollectionUtil.isEmpty(listMap)) {
            return Collections.emptyList();
        }
        
        // 1. 未指定问题分类时 后台首页展示分类一
        Long queryTypeId = faqQuery.getTypeId();
        if (Objects.isNull(queryTypeId)) {
            // 结果集填充
            List<FaqListVos> faqListVosList = listMap.entrySet().stream().map(e -> {
                FaqListVos faqListVos = new FaqListVos();
                faqListVos.setId(e.getKey());
                faqListVos.setTypeId(e.getKey());
                faqListVos.setType(e.getValue().stream().findAny().get().getType());
                faqListVos.setSort(e.getValue().stream().findAny().get().getTypeSort());
                long count = e.getValue().size();
                faqListVos.setCount((int) count);
                return faqListVos;
            }).sorted(Comparator.comparing(FaqListVos::getTypeId)).collect(Collectors.toList());
            
            faqListVosList.get(0).setFaqBOList(listMap.get(faqListVosList.get(0).getTypeId()));
            return faqListVosList;
        }
        
        // 2. 指定了问题分类时
        return getFaqListVosByTypeId(listMap, queryTypeId);
    }
    
    
    /**
     * 小程序查看常见问题
     */
    @Override
    public List<FaqListVos> listFaqQueryForApp(AdminFaqQuery faqQuery) {
        Map<Long, List<FaqV2BO>> listMap = listFaqQuery(faqQuery);
        if (CollectionUtil.isEmpty(listMap)) {
            return Collections.emptyList();
        }
        
        ElectricityConfig electricityConfig = electricityConfigMapper.selectElectricityConfigByTenantId(TenantContextHolder.getTenantId());
        
        if (Objects.isNull(electricityConfig) || electricityConfig.getWxCustomer() == 0) {
            return Collections.emptyList();
        }
        // 1. 未指定问题分类时 小程序端首页展示全部
        Long queryTypeId = faqQuery.getTypeId();
        if (Objects.isNull(queryTypeId)) {
            // 结果集填充
            return listMap.entrySet().stream().map(e -> {
                FaqListVos faqListVos = new FaqListVos();
                faqListVos.setId(e.getKey());
                faqListVos.setTypeId(e.getKey());
                faqListVos.setType(e.getValue().stream().findAny().get().getType());
                faqListVos.setSort(e.getValue().stream().findAny().get().getTypeSort());
                faqListVos.setFaqBOList(e.getValue());
                long count = e.getValue().size();
                faqListVos.setCount((int) count);
                return faqListVos;
            }).sorted(Comparator.comparing(FaqListVos::getTypeId)).collect(Collectors.toList());
        }
        
        // 2. 指定了问题分类时
        return getFaqListVosByTypeId(listMap, queryTypeId);
    }
    
    private static List<FaqListVos> getFaqListVosByTypeId(Map<Long, List<FaqV2BO>> listMap, Long queryTypeId) {
        return listMap.entrySet().stream().map(e -> {
            FaqListVos faqListVos = new FaqListVos();
            faqListVos.setId(e.getKey());
            faqListVos.setTypeId(e.getKey());
            faqListVos.setType(e.getValue().stream().findAny().get().getType());
            faqListVos.setSort(e.getValue().stream().findAny().get().getTypeSort());
            long count = e.getValue().size();
            faqListVos.setCount((int) count);
            if (queryTypeId.equals(e.getKey())) {
                faqListVos.setFaqBOList(e.getValue());
            }
            return faqListVos;
        }).sorted(Comparator.comparing(FaqListVos::getTypeId)).collect(Collectors.toList());
    }
    
    
    @Override
    public R queryDetail(Long id) {
        FaqVo faqVo = new FaqVo();
        FaqV2 faq = queryEntity(id);
        if (Objects.isNull(faq)) {
            return R.fail("此问题不存在");
        }
        BeanUtil.copyProperties(faq, faqVo);
        
        return R.ok(faqVo);
    }
    
    public FaqV2 queryEntity(Long id) {
        return faqV2Mapper.selectByPrimaryKey(id);
    }
    
    
    public Map<String, Object> beanToMap(AdminFaqQuery bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }
}
