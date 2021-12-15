package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BankCard;
import com.xiliulou.electricity.query.BankCardQuery;

/**
 *
 *
 * @author Hardy
 * @email ${email}
 * @date 2021-05-24 13:58:23
 */
public interface BankCardService extends IService<BankCard> {

    R bindBank(BankCard bankCard);

    R unBindBank(Integer id);

    R queryList(BankCardQuery bankCardQuery);

    R queryCount(BankCardQuery bankCardQuery);

    R unBindByWeb(Integer id);

	BankCard queryByUidAndDel(Long uid);

    BankCard queryByUid(Long uid);

    BankCard queryByBankNo(String bankNumber);
}

