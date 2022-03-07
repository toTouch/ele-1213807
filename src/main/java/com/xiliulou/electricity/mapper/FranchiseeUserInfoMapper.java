package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * 用户绑定列表(FranchiseeUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
public interface FranchiseeUserInfoMapper  extends BaseMapper<FranchiseeUserInfo>{

    Integer unBind(FranchiseeUserInfo franchiseeUserInfo);

	@Update("update t_franchisee_user_info set remaining_number=remaining_number-1 where id=#{id} and remaining_number>0 and del_flag=0")
	Integer minCount(Long id);

	@Update("update t_franchisee_user_info set remaining_number=remaining_number-1 where id=#{id} and del_flag=0")
	Integer minMemberCountForOffLineEle(Long id);

	@Update("update t_franchisee_user_info set remaining_number=remaining_number+1 where id=#{id} and del_flag=0")
	Integer plusCount(Long id);

	void updateRefund(FranchiseeUserInfo franchiseeUserInfo);

	void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo);

	void updateByOrder(FranchiseeUserInfo franchiseeUserInfo);

	void updateOrderByUserInfoId(FranchiseeUserInfo franchiseeUserInfo);
}
