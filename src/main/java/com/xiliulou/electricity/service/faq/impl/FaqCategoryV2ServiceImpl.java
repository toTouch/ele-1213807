package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.faq.FaqV2BO;
import com.xiliulou.electricity.config.InitFaqProperties;
import com.xiliulou.electricity.entity.faq.FaqCategoryV2;
import com.xiliulou.electricity.entity.faq.FaqV2;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqCategoryVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 常见问题分类Service接口实现类
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Service
@AllArgsConstructor
@Slf4j
public class FaqCategoryV2ServiceImpl implements FaqCategoryV2Service {
    
    private final FaqCategoryV2Mapper faqCategoryV2Mapper;
    
    @Resource
    private InitFaqProperties initFaqProperties;
    
    @Resource
    private FaqV2Service faqV2Service;
    
    @Override
    public void saveFaqCategory(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategoryV2 faqCategory = BeanUtil.toBean(faqCategoryReq, FaqCategoryV2.class);
        Integer count = faqCategoryV2Mapper.countFaqCategoryByTenantId(TenantContextHolder.getTenantId());
        
        faqCategory.setSort(BigDecimal.valueOf(count + 1));
        faqCategory.setTenantId(TenantContextHolder.getTenantId());
        faqCategory.setOpUser(SecurityUtils.getUid());
        faqCategory.setCreateTime(System.currentTimeMillis());
        faqCategory.setUpdateTime(System.currentTimeMillis());
        
        faqCategoryV2Mapper.insert(faqCategory);
    }
    
    @Override
    public void editFaqCategory(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategoryV2 faqCategory = new FaqCategoryV2();
        BeanUtil.copyProperties(faqCategoryReq, faqCategory);
        faqCategory.setOpUser(SecurityUtils.getUid());
        faqCategory.setUpdateTime(System.currentTimeMillis());
        
        faqCategoryV2Mapper.updateByPrimaryKeySelective(faqCategory);
    }
    
    @Override
    public List<FaqCategoryVo> listFaqCategory(String title, Integer typeId) {
        List<FaqV2BO> faqBos = faqCategoryV2Mapper.selectLeftJoinByParams(TenantContextHolder.getTenantId(), title, typeId);
        
        if (CollectionUtil.isEmpty(faqBos)) {
            return null;
        }
        
        Map<Long, List<FaqV2BO>> collect = faqBos.parallelStream().collect(Collectors.groupingBy(FaqV2BO::getTypeId));
        
        return collect.entrySet().parallelStream().map(faqCategory -> {
            log.info("faqCategory.getValue().size():{}", faqCategory.getValue().size());
            FaqCategoryVo faqCategoryVo = new FaqCategoryVo();
            faqCategoryVo.setId(faqCategory.getKey());
            faqCategoryVo.setType(faqCategory.getValue().get(0).getType());
            faqCategoryVo.setSort(faqCategory.getValue().get(0).getTypeSort());
            int count = faqCategory.getValue().get(0).getId() == null ? 0 : faqCategory.getValue().size();
            faqCategoryVo.setCount(count);
            BeanUtil.copyProperties(faqCategory, faqCategoryVo);
            return faqCategoryVo;
        }).sorted(Comparator.comparing(FaqCategoryVo::getSort)).collect(Collectors.toList());
    }
    
    @Override
    public long listFaqCategoryCount(String title) {
        List<FaqV2BO> faqBos = faqCategoryV2Mapper.selectLeftJoinByParams(TenantContextHolder.getTenantId(), title, null);
        return CollectionUtil.isEmpty(faqBos) ? 0 : faqBos.stream().filter(f -> f.getId() != null).count();
    }
    
    @Override
    public void initFaqByTenantId(Integer tenantId) {
        List<InitFaqProperties.Category> categoryList = initFaqProperties.getCategory();
        if (CollectionUtils.isEmpty(categoryList)) {
            log.warn("InitFaqByTenantId warn! categoryList is empty!");
            return;
        }
        
        Map<String, List<InitFaqProperties.Category.Problem>> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(InitFaqProperties.Category::getType, InitFaqProperties.Category::getProblem));
        if (MapUtils.isEmpty(categoryMap)) {
            log.warn("InitFaqByTenantId warn! categoryMap is empty!");
            return;
        }
        
        List<FaqCategoryV2> faqCategoryInsertList = this.buildFaqCategoryList(categoryMap.keySet(), tenantId);
        if (CollectionUtils.isNotEmpty(faqCategoryInsertList)) {
            faqCategoryV2Mapper.batchInsert(faqCategoryInsertList);
        }
        
        List<FaqCategoryV2> faqCategoryList = this.listByTenantId(tenantId);
        if (CollectionUtils.isEmpty(faqCategoryList)) {
            log.warn("InitFaqByTenantId warn! faqCategoryList is empty!");
            return;
        }
        
        Map<String, Long> typeMap = faqCategoryList.stream().collect(Collectors.toMap(FaqCategoryV2::getType, FaqCategoryV2::getId));
        if (MapUtils.isEmpty(typeMap)) {
            log.warn("InitFaqByTenantId warn! typeMap is empty!");
            return;
        }
        
        List<FaqV2> faqList = this.buildFaqV2List(categoryMap, typeMap, tenantId);
        if (CollectionUtils.isNotEmpty(faqList)) {
            faqV2Service.batchInsert(faqList);
        }
        
    }
    
    private List<FaqCategoryV2> buildFaqCategoryList(Set<String> typeSet, Integer tenantId) {
        final BigDecimal[] sort = {BigDecimal.ZERO};
        
        return typeSet.stream().map(type -> {
            FaqCategoryV2 faqCategory = new FaqCategoryV2();
            faqCategory.setType(type);
            faqCategory.setSort(sort[0]);
            faqCategory.setTenantId(tenantId);
            faqCategory.setOpUser(SecurityUtils.getUid());
            faqCategory.setCreateTime(System.currentTimeMillis());
            faqCategory.setUpdateTime(System.currentTimeMillis());
            
            sort[0] = sort[0].add(BigDecimal.ONE);
            
            return faqCategory;
        }).collect(Collectors.toList());
    }
    
    private List<FaqV2> buildFaqV2List(Map<String, List<InitFaqProperties.Category.Problem>> categoryMap, Map<String, Long> typeMap, Integer tenantId) {
        BigDecimal[] sort = {BigDecimal.ZERO};
        List<FaqV2> list = new ArrayList<>();
        
        for (Map.Entry<String, List<InitFaqProperties.Category.Problem>> entry : categoryMap.entrySet()) {
            String type = entry.getKey();
            List<InitFaqProperties.Category.Problem> problemList = entry.getValue();
            if (CollectionUtils.isEmpty(problemList)) {
                continue;
            }
            
            for (InitFaqProperties.Category.Problem problem : problemList) {
                if (!typeMap.containsKey(type)) {
                    continue;
                }
                
                FaqV2 faq = new FaqV2();
                faq.setTypeId(typeMap.get(type));
                faq.setTitle(problem.getTitle());
                faq.setOnShelf(FaqV2.SHELF_TYPE);
                faq.setSort(sort[0]);
                faq.setTenantId(tenantId);
                faq.setOpUser(SecurityUtils.getUid());
                faq.setCreateTime(System.currentTimeMillis());
                faq.setUpdateTime(System.currentTimeMillis());
                
                sort[0] = sort[0].add(BigDecimal.ONE);
                list.add(faq);
            }
        }
        
        return list;
    }
    
    @Slave
    @Override
    public List<FaqCategoryV2> listByTenantId(Integer tenantId) {
        return faqCategoryV2Mapper.selectListByTenantId(tenantId);
    }
    
}
