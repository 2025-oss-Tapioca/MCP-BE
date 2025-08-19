package com.tapioca.MCPBE.util.common;

import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogLineParser {

    private static final Pattern ISO_PREFIX = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}[T\\s]\\d{2}:\\d{2}:\\d{2}(?:,\\d{3}|\\.\\d{3})?(?:Z|[+\\-]\\d{2}:?\\d{2})?)\\s+([A-Z]+)\\b.*?\\s-\\s(.*)$");
    private static final Pattern BRACKET_LEVEL = Pattern.compile("^\\s*\\[([A-Z]+)]\\s*(.*)$");

    public Map<String,Object> toMap(String line, String sourceType, String teamCode) {
        return toMap(line, sourceType, teamCode, System.currentTimeMillis());
    }

    public Map<String,Object> toMap(String line, String sourceType, String teamCode, long epochMillis) {
        try {
            String level = "INFO";
            String message = line;

            Matcher m1 = ISO_PREFIX.matcher(line);
            if (m1.find()) {
                String ts = m1.group(1);
                level = safeUpper(m1.group(2));
                message = m1.group(3);
                return base(ts, level, sourceType, message);
            }

            Matcher m2 = BRACKET_LEVEL.matcher(line);
            if (m2.find()) {
                level = safeUpper(m2.group(1));
                message = m2.group(2);
            }
            return base(epochMillis, level, sourceType, message);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.LOG_PARSING_ERROR);
        }
    }

    private static Map<String,Object> base(Object ts, String level, String sourceType, String message){
        Map<String,Object> map = new HashMap<>();
        map.put("timestamp", ts);           // String ISO or long epoch — BE 파서가 모두 수용
        map.put("level", level);
        map.put("service", sourceType.toLowerCase());
        map.put("message", message);
        return map;
    }

    private static String safeUpper(String s){ return s==null? "INFO" : s.toUpperCase(); }
}
