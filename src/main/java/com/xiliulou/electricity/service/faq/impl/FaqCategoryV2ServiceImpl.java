package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.electricity.entity.faq.FaqCategoryV2;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqCategoryVo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void add(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategoryV2 faqCategory = BeanUtil.toBean(faqCategoryReq, FaqCategoryV2.class);
        faqCategory.setTenantId(TenantContextHolder.getTenantId()).setOpUser(SecurityUtils.getUid()).setCreateTime(System.currentTimeMillis())
                .setUpdateTime(System.currentTimeMillis());
        faqCategoryV2Mapper.insert(faqCategory);
    }
    
    @Override
    public void edit(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategoryV2 faqCategory = this.queryEntity(faqCategoryReq.getId());
        BeanUtil.copyProperties(faqCategoryReq, faqCategory);
        faqCategory.setOpUser(SecurityUtils.getUid()).setUpdateTime(System.currentTimeMillis());
        faqCategoryV2Mapper.updateByPrimaryKeySelective(faqCategory);
    }
    
    @Override
    public List<FaqCategoryVo> page() {
        return faqCategoryV2Mapper.selectListByTenantId(TenantContextHolder.getTenantId());
    }
    
    public FaqCategoryV2 queryEntity(Long id) {
        FaqCategoryV2 faqCategory = faqCategoryV2Mapper.selectByPrimaryKey(id);
        if (null == faqCategory) {
            throw new BizException("300000", "数据有误");
        }
        return faqCategory;
    }
}
