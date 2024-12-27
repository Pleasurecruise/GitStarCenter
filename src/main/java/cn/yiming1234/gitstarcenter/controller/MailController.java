package cn.yiming1234.gitstarcenter.controller;

import cn.yiming1234.gitstarcenter.result.Result;
import cn.yiming1234.gitstarcenter.service.MailService;
import cn.yiming1234.gitstarcenter.util.JwtUtil;
import cn.yiming1234.gitstarcenter.vo.MailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class MailController {

    private final MailService mailService;
    private final JwtUtil jwtUtil;
    public MailController(MailService mailService, JwtUtil jwtUtil) {
        this.mailService = mailService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取所有邮件
     */
    @GetMapping("/mail")
    @PreAuthorize("hasRole('USER')")
    public Result<List<MailVO>> getMails(@RequestParam(required = false) Integer status, @RequestHeader("token") String token) {
        return Result.success(mailService.getMails(jwtUtil.getNameFromToken(token), status));
    }

    /**
     * 阅读邮件
     */
    @PutMapping("/mails/{id}/read")
    @PreAuthorize("hasRole('USER')")
    public Result<?> readMails(@PathVariable Long id, @RequestHeader("token") String token) {
        String username = jwtUtil.getNameFromToken(token);
        mailService.read(username, id);
        return Result.success();
    }

    /**
     * 待读邮件
     */
    @PutMapping("/mails/{id}/unread")
    @PreAuthorize("hasRole('USER')")
    public Result<?> unreadMails(@PathVariable Long id, @RequestHeader("token") String token) {
        String username = jwtUtil.getNameFromToken(token);
        mailService.unread(username, id);
        return Result.success();
    }

    /**
     * 删除邮件
     */
    @DeleteMapping("/mails/{id}")
    @PreAuthorize("hasRole('USER')")
    public Result<?> deleteMails(@PathVariable Long id, @RequestHeader("token") String token) {
        String username = jwtUtil.getNameFromToken(token);
        mailService.delete(username, id);
        return Result.success();
    }

}
