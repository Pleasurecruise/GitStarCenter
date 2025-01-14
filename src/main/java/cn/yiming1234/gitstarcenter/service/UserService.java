package cn.yiming1234.gitstarcenter.service;

import cn.yiming1234.gitstarcenter.entity.Repository;
import cn.yiming1234.gitstarcenter.entity.User;
import cn.yiming1234.gitstarcenter.vo.RepositoryVO;
import cn.yiming1234.gitstarcenter.vo.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService {
    void updateRepository(Repository repository, String repoAuth, String repoName);
    UserVO getAccountInfo(String username);
    UserVO getTargetAccountInfo(String repoAuth, String repoName);
    void saveUserInfo(User user);
    void updateUser(User user);
    void bindRepository(String username, String repositoryAuth, String repositoryName);
    Page<RepositoryVO> getRepositoriesStarredByUser(String username, int page, int size);
    Page<RepositoryVO> getRepositoriesForkedByUser(String username, int page, int size);
    Page<RepositoryVO> getRepositoriesWatchedByUser(String username, int page, int size);
    Page<RepositoryVO> getRepositoriesFollowedByUser(String username, int page, int size);
    Page<RepositoryVO> getStarredRepositories(String username, int page, int size);
    Page<RepositoryVO> getForkedRepositories(String username, int page, int size);
    Page<RepositoryVO> getWatchedRepositories(String username, int page, int size);
    Page<RepositoryVO> getFollowedRepositories(String username, int page, int size);
}
