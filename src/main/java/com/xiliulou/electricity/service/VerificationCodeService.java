package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.VerificationCode;
import com.xiliulou.electricity.query.VerificationCodeQuery;

import java.util.List;

/**
 * 动态验证码(VerificationCode)表服务接口
 *
 * @author zzlong
 * @since 2022-06-28 11:07:36
 */
public interface VerificationCodeService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    VerificationCode queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    VerificationCode queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<VerificationCode> queryAllByLimit(VerificationCodeQuery codeQuery);

    int selectCountByQuery(VerificationCodeQuery codeQuery);

    /**
     * 新增数据
     *
     * @param verificationCode 实例对象
     * @return 实例对象
     */
    VerificationCode insert(VerificationCode verificationCode);

    /**
     * 修改数据
     *
     * @param verificationCode 实例对象
     * @return 实例对象
     */
    Integer update(VerificationCode verificationCode);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    /**
     * 生成验证码
     * @param entity
     * @return
     */
    R generationCode(VerificationCodeQuery entity);

    /**
     * 逻辑删除
     * @param id
     * @return
     */
    R deleteVerificationCode(Long id);

    R checkVerificationCode(String verificationCode);

}
