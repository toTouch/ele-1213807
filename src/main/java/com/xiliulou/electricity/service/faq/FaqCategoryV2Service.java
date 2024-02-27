package com.xiliulou.electricity.service.faq;

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
    void add(AdminFaqCategoryReq faqCategoryAddParam);
    
    /**
     * 更新常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    void edit(AdminFaqCategoryReq faqCategoryReq);
    
    List<FaqCategoryVo> page();
}
