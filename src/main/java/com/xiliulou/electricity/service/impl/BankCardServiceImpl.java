package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BankNoConstants;
import com.xiliulou.electricity.entity.BankCard;
import com.xiliulou.electricity.mapper.BankCardMapper;
import com.xiliulou.electricity.query.BankCardQuery;
import com.xiliulou.electricity.service.BankCardService;
import com.xiliulou.electricity.utils.DesensitizationUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BankCardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service("bankCardService")
public class BankCardServiceImpl extends ServiceImpl<BankCardMapper, BankCard> implements BankCardService {

	@Override
	public R bindBank(BankCard bankCard) {

		String fullName = BankNoConstants.BankNoMap.get(bankCard.getEncBankCode());
		if (!Objects.equals(fullName,bankCard.getFullName())) {
			log.error("bankCode is not equal fullName! bankCode:{},fullName:{}",bankCard.getEncBankCode(),bankCard.getFullName());
			return R.fail("PAY_TRANSFER.0001", "银行名称和银行编号不匹配");
		}


		List<BankCard> bankCardList = baseMapper.selectList(Wrappers.<BankCard>lambdaQuery()
				.eq(BankCard::getUid, bankCard.getUid()).eq(BankCard::getDelFlag,BankCard.DEL_NORMAL));
		if (ObjectUtil.isNotEmpty(bankCardList)) {
			BankCard oldBankCard=bankCardList.get(0);
			if(!Objects.equals(oldBankCard.getEncBindUserName(),bankCard.getEncBindUserName())||
					!Objects.equals(oldBankCard.getEncBindIdNumber(),bankCard.getEncBindIdNumber())){
				log.error("trueName or idNumber  is not equal ! oldTrueName:{},oldIdNumber:{},newTrueName:{},newIdNumber:{}"
						,oldBankCard.getEncBindUserName(),oldBankCard.getEncBindIdNumber(),bankCard.getEncBindUserName(),bankCard.getEncBindIdNumber());
				return R.fail("PAY_TRANSFER.0002", "开户人姓名或身份证号与本人不符");
			}
		}


		Integer count=baseMapper.selectCount(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getEncBankNo, bankCard.getEncBankNo()).eq(BankCard::getDelFlag,BankCard.DEL_NORMAL));
		if(count>0){
			log.error("bankNo is bind fullName! bankNo:{}",bankCard.getEncBankNo());
			return R.fail("PAY_TRANSFER.0003", "该银行卡号已被绑定");
		}

		bankCard.setFullName(fullName);
		bankCard.setUid(SecurityUtils.getUid());
		bankCard.setCreateTime(System.currentTimeMillis());
		bankCard.setUpdateTime(System.currentTimeMillis());
		baseMapper.insert(bankCard);
		return R.ok();

	}

	@Override
	public R unBindBank(Integer id) {
		BankCard bankCard = baseMapper.selectById(id);

		if (Objects.isNull(bankCard)) {
			log.error("not found bank By id ,id:{}", id);
			return R.fail("PAY_TRANSFER.0004", "未找到银行卡");
		}
		if (ObjectUtil.notEqual(SecurityUtils.getUid(), bankCard.getUid())) {
			log.error("bank bind uid  is no matched  operate uid, id:{},bindUid:{},operateUserId:{}", id, bankCard.getUid(), SecurityUtils.getUid());
			return R.fail("PAY_TRANSFER.0005", "不能解绑别人银行卡");
		}
		bankCard.setDelFlag(BankCard.DEL_DEL);
		bankCard.setUpdateTime(System.currentTimeMillis());
		baseMapper.updateById(bankCard);
		return R.ok();
	}



	@Override
	public R queryList(BankCardQuery bankCardQuery) {
		List<BankCard> bankCardList=baseMapper.queryList(bankCardQuery);
		List<BankCardVO> bankCardVOList=new ArrayList<>();
		//脱敏
		for (BankCard bankCard : bankCardList) {
			BankCardVO bankCardVO=new BankCardVO();
			BeanUtil.copyProperties(bankCard,bankCardVO);
			bankCardVO.setBankNo(DesensitizationUtil.bankCard(bankCard.getEncBankNo()));
			bankCard.setEncBindIdNumber(DesensitizationUtil.idCard(bankCard.getEncBindIdNumber()));
			bankCardVOList.add(bankCardVO);
		}
		return R.ok(bankCardVOList);
	}

	@Override
	public R queryCount(BankCardQuery bankCardQuery) {
		return R.ok(baseMapper.queryCount(bankCardQuery));
	}

	@Override
	public R unBindByWeb(Integer id) {
		BankCard bankCard = baseMapper.selectById(id);
		if (Objects.isNull(bankCard)) {
			log.error("not found bank By id ,id:{}", id);
			return R.fail("PAY_TRANSFER.0004", "未找到银行卡");
		}
		bankCard.setDelFlag(BankCard.DEL_DEL);
		bankCard.setUpdateTime(System.currentTimeMillis());
		baseMapper.updateById(bankCard);
		return R.ok();
	}

	@Override
	public BankCard queryByUidAndDel(Long uid) {
		List<BankCard> bankCardList=baseMapper.selectList(new LambdaQueryWrapper<BankCard>().eq(BankCard::getUid,uid).eq(BankCard::getDelFlag,BankCard.DEL_NORMAL));
		if(ObjectUtil.isEmpty(bankCardList)){
			return null;
		}
		return bankCardList.get(0);
	}

	@Override
	public BankCard queryByUid(Long uid) {
		List<BankCard> bankCardList=baseMapper.selectList(new LambdaQueryWrapper<BankCard>().eq(BankCard::getUid,uid).eq(BankCard::getDelFlag, BankCard.DEL_NORMAL));
		if(ObjectUtil.isEmpty(bankCardList)){
			return null;
		}
		return bankCardList.get(0);
	}

	@Override
	public BankCard queryByBankNo(String bankNumber) {
		List<BankCard> bankCardList=baseMapper.selectList(
				new LambdaQueryWrapper<BankCard>()
						.eq(BankCard::getEncBankNo, bankNumber)
						.eq(BankCard::getDelFlag, BankCard.DEL_NORMAL));

		if(ObjectUtil.isEmpty(bankCardList)){
			return null;
		}
		return bankCardList.get(0);
	}

}
