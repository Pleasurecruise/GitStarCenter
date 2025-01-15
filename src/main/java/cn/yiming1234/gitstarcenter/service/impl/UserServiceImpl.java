package cn.yiming1234.gitstarcenter.service.impl;

import cn.yiming1234.gitstarcenter.constant.MessageConstant;
import cn.yiming1234.gitstarcenter.entity.Interaction;
import cn.yiming1234.gitstarcenter.entity.Repository;
import cn.yiming1234.gitstarcenter.entity.User;
import cn.yiming1234.gitstarcenter.mapper.InteractionMapper;
import cn.yiming1234.gitstarcenter.mapper.RepositoryMapper;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.service.RepositoryService;
import cn.yiming1234.gitstarcenter.service.StarService;
import cn.yiming1234.gitstarcenter.service.UserService;
import cn.yiming1234.gitstarcenter.vo.RepositoryVO;
import cn.yiming1234.gitstarcenter.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RepositoryMapper repositoryMapper;
    private final RepositoryService repositoryService;
    private final InteractionMapper interactionMapper;
    private final StarService starService;

    public UserServiceImpl(UserMapper userMapper, RepositoryMapper repositoryMapper, RepositoryService repositoryService, InteractionMapper interactionMapper, StarService starService) {
        this.userMapper = userMapper;
        this.repositoryMapper = repositoryMapper;
        this.repositoryService = repositoryService;
        this.interactionMapper = interactionMapper;
        this.starService = starService;
    }

    /**
     *  转换仓库信息
     */
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

    /**
     * 更新仓库信息
     */
    @Override
    public void updateRepository(Repository repository,
                                  String repoAuth,
                                  String repoName) {
        repository.setRepoAuth(repoAuth);
        repository.setRepoName(repoName);
        repository.setStarCount(repositoryService.getStarCount(repoAuth, repoName));
        repository.setForkCount(repositoryService.getForkCount(repoAuth, repoName));
        repository.setWatchCount(repositoryService.getWatchCount(repoAuth, repoName));
        repository.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        repositoryMapper.updateById(repository);
    }

    /**
     * 同步互动数据
     */
    @Override
    public void syncRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception {
        boolean isStarred = starService.checkStarStatus(authorizedClient, repoAuth, repoName);
        boolean isForked = starService.checkForkStatus(authorizedClient, repoAuth, repoName);
        boolean isWatched = starService.checkWatchStatus(authorizedClient, repoAuth, repoName);

        int sourceUserId = userMapper.selectByUsername(authorizedClient.getPrincipalName()).getId();
        int targetUserId = repositoryMapper.selectByRepoAuthAndRepoName(repoAuth, repoName).getUserId();
        Interaction interaction = interactionMapper.selectOne(new QueryWrapper<Interaction>()
                .eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId));
        if (interaction != null) {
            interaction.setIsStar(isStarred);
            interaction.setIsFork(isForked);
            interaction.setIsWatch(isWatched);
            log.info("Updating interaction: {}", interaction);
            interaction.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            interactionMapper.updateById(interaction);
        } else {
            interaction = new Interaction();
            interaction.setSourceUserId(sourceUserId);
            interaction.setTargetUserId(targetUserId);
            interaction.setIsStar(isStarred);
            interaction.setIsFork(isForked);
            interaction.setIsWatch(isWatched);
            interaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            interactionMapper.insert(interaction);
        }
    }

    /**
     * 通过用户名获取用户信息
     */
    @Override
    public UserVO getAccountInfo(String username) {
        User user = userMapper.selectByUsername(username);
        UserVO.UserVOBuilder userVOBuilder = UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .avatar(user.getAvatarUrl())
            .followerCount(user.getFollowerCount());

        Repository repository = repositoryMapper.selectByUserId(userMapper.selectByUsername(username).getId());
        if (repository != null) {
            userVOBuilder
                .repoAuth(repository.getRepoAuth())
                .repoName(repository.getRepoName())
                .starCount(repository.getStarCount())
                .forkCount(repository.getForkCount())
                .watchCount(repository.getWatchCount())
                .repoUpdateTime(repository.getUpdatedAt());
        }
        return userVOBuilder.build();
    }

    /**
     * 通过作者和仓库名获取用户信息
     */
    @Override
    public UserVO getTargetAccountInfo(String repoAuth, String repoName){
        Repository repository = repositoryMapper.selectByRepoAuthAndRepoName(repoAuth, repoName);
        User user = userMapper.selectById(repository.getUserId());
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .avatar(user.getAvatarUrl())
            .followerCount(user.getFollowerCount())
            .repoAuth(repository.getRepoAuth())
            .repoName(repository.getRepoName())
            .starCount(repository.getStarCount())
            .forkCount(repository.getForkCount())
            .watchCount(repository.getWatchCount())
            .repoUpdateTime(repository.getUpdatedAt())
            .build();
    }

    @Override
    public void saveUserInfo(User user) {
        log.info("Saving user: {}", user);
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("Error inserting user: {}", e.getMessage());
        }
    }

    @Override
    public void updateUser(User user) {
        log.info("Updating user: {}", user);
        try {
            userMapper.updateById(user);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage());
        }
    }

    @Override
    public void bindRepository(String username, String repositoryAuth, String repositoryName) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException(MessageConstant.USER_NOT_FOUND);
        }
        long currentTime = System.currentTimeMillis();
        String repoLanguage = repositoryService.getRepositoryLanguage(repositoryAuth, repositoryName);
        String repoBio = repositoryService.getRepositoryBio(repositoryAuth, repositoryName);
        Repository existingRepository = repositoryMapper.selectById(user.getId());
        if (existingRepository == null) {
            Repository repository = new Repository();
            repository.setUserId(user.getId());
            repository.setRepoBio(repoBio);
            repository.setLanguage(repoLanguage);
            updateRepository(repository, repositoryAuth, repositoryName);
            repository.setCreatedAt(new Timestamp(currentTime));
            repositoryMapper.insert(repository);
        }
    }

    /**
     * 用户名 -> ID
     *  ID => Target User ID ->Source User ID
     *  Source User ID -> Repository
     */
    @Override
    public Page<RepositoryVO> getRepositoriesStarredByUser(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> sourceUserIds = interactionMapper.selectByTargetUserId(userId).stream()
            .filter(Interaction::getIsStar)
            .map(Interaction::getSourceUserId)
            .toList();
        if (sourceUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectStarredRepositoriesBySourceUserIds(sourceUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getRepositoriesForkedByUser(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> sourceUserIds = interactionMapper.selectByTargetUserId(userId).stream()
            .filter(Interaction::getIsFork)
            .map(Interaction::getSourceUserId)
            .toList();
        if (sourceUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectForkedRepositoriesBySourceUserIds(sourceUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getRepositoriesWatchedByUser(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> sourceUserIds = interactionMapper.selectByTargetUserId(userId).stream()
            .filter(Interaction::getIsWatch)
            .map(Interaction::getSourceUserId)
            .toList();
        if (sourceUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectWatchedRepositoriesBySourceUserIds(sourceUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getRepositoriesFollowedByUser(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> sourceUserIds = interactionMapper.selectByTargetUserId(userId).stream()
            .filter(Interaction::getIsFollow)
            .map(Interaction::getSourceUserId)
            .toList();
        if (sourceUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectFollowedRepositoriesBySourceUserIds(sourceUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    /**
     * 用户名 -> ID
     *  ID => Source User ID ->Target User ID
     *  Target User ID -> Repository
     */
    @Override
    public Page<RepositoryVO> getStarredRepositories(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> targetUserIds = interactionMapper.selectBySourceUserId(userId).stream()
            .filter(Interaction::getIsStar)
            .map(Interaction::getTargetUserId)
            .toList();
        if (targetUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectStarredRepositoriesByTargetUserIds(targetUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getForkedRepositories(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> targetUserIds = interactionMapper.selectBySourceUserId(userId).stream()
            .filter(Interaction::getIsFork)
            .map(Interaction::getTargetUserId)
            .toList();
        if (targetUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectForkedRepositoriesByTargetUserIds(targetUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getWatchedRepositories(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> targetUserIds = interactionMapper.selectBySourceUserId(userId).stream()
            .filter(Interaction::getIsWatch)
            .map(Interaction::getTargetUserId)
            .toList();
        if (targetUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectWatchedRepositoriesByTargetUserIds(targetUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }

    @Override
    public Page<RepositoryVO> getFollowedRepositories(String username, int page, int size) {
        int userId = userMapper.selectByUsername(username).getId();
        List<Integer> targetUserIds = interactionMapper.selectBySourceUserId(userId).stream()
            .filter(Interaction::getIsFollow)
            .map(Interaction::getTargetUserId)
            .toList();
        if (targetUserIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<Repository> repositoryPage = userMapper.selectFollowedRepositoriesByTargetUserIds(targetUserIds, new Page<>(page, size));
        return getRepositoryVOPage(page, size, repositoryPage);
    }
}