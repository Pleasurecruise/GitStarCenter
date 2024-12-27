package cn.yiming1234.gitstarcenter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlParserUtil {
    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)");

    public static String[] parseGithubRepoUrl(String url) {
        Matcher matcher = GITHUB_REPO_PATTERN.matcher(url);
        if (matcher.matches()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        throw new IllegalArgumentException("Invalid GitHub repository URL");
    }
}