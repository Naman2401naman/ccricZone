package com.criczone.demo.api;

import com.criczone.demo.service.SystemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "Health and version endpoints")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return systemService.health();
    }

    @GetMapping("/api/version")
    public Map<String, Object> version() {
        return systemService.version();
    }
}
