package cn.yiming1234.gitstarcenter.service.impl;

import cn.yiming1234.gitstarcenter.constant.MessageConstant;
import cn.yiming1234.gitstarcenter.enumeration.InteractionType;
import cn.yiming1234.gitstarcenter.entity.Interaction;
import cn.yiming1234.gitstarcenter.mapper.InteractionMapper;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.service.StarService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StarServiceImpl implements StarService {

    private final WebClient webClient;
    private final InteractionMapper interactionMapper;
    private final UserMapper userMapper;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;
    public StarServiceImpl(WebClient.Builder webClientBuilder, InteractionMapper interactionMapper, UserMapper userMapper) {
        this.webClient = webClientBuilder.build();
        this.interactionMapper = interactionMapper;
        this.userMapper = userMapper;
    }

    /**
     * 执行 GitHub API 请求
     */
    private void executeGitHubApi(
            OAuth2AuthorizedClient authorizedClient,
            HttpMethod method,
            String apiUrl,
            String requestBody,
            HttpStatus... successStatuses
    ) throws Exception {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        log.info("Executing GitHub API request: {} {}", method, apiUrl);
        try {
            ResponseEntity<String> response = webClient.method(method)
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response == null || !isStatusSuccessful((HttpStatus) response.getStatusCode(), successStatuses)) {
                log.error("GitHub API request failed: {} {} - Status: {}", method, apiUrl, response != null ? response.getStatusCode() : "null");
                throw new Exception("GitHub API 请求失败，状态码：" + (response != null ? response.getStatusCode() : "null"));
            }

            log.info("GitHub API request successful: {} {}", method, apiUrl);
        } catch (Exception e) {
            log.error("GitHub API request exception: {} {} - {}", method, apiUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * 检查 GitHub 状态
     */
    private boolean isStatusSuccessful(HttpStatus statusCode, HttpStatus... successStatuses) {
        for (HttpStatus successStatus : successStatuses) {
            if (statusCode == successStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查Github中的互动状态
     */
    private boolean checkGitHubStatus(OAuth2AuthorizedClient authorizedClient, String apiUrl) throws Exception {
        try {
            ResponseEntity<String> response = webClient.get()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "token " + authorizedClient.getAccessToken().getTokenValue())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .toEntity(String.class)
                .block();

                return response != null && (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getStatusCode() == HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("GitHub API request exception: {} - {}", apiUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * 处理用户交互
     */
    private void handleInteraction(
            Integer sourceUserId,
            Integer targetUserId,
            InteractionType type,
            boolean status
    ) {
        QueryWrapper<Interaction> query = new QueryWrapper<>();
        query.eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId);
        Interaction interaction = interactionMapper.selectOne(query);
        interaction.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        if (sourceUserId.equals(targetUserId) && type == InteractionType.IS_FOLLOW) {
            throw new IllegalArgumentException(MessageConstant.CANNOT_FOLLOW_YOURSELF);
        }
        switch (type) {
            case IS_STAR:
                interaction.setIsStar(status);
                break;
            case IS_FORK:
                interaction.setIsFork(status);
                break;
            case IS_WATCH:
                interaction.setIsWatch(status);
                break;
            case IS_FOLLOW:
                interaction.setIsFollow(status);
                break;
            default:
                throw new IllegalArgumentException("未知的interaction类型: " + type);
        }
        interactionMapper.updateById(interaction);
        log.info("Interaction updated: sourceUserId={}, targetUserId={}, type={}, status={}", sourceUserId, targetUserId, type, status);
    }

    @Override
    public boolean checkStarStatus(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception {
        String apiUrl = String.format("%s/user/starred/%s/%s", githubApiBaseUrl, repoAuth, repoName);
        try {
            return checkGitHubStatus(authorizedClient, apiUrl);
        } catch (Exception e) {
            if (e instanceof WebClientResponseException.NotFound) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean checkForkStatus(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception {
        String apiUrl = String.format("%s/repos/%s/%s/forks", githubApiBaseUrl, repoAuth, repoName);
        try {
            return checkGitHubStatus(authorizedClient, apiUrl);
        } catch (Exception e) {
            if (e instanceof WebClientResponseException.NotFound) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean checkWatchStatus(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception {
        String apiUrl = String.format("%s/repos/%s/%s/subscribers", githubApiBaseUrl, repoAuth, repoName);
        try {
            List<Map<String, Object>> subscribers = webClient.get()
                .uri(apiUrl)
                .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

            String currentUsername = authorizedClient.getPrincipalName();
            return subscribers != null && subscribers.stream()
                .anyMatch(user -> currentUsername.equals(user.get("login")));
        } catch (Exception e) {
            if (e instanceof WebClientResponseException.NotFound) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean checkFollowStatus(OAuth2AuthorizedClient authorizedClient, int targetUserId) {
        String apiUrl = String.format("%s/user/following", githubApiBaseUrl);
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        List<Map<String, Object>> followingUsers = webClient.get()
            .uri(apiUrl)
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
            .block();
        String targetUsername = userMapper.selectById(targetUserId).getUsername();
        return followingUsers != null && followingUsers.stream()
            .anyMatch(user -> targetUsername.equals(user.get("login")));
}

    @Override
    public void addStar(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception {
        String apiUrl = String.format("%s/user/starred/%s/%s", githubApiBaseUrl, repoAuth, repoName);
        Interaction existingInteraction = interactionMapper.selectOne(new QueryWrapper<Interaction>()
                .eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId)
                .eq("is_star", true));
        if (existingInteraction != null) {
            log.info("User {} already starred repository {}/{}", sourceUserId, repoAuth, repoName);
            return;
        }
        executeGitHubApi(
                authorizedClient,
                HttpMethod.PUT,
                apiUrl,
                "",
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_STAR, true);
    }

    @Override
    public void removeStar(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception {
        String apiUrl = String.format("%s/user/starred/%s/%s", githubApiBaseUrl, repoAuth, repoName);
        executeGitHubApi(
                authorizedClient,
                HttpMethod.DELETE,
                apiUrl,
                "",
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_STAR, false);
    }

    @Override
    public void forkRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception {
        String apiUrl = String.format("%s/repos/%s/%s/forks", githubApiBaseUrl, repoAuth, repoName);
        Interaction existingInteraction = interactionMapper.selectOne(new QueryWrapper<Interaction>()
                .eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId)
                .eq("is_fork", true));
        if (existingInteraction != null) {
            log.info("User {} already forked repository {}/{}", sourceUserId, repoAuth, repoName);
            return;
        }
        executeGitHubApi(
                authorizedClient,
                HttpMethod.POST,
                apiUrl,
                "",
                HttpStatus.CREATED, // 201
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_FORK, true);
    }

    @Override
    public void removeFork(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception {
        String apiUrl = String.format("%s/repos/%s/%s", githubApiBaseUrl, repoAuth, repoName);
        executeGitHubApi(
                authorizedClient,
                HttpMethod.DELETE,
                apiUrl,
                "",
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_FORK, false);
    }

    @Override
    public void watchRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception {
        String apiUrl = String.format("%s/repos/%s/%s/subscription", githubApiBaseUrl, repoAuth, repoName);
        String requestBody = "{\"subscribed\": true, \"ignored\": false, \"participating\": true}";
        Interaction existingInteraction = interactionMapper.selectOne(new QueryWrapper<Interaction>()
                .eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId)
                .eq("is_watch", true));
        if (existingInteraction != null) {
            log.info("User {} already watching repository {}/{}", sourceUserId, repoAuth, repoName);
            return;
        }
        executeGitHubApi(
                authorizedClient,
                HttpMethod.PUT,
                apiUrl,
                requestBody,
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_WATCH, true);
    }

    @Override
    public void unwatchRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception {
        String apiUrl = String.format("%s/repos/%s/%s/subscription", githubApiBaseUrl, repoAuth, repoName);
        executeGitHubApi(
                authorizedClient,
                HttpMethod.DELETE,
                apiUrl,
                "",
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_WATCH, false);
    }

    @Override
    public void followUser(OAuth2AuthorizedClient authorizedClient, String targetUsername, Integer sourceUserId) throws Exception {
        Integer targetUserId = userMapper.selectByUsername(targetUsername).getId();
        String apiUrl = String.format("%s/user/following/%s", githubApiBaseUrl, targetUsername);
        Interaction existingInteraction = interactionMapper.selectOne(new QueryWrapper<Interaction>()
                .eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId)
                .eq("is_follow", true));
        if (existingInteraction != null) {
            log.info("User {} already follows user {}", sourceUserId, targetUserId);
            return;
        }
        executeGitHubApi(
                authorizedClient,
                HttpMethod.PUT,
                apiUrl,
                "",
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_FOLLOW, true);
    }

    @Override
    public void unfollowUser(OAuth2AuthorizedClient authorizedClient, String targetUsername, Integer sourceUserId) throws Exception {
        Integer targetUserId = userMapper.selectByUsername(targetUsername).getId();
        String apiUrl = String.format("%s/user/following/%s", githubApiBaseUrl, targetUsername);
        executeGitHubApi(
                authorizedClient,
                HttpMethod.DELETE,
                apiUrl,
                "",
                HttpStatus.NO_CONTENT, // 204
                HttpStatus.OK // 200
        );
        handleInteraction(sourceUserId, targetUserId, InteractionType.IS_FOLLOW, false);
    }
}
