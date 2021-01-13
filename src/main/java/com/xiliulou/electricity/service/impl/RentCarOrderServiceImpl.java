package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.RentCarOrder;
import com.xiliulou.electricity.mapper.RentCarOrderMapper;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.service.RentCarOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 租车记录(TRentCarOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@Service("tRentCarOrderService")
public class RentCarOrderServiceImpl implements RentCarOrderService {
    @Resource
    private RentCarOrderMapper rentCarOrderMapper;

    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentCarOrder insert(RentCarOrder rentCarOrder) {
        this.rentCarOrderMapper.insert(rentCarOrder);
        return rentCarOrder;
    }

    @Override
    public R queryList(RentCarOrderQuery rentCarOrderQuery) {
        Page page = new Page();

        page.setCurrent(ObjectUtil.equal(0, rentCarOrderQuery.getOffset()) ? 1L
                : new Double(Math.ceil(Double.parseDouble(String.valueOf(rentCarOrderQuery.getOffset())) / rentCarOrderQuery.getSize())).longValue());
        page.setSize(rentCarOrderQuery.getSize());

        return R.ok( rentCarOrderMapper.queryList(page, rentCarOrderQuery));
    }
}