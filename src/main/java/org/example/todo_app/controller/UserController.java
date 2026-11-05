package org.example.todo_app.controller;

import org.example.todo_app.model.User;
import org.example.todo_app.repository.UserRepository;
import org.example.todo_app.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(currentUserService.require(authorization));
    }

    @PutMapping("/current")
    @Transactional
    public ResponseEntity<User> updateProfile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                              @RequestBody User updatedData) {
        User user = currentUserService.require(authorization);
        if (updatedData.getName() != null && !updatedData.getName().isBlank()) user.setName(updatedData.getName().trim());
        if (updatedData.getUsername() != null && !updatedData.getUsername().isBlank()) user.setUsername(updatedData.getUsername().trim());
        if (updatedData.getAvatarUrl() != null) user.setAvatarUrl(updatedData.getAvatarUrl().trim());
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping(value = "/current/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<User> uploadAvatar(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        User user = currentUserService.require(authorization);
        if (file.isEmpty() || file.getSize() > 3 * 1024 * 1024 ||
                !(MediaType.IMAGE_JPEG_VALUE.equals(file.getContentType()) || MediaType.IMAGE_PNG_VALUE.equals(file.getContentType()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a PNG or JPEG image up to 3 MB");
        }
        byte[] bytes = file.getBytes();
        if (ImageIO.read(new java.io.ByteArrayInputStream(bytes)) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a valid image");
        }
        user.setAvatarData(bytes);
        user.setAvatarContentType(file.getContentType());
        user.setAvatarUrl("/api/profile/" + user.getId() + "/avatar");
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/{id}/avatar")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getAvatarData() == null || user.getAvatarContentType() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(user.getAvatarContentType()))
                .cacheControl(org.springframework.http.CacheControl.noCache()).body(user.getAvatarData());
    }
}
