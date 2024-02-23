package com.xiliulou.electricity.service.fqa.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.fqa.FqaCategory;
import com.xiliulou.electricity.mapper.fqa.FqaCategoryMapper;
import com.xiliulou.electricity.reqparam.fqa.AdminFqaCategoryAddParam;
import com.xiliulou.electricity.service.fqa.FqaCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 常见问题分类Service接口实现类
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Service
public class FqaCategoryServiceImpl extends ServiceImpl<FqaCategoryMapper, FqaCategory> implements FqaCategoryService {
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(AdminFqaCategoryAddParam fqaCategoryAddParam) {
        FqaCategory fqaCategory = BeanUtil.toBean(fqaCategoryAddParam, FqaCategory.class);
        this.save(fqaCategory);
    }
}
