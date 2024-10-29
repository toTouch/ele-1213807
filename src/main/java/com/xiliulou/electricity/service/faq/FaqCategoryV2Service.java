package com.xiliulou.electricity.service.faq;

import com.xiliulou.electricity.entity.faq.FaqCategoryV2;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.vo.faq.FaqCategoryVo;

import java.util.List;

/**
 * 常见问题分类Service接口
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
public interface FaqCategoryV2Service {
    
    /**
     * 添加常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    void saveFaqCategory(AdminFaqCategoryReq faqCategoryAddParam);
    
    /**
     * 更新常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    void editFaqCategory(AdminFaqCategoryReq faqCategoryReq);
    
    List<FaqCategoryVo> listFaqCategory(String title, Integer typeId);
    
    long listFaqCategoryCount(String title);
    
    void initFaqByTenantId(Integer tenantId);
    
    List<FaqCategoryV2> listByTenantId(Integer tenantId);
}
