package com.xiliulou.electricity.service.impl.merchant;


import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawOldConfigInfo;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawOldConfigInfoMapper;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawOldConfigInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * 商户提现使用旧配置信息表(TMerchantWithdrawOldConfigInfo)表服务实现类
 *
 * @author maxiaodong
 * @since 2025-02-13 17:44:25
 */
@Service("merchantWithdrawOldConfigInfoService")
@Slf4j
public class MerchantWithdrawOldConfigInfoServiceImpl implements MerchantWithdrawOldConfigInfoService {
    @Resource
    private MerchantWithdrawOldConfigInfoMapper merchantWithdrawOldConfigInfoMapper;

    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;

    @Override
    @Slave
    public boolean existsMerchantOldWithdrawConfigInfo(Integer tenantId, Long franchiseeId) {
        Integer count = merchantWithdrawOldConfigInfoMapper.existsMerchantOldWithdrawConfigInfo(tenantId, franchiseeId);
        if (Objects.nonNull(count)) {
            return true;
        }

        return false;
    }
}
