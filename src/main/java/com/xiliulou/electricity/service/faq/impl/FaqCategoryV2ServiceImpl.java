package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.electricity.entity.faq.FaqCategoryV2;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqCategoryVo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 常见问题分类Service接口实现类
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Service
@AllArgsConstructor
public class FaqCategoryV2ServiceImpl implements FaqCategoryV2Service {
    
    private final FaqCategoryV2Mapper faqCategoryV2Mapper;
    
    @Override
    public void saveFaqCategory(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategoryV2 faqCategory = BeanUtil.toBean(faqCategoryReq, FaqCategoryV2.class);
        Integer count = faqCategoryV2Mapper.countFaqCategoryByTenantId(faqCategory);
        count = count == null ? 0 : count;
        
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
    public List<FaqCategoryVo> listFaqCategory() {
        List<FaqCategoryV2> faqCategories = faqCategoryV2Mapper.selectListByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faqCategories)) {
            return null;
        }
        
        return faqCategories.stream().map(faqCategory -> {
            FaqCategoryVo faqCategoryVo = new FaqCategoryVo();
            BeanUtil.copyProperties(faqCategory, faqCategoryVo);
            return faqCategoryVo;
        }).collect(Collectors.toList());
    }
}
