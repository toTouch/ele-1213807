package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.mapper.merchant.MerchantCabinetBindTimeMapper;
import com.xiliulou.electricity.service.merchant.MerchantCabinetBindTimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/2/26 18:45
 * @desc
 */
@Service("merchantCabinetBindTimeService")
@Slf4j
public class MerchantCabinetBindTimeServiceImpl implements MerchantCabinetBindTimeService {
    @Resource
    private MerchantCabinetBindTimeMapper merchantCabinetBindTimeMapper;
}
