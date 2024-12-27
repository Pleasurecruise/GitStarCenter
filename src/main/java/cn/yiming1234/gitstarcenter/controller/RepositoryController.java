package cn.yiming1234.gitstarcenter.controller;

import cn.yiming1234.gitstarcenter.constant.MessageConstant;
import cn.yiming1234.gitstarcenter.service.RepositoryService;
import cn.yiming1234.gitstarcenter.vo.RepositoryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class RepositoryController {

    private final RepositoryService repositoryService;
    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * 分页获取仓库
     */
    @GetMapping("/repositories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRepositories(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = repositoryService.getRepositories(page, size);
            response.put("code", 1);
            response.put("data", repositories);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 0);
            response.put("message", MessageConstant.REPOSITORY_FETCHED_FAILURE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据语言分页获取仓库
     */
    @GetMapping("/repositories/by-language")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRepositoriesByLanguage(@RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "10") int size,
                                                                         @RequestParam String language) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<RepositoryVO> repositories = repositoryService.getRepositoriesByLanguage(page, size, language);
            response.put("status", "success");
            response.put("data", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", MessageConstant.REPOSITORY_FETCHED_FAILURE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}