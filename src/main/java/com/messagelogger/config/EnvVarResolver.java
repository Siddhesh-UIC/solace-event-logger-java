package com.messagelogger.config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvVarResolver {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+)(?::-(.*?))?}");

    private final Map<String, String> env;
    private final Map<String, String> yamlProps;

    public EnvVarResolver(Map<String, String> env, Map<String, String> yamlProps) {
        this.env = env;
        this.yamlProps = yamlProps;
    }

    public String resolve(String input) {
        if (input == null) return null;
        Matcher m = PLACEHOLDER.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String key = m.group(1).trim();
            String defaultVal = m.group(2); // null if no :-
            String value = env.getOrDefault(key, yamlProps.get(key));
            if (value == null) {
                if (defaultVal != null) {
                    value = defaultVal;
                } else {
                    throw new IllegalStateException(
                        "Required config variable '${" + key + "}' is not set and has no default");
                }
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
