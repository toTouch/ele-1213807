package com.xiliulou.electricity.utils;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2021-01-14 09:53
 **/
public class PageUtil {


    public static Page getPage(Long offset, Long size) {
        Page page = new Page();
        page.setCurrent(ObjectUtil.equal(0, offset) ? 1L
                : new Double(Math.ceil(Double.parseDouble(String.valueOf(offset)) / size)).longValue());
        page.setSize(size);
        return page;
    }

}
