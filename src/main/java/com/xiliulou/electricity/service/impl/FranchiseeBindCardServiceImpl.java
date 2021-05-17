package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeBindCard;
import com.xiliulou.electricity.mapper.FranchiseeBindCardMapper;
import com.xiliulou.electricity.query.FranchiseeBindCardBindQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeBindCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 加盟商套餐绑定表(TFranchiseeBindCard)表服务实现类
 *
 * @author makejava
 * @since 2021-04-16 15:12:51
 */
@Service("tFranchiseeBindCardService")
@Slf4j
public class FranchiseeBindCardServiceImpl implements FranchiseeBindCardService {
    @Resource
    private FranchiseeBindCardMapper franchiseeBindCardMapper;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FranchiseeBindCard queryByIdFromDB(Long id) {
        return this.franchiseeBindCardMapper.selectById(id);
    }


    /**
     * 新增数据
     *
     * @param franchiseeBindCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FranchiseeBindCard insert(FranchiseeBindCard franchiseeBindCard) {
        this.franchiseeBindCardMapper.insert(franchiseeBindCard);
        return franchiseeBindCard;
    }

    /**
     * 修改数据
     *
     * @param franchiseeBindCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FranchiseeBindCard franchiseeBindCard) {
       return this.franchiseeBindCardMapper.updateById(franchiseeBindCard);

    }

    @Override
    public R bindCard(FranchiseeBindCardBindQuery franchiseeBindCardBindQuery) {

        Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeBindCardBindQuery.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("bind Card  ERROR! not found Franchisee ! FranchiseeId:{} ",franchiseeBindCardBindQuery.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }


        //先删除
        franchiseeBindCardMapper.delete(new LambdaQueryWrapper<FranchiseeBindCard>()
                .eq(FranchiseeBindCard::getFranchiseeId, franchiseeBindCardBindQuery.getFranchiseeId()));

        List<Integer> cardIdList=franchiseeBindCardBindQuery.getCardIdList();
        if(ObjectUtil.isEmpty(cardIdList)){
            return R.ok();
        }


        //再重新绑定
        FranchiseeBindCard franchiseeBindCard=new FranchiseeBindCard();
        franchiseeBindCard.setFranchiseeId(franchiseeBindCardBindQuery.getFranchiseeId());
        for (Integer cardId:cardIdList) {
            franchiseeBindCard.setCardId(cardId);
            franchiseeBindCardMapper.insert(franchiseeBindCard);
        }

        return R.ok();
    }


    @Override
    public R queryBindCard(Integer id) {
        List<FranchiseeBindCard> franchiseeBindCardList=franchiseeBindCardMapper.selectList(new LambdaQueryWrapper<FranchiseeBindCard>()
                .eq(FranchiseeBindCard::getFranchiseeId, id));
        if(ObjectUtil.isEmpty(franchiseeBindCardList)){
            return null;
        }


        List<ElectricityMemberCard> electricityMemberCardList=new ArrayList<>();
        for (FranchiseeBindCard franchiseeBindCard:franchiseeBindCardList) {
            ElectricityMemberCard electricityMemberCard=electricityMemberCardService.getElectricityMemberCard(franchiseeBindCard.getCardId());
            if(Objects.nonNull(electricityMemberCard)){
                electricityMemberCardList.add(electricityMemberCard);
            }
        }
        return R.ok(electricityMemberCardList);
    }

    @Override
    public List<FranchiseeBindCard> queryByFranchisee(Integer id) {
        return franchiseeBindCardMapper.selectList(new LambdaQueryWrapper<FranchiseeBindCard>()
                .eq(FranchiseeBindCard::getFranchiseeId, id));
    }
}
