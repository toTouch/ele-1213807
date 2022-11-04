package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜保险用户绑定(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("insuranceOrderService")
@Slf4j
public class InsuranceOrderServiceImpl extends ServiceImpl<InsuranceOrderMapper, InsuranceOrder> implements InsuranceOrderService {

    @Resource
    InsuranceOrderMapper insuranceOrderMapper;


    @Override
    public R queryList(InsuranceOrderQuery insuranceOrderQuery) {

        List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderMapper.queryList(insuranceOrderQuery);
        if (ObjectUtil.isEmpty(insuranceOrderVOList)) {
            return R.ok(new ArrayList<>());
        }

        insuranceOrderVOList.parallelStream().forEach(e -> {
            Integer validDays = e.getValidDays();
            Long insuranceExpireTime = validDays * (24 * 60 * 60 * 1000L);
            e.setInsuranceExpireTime(insuranceExpireTime);
        });

        return R.ok(insuranceOrderVOList);
    }

    @Override
    public R queryCount(InsuranceOrderQuery insuranceOrderQuery) {
        return R.ok(insuranceOrderMapper.queryCount(insuranceOrderQuery));
    }
}
