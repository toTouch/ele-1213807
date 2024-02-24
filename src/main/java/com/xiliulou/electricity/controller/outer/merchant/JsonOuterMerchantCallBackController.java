package com.xiliulou.electricity.controller.outer.merchant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: Ant
 * @className: JsonOuterMerchantCallBackController
 * @description:
 **/
@Slf4j
@RestController
@RequestMapping("/outer/merchant/notify")
public class JsonOuterMerchantCallBackController {
    
    /**
     * 宝付开户回调
     * @param tenantId 租户ID
     * @param httpServletRequest HttpServletRequest
     * @return 返回数据
     */
    @PostMapping("/openAccount/{tenantId}")
    public String openAccountNotify(@PathVariable("tenantId") Long tenantId, HttpServletRequest httpServletRequest) {
        String memberId = httpServletRequest.getParameter("member_id");
        String terminalId = httpServletRequest.getParameter("terminal_id");
        String dataContent = httpServletRequest.getParameter("data_content");
        return "OK";
    }
}
