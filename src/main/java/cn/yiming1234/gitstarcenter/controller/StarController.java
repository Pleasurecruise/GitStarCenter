package cn.yiming1234.gitstarcenter.controller;

import cn.yiming1234.gitstarcenter.constant.MessageConstant;
import cn.yiming1234.gitstarcenter.entity.Interaction;
import cn.yiming1234.gitstarcenter.mapper.InteractionMapper;
import cn.yiming1234.gitstarcenter.mapper.RepositoryMapper;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.result.Result;
import cn.yiming1234.gitstarcenter.service.RepositoryService;
import cn.yiming1234.gitstarcenter.service.StarService;
import cn.yiming1234.gitstarcenter.service.UserService;
import cn.yiming1234.gitstarcenter.util.JwtUtil;
import cn.yiming1234.gitstarcenter.util.MailUtil;
import cn.yiming1234.gitstarcenter.util.SendMail;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
public class StarController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RepositoryService repositoryService;
    private final StarService starService;
    private final UserService userService;
    private final MailUtil mailUtil;
    private final SendMail sendMail;
    private final InteractionMapper interactionMapper;
    private final RepositoryMapper repositoryMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public StarController(OAuth2AuthorizedClientService authorizedClientService, RepositoryService repositoryService, StarService starService, UserService userService, MailUtil mailUtil, SendMail sendMail, InteractionMapper interactionMapper, RepositoryMapper repositoryMapper, UserMapper userMapper, JwtUtil jwtUtil) {
        this.authorizedClientService = authorizedClientService;
        this.repositoryService = repositoryService;
        this.starService = starService;
        this.userService = userService;
        this.mailUtil = mailUtil;
        this.sendMail = sendMail;
        this.interactionMapper = interactionMapper;
        this.repositoryMapper = repositoryMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    private OAuth2AuthorizedClient getAuthorizedClient(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new IllegalStateException("Not authenticated with OAuth2");
        }
        String clientRegistrationId = "github";
        return authorizedClientService.loadAuthorizedClient(clientRegistrationId, oauth2User.getName());
    }

    private Result<String> performAction(OAuth2AuthorizedClient authorizedClient, String repoAuth, String repoName, Integer sourceUserId, Integer targetUserId, String action) {
        try {
            // 限制频繁操作
            List<Interaction> interactions = interactionMapper.selectBySourceUserId(sourceUserId);
            if (!interactions.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime latestInteractionTime = interactions.get(0).getUpdatedAt().toLocalDateTime();
                if (Duration.between(latestInteractionTime, now).toHours() < 1) {
                    return Result.error(MessageConstant.OPERATION_TOO_FREQUENT);
                }
                if (interactions.size() >= 5) {
                    LocalDateTime fifthLatestInteractionTime = interactions.get(4).getUpdatedAt().toLocalDateTime();
                    if (Duration.between(fifthLatestInteractionTime, latestInteractionTime).toHours() < 1) {
                        return Result.error(MessageConstant.OPERATION_TOO_FREQUENT);
                    }
                }
            }
            // 是否通知用户
            String email = null;
            String username = null;
            try {
                username = authorizedClient.getPrincipalName();
                email = userService.getAccountInfo(username).getEmail();
                log.info("email: {}", email);
            } catch (Exception ignored) {}

            switch (action) {
                case "star":
                    starService.addStar(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId);
                    sendMail.apply(sourceUserId, targetUserId, "Star");
                    if (email != null && username != null) {
                        mailUtil.sendMail(email, username, "Star");
                    }
                    return Result.success(MessageConstant.STAR_SUCCESS);
                case "unstar":
                    starService.removeStar(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId);
                    sendMail.apply(sourceUserId, targetUserId, "Unstar");
                    if (email != null && username != null) {
                        mailUtil.sendMail(email, username, "Unstar");
                    }
                    return Result.success(MessageConstant.UNSTAR_SUCCESS);
                case "fork":
                    starService.forkRepository(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId);
                    sendMail.apply(sourceUserId, targetUserId, "Fork");
                    if (email != null && username != null) {
                        mailUtil.sendMail(email, username, "Fork");
                    }
                    return Result.success(MessageConstant.FORK_SUCCESS);
                case "unfork":
                    starService.removeFork(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId);
                    sendMail.apply(sourceUserId, targetUserId, "Unfork");
                    if (email != null && username != null) {
                        mailUtil.sendMail(email, username, "Unfork");
                    }
                    return Result.success(MessageConstant.UNFORK_SUCCESS);
                case "watch":
                    starService.watchRepository(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId);
                    sendMail.apply(sourceUserId, targetUserId, "Watch");
                    if (email != null && username != null) {
                        mailUtil.sendMail(email, username, "Watch");
                    }
                    return Result.success(MessageConstant.WATCH_SUCCESS);
                case "unwatch":
                    starService.unwatchRepository(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId);
                    sendMail.apply(sourceUserId, targetUserId, "Unwatch");
                    if (email != null && username != null) {
                        mailUtil.sendMail(email, username, "Unwatch");
                    }
                    return Result.success(MessageConstant.UNWATCH_SUCCESS);
                default:
                    return Result.error(MessageConstant.UNKNOWN_ACTION);
            }
        } catch (Exception e) {
            return Result.error(action + " failure: " + e.getMessage());
        }
    }

    /**
     * 返回已存在的交互
     */
    @GetMapping("/interactions")
    @PreAuthorize("hasRole('USER')")
    public Result<Interaction> getInteractions(
            Authentication authentication,
            @RequestHeader("token") String token,
            @RequestParam String repoAuth,
            @RequestParam String repoName) throws Exception {
        // PreCheck是否绑定仓库
        // 如果和当前用户不存在交互
        // 则获取并插入数据
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        int sourceUserId = userMapper.selectByUsername(jwtUtil.getNameFromToken(token)).getId();
        int targetUserId = repositoryMapper.selectByRepoAuthAndRepoName(repoAuth, repoName).getUserId();
        if (repositoryService.checkRepositoryBinding(sourceUserId)) {
            Interaction existingInteraction = interactionMapper.selectOne(new QueryWrapper<Interaction>()
                .eq("source_user_id", sourceUserId)
                .eq("target_user_id", targetUserId));
            if (existingInteraction == null) {
                Interaction interaction = new Interaction();
                interaction.setSourceUserId(sourceUserId);
                interaction.setTargetUserId(targetUserId);

                boolean isFollowing = starService.checkFollowStatus(authorizedClient, targetUserId);
                interaction.setIsFollow(isFollowing);
                boolean hasStarred = starService.checkStarStatus(authorizedClient, repoAuth, repoName);
                interaction.setIsStar(hasStarred);
                boolean hasForked = starService.checkForkStatus(authorizedClient, repoAuth, repoName);
                interaction.setIsFork(hasForked);
                boolean hasWatched = starService.checkWatchStatus(authorizedClient, repoAuth, repoName);
                interaction.setIsWatch(hasWatched);

                interactionMapper.insert(interaction);
            }
        } else {
            return Result.error(MessageConstant.REPOSITORY_NOT_BOUND);
        }
        Interaction interaction = interactionMapper.selectBySourceUserIdAndTargetUserId(sourceUserId, targetUserId);
        return Result.success(interaction);
    }

    /**
     * 添加 Star
     */
    @PostMapping("/star")
    @PreAuthorize("hasRole('USER')")
    public Result<String> addStar(
            Authentication authentication,
            @RequestParam String repoAuth,
            @RequestParam String repoName,
            @RequestParam Integer sourceUserId,
            @RequestParam Integer targetUserId) {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        return performAction(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId, "star");
    }

    /**
     * 取消 Star
     */
    @PostMapping("/unstar")
    public Result<String> removeStar(
            Authentication authentication,
            @RequestParam String repoAuth,
            @RequestParam String repoName,
            @RequestParam Integer sourceUserId,
            @RequestParam Integer targetUserId) {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        return performAction(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId, "unstar");
    }

    /**
     * Fork 仓库
     */
    @PostMapping("/fork")
    public Result<String> forkRepository(
            Authentication authentication,
            @RequestParam String repoAuth,
            @RequestParam String repoName,
            @RequestParam Integer sourceUserId,
            @RequestParam Integer targetUserId) {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        return performAction(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId, "fork");
    }

    /**
     * 取消 Fork
     */
    @PostMapping("/unfork")
    public Result<String> removeFork(
            Authentication authentication,
            @RequestParam String repoAuth,
            @RequestParam String repoName,
            @RequestParam Integer sourceUserId,
            @RequestParam Integer targetUserId) {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        return performAction(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId, "unfork");
    }

    /**
     * Watch 仓库
     */
    @PostMapping("/watch")
    public Result<String> watchRepository(
            Authentication authentication,
            @RequestParam String repoAuth,
            @RequestParam String repoName,
            @RequestParam Integer sourceUserId,
            @RequestParam Integer targetUserId) {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        return performAction(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId, "watch");
    }

    /**
     * 取消 Watch
     */
    @PostMapping("/unwatch")
    public Result<String> unwatchRepository(
            Authentication authentication,
            @RequestParam String repoAuth,
            @RequestParam String repoName,
            @RequestParam Integer sourceUserId,
            @RequestParam Integer targetUserId) {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        return performAction(authorizedClient, repoAuth, repoName, sourceUserId, targetUserId, "unwatch");
    }

    /**
     * Follow 用户
     */
    @PostMapping("/follow")
    public Result<String> followUser(
            Authentication authentication,
            @RequestParam String targetUsername,
            @RequestParam Integer sourceUserId) {
        try {
            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
            starService.followUser(authorizedClient, targetUsername, sourceUserId);
            return Result.success(MessageConstant.FOLLOW_SUCCESS);
        } catch (Exception e) {
            return Result.error("Follow failure: " + e.getMessage());
        }
    }

    /**
     * Unfollow 用户
     */
    @PostMapping("/unfollow")
    public Result<String> unfollowUser(
            Authentication authentication,
            @RequestParam String targetUsername,
            @RequestParam Integer sourceUserId) {
        try {
            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
            starService.unfollowUser(authorizedClient, targetUsername, sourceUserId);
            return Result.success(MessageConstant.UNFOLLOW_SUCCESS);
        } catch (Exception e) {
            return Result.error("Unfollow failure: " + e.getMessage());
        }
    }
}