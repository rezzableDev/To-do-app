package org.example.todo_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/app")
@CrossOrigin(origins = "*")
public class AppModeController {

    private static final AtomicBoolean specialMode = new AtomicBoolean(false);

    public static void setSpecialMode(boolean enabled) {
        specialMode.set(enabled);
    }

    public static boolean isSpecialMode() {
        return specialMode.get();
    }

    @GetMapping("/mode")
    public ResponseEntity<?> getMode() {
        return ResponseEntity.ok(Map.of("specialMode", specialMode.get()));
    }
}