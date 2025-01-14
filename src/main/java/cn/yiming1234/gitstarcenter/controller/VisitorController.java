package cn.yiming1234.gitstarcenter.controller;

import cn.yiming1234.gitstarcenter.result.Result;
import cn.yiming1234.gitstarcenter.service.VisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Slf4j
public class VisitorController {

    private final VisitorService visitorService;
    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/visitor")
    @PreAuthorize("hasRole('USER')")
    public void recordVisit(@RequestBody String userUUID) {
        visitorService.recordVisit(userUUID);
    }

    @GetMapping("/uv")
    @PreAuthorize("hasRole('USER')")
    public Result<Long> getDailyUV() {
        return Result.success(visitorService.countUniqueVisitors(LocalDate.now()));
    }
}
