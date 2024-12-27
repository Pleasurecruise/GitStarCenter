package cn.yiming1234.gitstarcenter.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public interface StarService {
    void addStar(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception;
    void removeStar(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception;
    void forkRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception;
    void removeFork(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId)  throws Exception;
    void watchRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception;
    void unwatchRepository(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId) throws Exception;
    void followUser(OAuth2AuthorizedClient authorizedClient, String targetUsername, Integer sourceUserId) throws Exception;
    void unfollowUser(OAuth2AuthorizedClient authorizedClient, String targetUsername, Integer sourceUserId) throws Exception;
    boolean checkStarStatus(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception;
    boolean checkForkStatus(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception;
    boolean checkWatchStatus(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName) throws Exception;
    boolean checkFollowStatus(OAuth2AuthorizedClient authorizedClient, int targetUserId) throws Exception;
}