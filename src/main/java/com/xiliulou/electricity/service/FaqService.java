package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Faq;
import com.xiliulou.electricity.query.FaqQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (Faq)表服务接口
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface FaqService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Faq queryByIdFromDB(Integer id,Integer tenantId);



    /**
     * 新增数据
     *
     * @param faq 实例对象
     * @return 实例对象
     */
    Faq insert(Faq faq);

    /**
     * 修改数据
     *
     * @param faq 实例对象
     * @return 实例对象
     */
    Integer update(Faq faq);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id,Integer tenantId);

    Triple<Boolean, String, Object> queryList(Integer size, Integer offset);

    Triple<Boolean, String, Object> addFaq(FaqQuery faqQuery);

    Triple<Boolean, String, Object> updateFaq(FaqQuery faqQuery);

    Triple<Boolean, String, Object> delete(Integer id);

    Triple<Boolean, String, Object> queryCount();
}
