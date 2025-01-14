package cn.yiming1234.gitstarcenter.service;

import cn.yiming1234.gitstarcenter.entity.Repository;
import cn.yiming1234.gitstarcenter.vo.RepositoryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface RepositoryService {
    Repository getRepository(String repoAuth, String repoName);
    Page<RepositoryVO> getRepositories(int page, int size);
    Page<RepositoryVO> getRepositoriesByLanguage(int page, int size, String language);
    boolean isRepositoryValid(String repoAuth, String repoName);
    boolean isRepositoryExist(String repoAuth, String repoName);
    boolean checkRepositoryBinding(int id);
    String getRepositoryLanguage(String repoAuth, String repoName);
    String getRepositoryBio(String repoAuth, String repoName);
    int getStarCount(String repositoryAuth, String repositoryName);
    int getForkCount(String repositoryAuth, String repositoryName);
    int getWatchCount(String repositoryAuth, String repositoryName);
}
