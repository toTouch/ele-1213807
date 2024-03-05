package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.electricity.bo.faq.FaqV2BO;
import com.xiliulou.electricity.entity.faq.FaqCategoryV2;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.mapper.faq.FaqV2Mapper;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqCategoryVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private FaqV2Mapper faqV2Mapper;
    
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
        List<FaqV2BO> faqBos = faqCategoryV2Mapper.selectLeftJoinByParams(TenantContextHolder.getTenantId(), title,typeId);
        
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
}
