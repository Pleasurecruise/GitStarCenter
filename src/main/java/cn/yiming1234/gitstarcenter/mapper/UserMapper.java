package cn.yiming1234.gitstarcenter.mapper;

import cn.yiming1234.gitstarcenter.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectStarredRepositoriesBySourceUserIds(@Param("sourceUserIds") List<Integer> sourceUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectForkedRepositoriesBySourceUserIds(@Param("sourceUserIds") List<Integer> sourceUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectWatchedRepositoriesBySourceUserIds(@Param("sourceUserIds") List<Integer> sourceUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectFollowedRepositoriesBySourceUserIds(@Param("sourceUserIds") List<Integer> sourceUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectStarredRepositoriesByTargetUserIds(@Param("targetUserIds") List<Integer> targetUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectForkedRepositoriesByTargetUserIds(@Param("targetUserIds") List<Integer> targetUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectWatchedRepositoriesByTargetUserIds(@Param("targetUserIds") List<Integer> targetUserIds, @Param("objectPage") Page<Object> objectPage);
    Page<cn.yiming1234.gitstarcenter.entity.Repository> selectFollowedRepositoriesByTargetUserIds(@Param("targetUserIds") List<Integer> targetUserIds, @Param("objectPage") Page<Object> objectPage);
}