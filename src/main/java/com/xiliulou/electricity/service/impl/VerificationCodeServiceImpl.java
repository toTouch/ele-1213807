package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.VerificationCode;
import com.xiliulou.electricity.mapper.VerificationCodeMapper;
import com.xiliulou.electricity.query.VerificationCodeQuery;
import com.xiliulou.electricity.service.VerificationCodeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * 动态验证码(VerificationCode)表服务实现类
 *
 * @author zzlong
 * @since 2022-06-28 11:07:36
 */
@Service("verificationCodeService")
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Resource
    private VerificationCodeMapper verificationCodeMapper;


    @Override
    public R generationCode(VerificationCodeQuery entity) {
        if (!Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
            return R.fail("权限不足");
        }

        String code = RandomUtil.randomString(6);

        VerificationCode verificationCode = VerificationCode.builder()
                .verificationCode(code)
                .userName(entity.getUserName())
                .phone(entity.getPhone())
                .status(VerificationCode.STATUE_ENABLE)
                .delFlag(VerificationCode.DEL_NORMAL)
                .createdTime(System.currentTimeMillis()).build();

        if (verificationCodeMapper.insert(verificationCode) > 0) {
            return R.ok("动态验证码生成成功！");
        }
        return R.fail("动态验证码生成失败！");
    }

    @Override
    public R deleteVerificationCode(Long id) {
        VerificationCode updateEntity = VerificationCode.builder()
                .id(id)
                .delFlag(VerificationCode.DEL_DEL)
                .updatedTime(System.currentTimeMillis()).build();

        if(verificationCodeMapper.update(updateEntity)>0){
            return R.ok("验证码删除成功！");
        }
        return R.failMsg("验证码删除失败！");
    }


    @Override
    public R checkVerificationCode(String verificationCode) {
        Integer count = verificationCodeMapper.selectCount(new LambdaQueryWrapper<VerificationCode>()
                .eq(VerificationCode::getVerificationCode, verificationCode)
                .eq(VerificationCode::getDelFlag, VerificationCode.DEL_NORMAL));

        if(count>0){
            return R.ok();
        }
        return R.failMsg("验证码不存在！");
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public VerificationCode queryByIdFromDB(Long id) {
        return this.verificationCodeMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public VerificationCode queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<VerificationCode> queryAllByLimit(VerificationCodeQuery codeQuery) {
        return this.verificationCodeMapper.queryAllByLimit(codeQuery);
    }

    @Override
    public int selectCountByQuery(VerificationCodeQuery codeQuery) {
        return this.verificationCodeMapper.selectCountByQuery(codeQuery);
    }

    /**
     * 新增数据
     *
     * @param verificationCode 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerificationCode insert(VerificationCode verificationCode) {
        this.verificationCodeMapper.insertOne(verificationCode);
        return verificationCode;
    }

    /**
     * 修改数据
     *
     * @param verificationCode 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(VerificationCode verificationCode) {
        return this.verificationCodeMapper.update(verificationCode);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.verificationCodeMapper.deleteById(id) > 0;
    }
}
