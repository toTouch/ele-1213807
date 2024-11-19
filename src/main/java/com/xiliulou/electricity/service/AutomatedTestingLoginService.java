/**
 *  Create date: 2024/7/4
 */

package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.merchant.AutomatedTestingLoginRequest;
import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/4 17:43
 */
public interface AutomatedTestingLoginService {
    
    
    Triple<Boolean, String, Object> login(HttpServletRequest request, AutomatedTestingLoginRequest loginRequest);
}
