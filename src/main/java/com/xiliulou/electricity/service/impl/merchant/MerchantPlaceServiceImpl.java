package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:46
 * @desc
 */
@Service("merchantPlaceService")
@Slf4j
public class MerchantPlaceServiceImpl implements MerchantPlaceService {
    @Resource
    private MerchantMapper merchantMapper;
}
