package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.mapper.RentBatteryOrderMapper;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.utils.PageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 租电池记录(TRentBatteryOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
@Service("tRentBatteryOrderService")
public class RentBatteryOrderServiceImpl implements RentBatteryOrderService {
    @Resource
    private RentBatteryOrderMapper rentBatteryOrderMapper;


    /**
     * 新增数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentBatteryOrder insert(RentBatteryOrder rentBatteryOrder) {
        this.rentBatteryOrderMapper.insert(rentBatteryOrder);
        return rentBatteryOrder;
    }


    @Override
    public R queryList(RentBatteryOrderQuery rentBatteryOrderQuery) {
        Page page = PageUtil.getPage(rentBatteryOrderQuery.getOffset(), rentBatteryOrderQuery.getSize());

        return R.ok(rentBatteryOrderMapper.queryList(page, rentBatteryOrderQuery));
    }
}