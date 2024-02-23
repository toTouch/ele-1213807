package com.xiliulou.electricity.service.fqa;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.electricity.entity.fqa.FqaCategory;
import com.xiliulou.electricity.reqparam.fqa.AdminFqaCategoryAddParam;

/**
 * 常见问题分类Service接口
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
public interface FqaCategoryService extends IService<FqaCategory> {
    
    /**
     * 添加常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    void add(AdminFqaCategoryAddParam fqaCategoryAddParam);
}
