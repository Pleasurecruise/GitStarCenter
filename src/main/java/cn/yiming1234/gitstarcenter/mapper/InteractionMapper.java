package cn.yiming1234.gitstarcenter.mapper;

import cn.yiming1234.gitstarcenter.entity.Interaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;
import java.util.List;

public interface InteractionMapper extends BaseMapper<Interaction> {
    List<Interaction> selectBySourceUserId(Integer sourceUserId);
    List<Interaction> selectByTargetUserId(Integer targetUserId);
    Interaction selectBySourceUserIdAndTargetUserId(int sourceUserId, int targetUserId);

}