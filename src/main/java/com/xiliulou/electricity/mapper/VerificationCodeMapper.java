package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.VerificationCode;

import java.util.List;

import com.xiliulou.electricity.query.VerificationCodeQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 动态验证码(VerificationCode)表数据库访问层
 *
 * @author zzlong
 * @since 2022-06-28 11:07:36
 */
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    VerificationCode queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<VerificationCode> queryAllByLimit(VerificationCodeQuery codeQuery);

    int selectCountByQuery(VerificationCodeQuery codeQuery);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param verificationCode 实例对象
     * @return 对象列表
     */
    List<VerificationCode> queryAll(VerificationCode verificationCode);

    /**
     * 新增数据
     *
     * @param verificationCode 实例对象
     * @return 影响行数
     */
    int insertOne(VerificationCode verificationCode);

    /**
     * 修改数据
     *
     * @param verificationCode 实例对象
     * @return 影响行数
     */
    int update(VerificationCode verificationCode);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);


}
