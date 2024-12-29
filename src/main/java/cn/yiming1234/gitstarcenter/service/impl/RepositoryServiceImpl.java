package cn.yiming1234.gitstarcenter.service.impl;

import cn.yiming1234.gitstarcenter.constant.MessageConstant;
import cn.yiming1234.gitstarcenter.entity.Repository;
import cn.yiming1234.gitstarcenter.mapper.RepositoryMapper;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.service.RepositoryService;
import cn.yiming1234.gitstarcenter.vo.RepositoryVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryMapper repositoryMapper;
    private final UserMapper userMapper;

    public RepositoryServiceImpl(RepositoryMapper repositoryMapper, UserMapper userMapper) {
        this.repositoryMapper = repositoryMapper;
        this.userMapper = userMapper;
    }

    private RepositoryVO convertToRepositoryVO(Repository repository) {
        RepositoryVO repositoryVO = new RepositoryVO();
        repositoryVO.setId(repository.getId());
        repositoryVO.setUserId(repository.getUserId());
        repositoryVO.setUsername(userMapper.selectById(repository.getUserId()).getUsername());
        repositoryVO.setRepoAuth(repository.getRepoAuth());
        repositoryVO.setRepoName(repository.getRepoName());
        repositoryVO.setRepoBio(repository.getRepoBio());
        repositoryVO.setStarCount(repository.getStarCount());
        repositoryVO.setForkCount(repository.getForkCount());
        repositoryVO.setWatchCount(repository.getWatchCount());
        repositoryVO.setFollowerCount(userMapper.selectById(repositoryMapper.selectById(repository.getId()).getUserId()).getFollowerCount());
        repositoryVO.setLanguage(repository.getLanguage());
        repositoryVO.setUpdatedAt(repository.getUpdatedAt());
        return repositoryVO;
    }

    private Page<RepositoryVO> getRepositoryVOPage(int page, int size, Page<Repository> repositoryPage) {
        Page<RepositoryVO> repositoryVOPage = new Page<>(page, size);
        repositoryVOPage.setRecords(repositoryPage.getRecords().stream().map(this::convertToRepositoryVO).collect(Collectors.toList()));
        return repositoryVOPage;
    }

    @Override
    public Page<RepositoryVO> getRepositories(int page, int size) {
        Page<Repository> repositoryPage = new Page<>(page, size);
        repositoryPage = repositoryMapper.selectPage(repositoryPage, null);
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getRepositoriesByLanguage(int page, int size, String language) {
        Page<Repository> repositoryPage = new Page<>(page, size);
        QueryWrapper<Repository> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("language", language);
        repositoryPage = repositoryMapper.selectPage(repositoryPage, queryWrapper);
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public boolean isRepositoryValid(String repoAuth, String repoName) {
        String apiUrl = "https://api.github.com/repos/" + repoAuth + "/" + repoName;
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject(apiUrl, String.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }

    @Override
    public boolean isRepositoryExist(String repoAuth, String repoName) {
        QueryWrapper<Repository> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("repo_auth", repoAuth);
        queryWrapper.eq("repo_name", repoName);
        return repositoryMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean checkRepositoryBinding(int id) {
        QueryWrapper<Repository> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        return repositoryMapper.selectCount(queryWrapper) > 0;
    }


    @Override
    public String getRepositoryLanguage(String repoAuth, String repoName) {
        String apiUrl = "https://api.github.com/repos/" + repoAuth + "/" + repoName + "/languages";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map<String, Integer>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            Map<String, Integer> languages = responseEntity.getBody();
            if (languages != null && !languages.isEmpty()) {
                return languages.entrySet().iterator().next().getKey();
            }
            return MessageConstant.URL_INVALID;
        } catch (RestClientException e) {
            return MessageConstant.URL_INVALID;
        }
    }

    @Override
    public String getRepositoryBio(String repoAuth, String repoName) {
        String apiUrl = "https://api.github.com/repos/" + repoAuth + "/" + repoName;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("description")) {
                return (String) response.get("description");
            }
            return MessageConstant.URL_INVALID;
        } catch (RestClientException e) {
            return MessageConstant.URL_INVALID;
        }
    }

    @Override
    public int getStarCount(String repositoryAuth, String repositoryName) {
        String apiUrl = "https://api.github.com/repos/" + repositoryAuth + "/" + repositoryName;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("stargazers_count")) {
                return (int) response.get("stargazers_count");
            }
        } catch (RestClientException e) {
            return 0;
        }
        return 0;
    }

    @Override
    public int getForkCount(String repositoryAuth, String repositoryName) {
        String apiUrl = "https://api.github.com/repos/" + repositoryAuth + "/" + repositoryName;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("forks_count")) {
                return (int) response.get("forks_count");
            }
        } catch (RestClientException e) {
            return 0;
        }
        return 0;
    }

    @Override
    public int getWatchCount(String repositoryAuth, String repositoryName) {
        String apiUrl = "https://api.github.com/repos/" + repositoryAuth + "/" + repositoryName;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("subscribers_count")) {
                return (int) response.get("subscribers_count");
            }
        } catch (RestClientException e) {
            return 0;
        }
        return 0;
    }
}