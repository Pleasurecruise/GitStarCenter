package cn.yiming1234.gitstarcenter.controller;

import cn.yiming1234.gitstarcenter.constant.MessageConstant;
import cn.yiming1234.gitstarcenter.entity.User;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.service.RepositoryService;
import cn.yiming1234.gitstarcenter.service.UserService;
import cn.yiming1234.gitstarcenter.util.JwtUtil;
import cn.yiming1234.gitstarcenter.vo.RepositoryVO;
import cn.yiming1234.gitstarcenter.vo.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class UserController {

    private final UserService userService;
    private final RepositoryService repositoryService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserController(UserService userService, RepositoryService repositoryService, JwtUtil jwtUtil, UserMapper userMapper) {
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @RequestMapping("/favicon.ico")
    @ResponseBody
     void favicon(){
    }
    
    // Test page
    @GetMapping("/test")
    public String testPage() {
        return "test";
    }

    /**
     * 获取当前账户信息
     */
    @GetMapping("/account")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getAccount(@RequestHeader("token") String token) {
        Map<String, Object> response = new HashMap<>();
        UserVO accountInfo = userService.getAccountInfo(jwtUtil.getNameFromToken(token));
        log.info("Account info: {}", accountInfo);
        try {
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", accountInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 0);
            response.put("msg", "Failed to fetch account info");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取目标账户信息
     */
    @GetMapping("/account/target")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getAccount(
            @RequestParam String repoAuth,
            @RequestParam String repoName) {
        Map<String, Object> response = new HashMap<>();
        UserVO accountInfo = userService.getTargetAccountInfo(repoAuth, repoName);
        try {
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", accountInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 0);
            response.put("msg", "Failed to fetch account info");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新账户信息
     */
    @PostMapping("/account")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> updateAccount(@RequestHeader("token") String  token, @RequestBody Map<String, Object> data){
        Map<String, Object> response = new HashMap<>();
        User user = userMapper.selectByUsername(jwtUtil.getNameFromToken(token));
        user.setNickname((String) data.get("nickname"));
        user.setEmail((String) data.get("email"));
        try {
            userService.updateUser(user);
            UserVO accountInfo = userService.getAccountInfo(jwtUtil.getNameFromToken(token));
            response.put("code", 1);
            response.put("data",  accountInfo);
            response.put("msg", "Account updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 0);
            response.put("msg", "Failed to update account");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 绑定仓库
     */
    @PostMapping("/bind-repository")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> bindRepository(
            @RequestHeader("token") String token,
            @RequestParam String repoAuth,
            @RequestParam String repoName) {
        Map<String, Object> response = new HashMap<>();
        int id = userMapper.selectByUsername(jwtUtil.getNameFromToken(token)).getId();
        if(repositoryService.isRepositoryExist(repoAuth, repoName) && repositoryService.checkRepositoryBinding(id)){
            response.put("code", 0);
            response.put("msg", MessageConstant.REPOSITORY_ALREADY_BOUND);
            return ResponseEntity.badRequest().body(response);
        } else if (!repositoryService.isRepositoryValid(repoAuth, repoName)) {
            response.put("code", 0);
            response.put("msg", MessageConstant.URL_INVALID);
            return ResponseEntity.badRequest().body(response);
        } else {
            userService.bindRepository(jwtUtil.getNameFromToken(token), repoAuth, repoName);
            UserVO accountInfo = userService.getAccountInfo(jwtUtil.getNameFromToken(token));
            response.put("code", 1);
            response.put("data", accountInfo);
            response.put("msg", "Repository bound successfully");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取Star自己的用户
     */
    @GetMapping("/user/starred-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRepositoriesStarredByUser(@RequestHeader("token") String token,
                                                                            @RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getRepositoriesStarredByUser(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch starred repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch liked repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取Fork自己的用户
     */
    @GetMapping("/user/forked-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRepositoriesForkedByUser(@RequestHeader("token") String token,
                                                                           @RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getRepositoriesForkedByUser(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch forked repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch forked repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取Watch自己的用户
     */
    @GetMapping("/user/watched-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRepositoriesWatchedByUser(@RequestHeader("token") String token,
                                                                            @RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getRepositoriesWatchedByUser(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch watched repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch watched repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取Follow自己的用户
     */
    @GetMapping("/user/followed-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRepositoriesFollowedByUser(@RequestHeader("token") String token,
                                                                             @RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getRepositoriesFollowedByUser(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch followed repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch followed repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取自己Star的用户
     */
    @GetMapping("/user/star-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getStarredRepositories(@RequestHeader("token") String token,
                                                                      @RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getStarredRepositories(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch starred repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch starred repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取自己Fork的用户
     */
    @GetMapping("/user/fork-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getForkedRepositories(@RequestHeader("token") String token,
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getForkedRepositories(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch forked repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch forked repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取自己Watch的用户
     */
    @GetMapping("/user/watch-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getWatchedRepositories(@RequestHeader("token") String token,
                                                                      @RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getWatchedRepositories(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch watched repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch watched repositories");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取自己Follow的用户
     */
    @GetMapping("/user/follow-repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getFollowedRepositories(@RequestHeader("token") String token,
                                                                       @RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "5") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = userService.getFollowedRepositories(jwtUtil.getNameFromToken(token), page, size);
            response.put("code", 1);
            response.put("msg", "Success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch followed repositories", e);
            response.put("code", 0);
            response.put("msg", "Failed to fetch followed repositories");
            return ResponseEntity.status(500).body(response);
        }
    }
}
