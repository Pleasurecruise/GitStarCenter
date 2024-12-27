package cn.yiming1234.gitstarcenter.mapper;

import cn.yiming1234.gitstarcenter.entity.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface RepositoryMapper extends BaseMapper<Repository> {
    Repository selectByUserId(int userId);

    Repository selectByRepoAuthAndRepoName(String repoAuth, String repoName);
}