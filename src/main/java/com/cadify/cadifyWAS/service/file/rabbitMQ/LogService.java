package com.cadify.cadifyWAS.service.file.rabbitMQ;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LogService {
    private final List<String> logs = new ArrayList<>();

    public void add(String message) {
        logs.add(message);
    }

    public List<String> getLogs() {
        return logs;
    }
}
