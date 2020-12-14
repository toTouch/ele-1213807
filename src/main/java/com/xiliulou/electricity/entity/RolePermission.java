package com.xiliulou.electricity.entity;


    



                                    
                                            
                                            
                    
                                    
                                            
                                            
                    

            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * (RolePermission)实体类
 *
 * @author Eclair
 * @since 2020-12-09 14:36:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_role_permission")
public class RolePermission {
    
    private Long id;
    
    private Long roleId;
    
    private Long pId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}