package com.xiliulou.electricity.service.faq;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.faq.FaqV2;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.reqparam.faq.AdminFaqChangeTypeReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqUpDownReq;
import com.xiliulou.electricity.vo.faq.FaqVo;

import java.util.List;

/**
 * 常见问题Service接口
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
public interface FaqV2Service {
    
    /**
     * 添加常见问题
     *
     * @return
     * @author kuz
     * @date 2024/2/23 16:11
     */
    R saveFaqQuery(AdminFaqReq faqReq);
    
    /**
     * 更改常见问题分类
     *
     * @return
     * @author kuz
     * @date 2024/2/23 16:11
     */
    R updateFaqReq(AdminFaqReq faqReq);
    
    /**
     * 删除常见问题分类，并且删除下面的所有问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    void removeByCategoryId(Long id);
    
    void upDownBatch(AdminFaqUpDownReq faqUpDownReq);
    
    R changeTypeBatch(AdminFaqChangeTypeReq faqChangeTypeReq);
    
    void removeByIds(List<Long> ids);
    
    R queryDetail(Long id);
    
    List<FaqVo> listFaqQueryResult(AdminFaqQuery faqQuery);
    
    List<FaqVo> listFaqQueryToUser(AdminFaqQuery faqQuery);
    
    R updateFaqReqSort(AdminFaqReq faqReq);
    
    Integer batchInsert(List<FaqV2> list);
}
