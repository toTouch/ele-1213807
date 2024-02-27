package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.electricity.entity.faq.FaqCategory;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.faq.FaqCategoryMapper;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryService;
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
public class FaqCategoryServiceImpl implements FaqCategoryService {
    
    private final FaqCategoryMapper faqCategoryMapper;
    
    @Override
    public void add(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategory faqCategory = BeanUtil.toBean(faqCategoryReq, FaqCategory.class);
        faqCategory.setTenantId(TenantContextHolder.getTenantId()).setOpUser(SecurityUtils.getUid()).setCreateTime(System.currentTimeMillis())
                .setUpdateTime(System.currentTimeMillis());
        faqCategoryMapper.insert(faqCategory);
    }
    
    @Override
    public void edit(AdminFaqCategoryReq faqCategoryReq) {
        FaqCategory faqCategory = this.queryEntity(faqCategoryReq.getId());
        BeanUtil.copyProperties(faqCategoryReq, faqCategory);
        faqCategory.setOpUser(SecurityUtils.getUid()).setUpdateTime(System.currentTimeMillis());
        faqCategoryMapper.updateByPrimaryKeySelective(faqCategory);
    }
    
    @Override
    public List<FaqCategoryVo> page() {
        return faqCategoryMapper.selectListByTenantId(TenantContextHolder.getTenantId());
    }
    
    public FaqCategory queryEntity(Long id) {
        FaqCategory faqCategory = faqCategoryMapper.selectByPrimaryKey(id);
        if (null == faqCategory) {
            throw new BizException("300000", "数据有误");
        }
        return faqCategory;
    }
}
