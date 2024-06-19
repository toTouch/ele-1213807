package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : eclair
 * @date : 2024/2/18 10:19
 */
public interface MerchantTokenService {
    
    Triple<Boolean, String, Object> login(HttpServletRequest request, MerchantLoginRequest merchantLoginRequest);
    
}
