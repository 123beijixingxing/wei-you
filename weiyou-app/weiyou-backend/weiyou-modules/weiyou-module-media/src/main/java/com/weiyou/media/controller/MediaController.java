package com.weiyou.media.controller;

import com.weiyou.common.core.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Value("${weiyou.media.local-dir:./data/uploads}")
    private String localDir;

    @PostMapping("/upload/policy")
    public ApiResponse<UploadPolicyData> getUploadPolicy(@Valid @RequestBody UploadPolicyRequest request) {
        return ApiResponse.ok(new UploadPolicyData(
                "https://oss.weiyou.local/upload",
                request.bizType() + "/2026/05/11/" + request.fileName(),
                "PUT",
                "2026-05-11T23:59:59Z"
        ));
    }

    @PostMapping(value = "/upload/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<LocalUploadData> uploadLocal(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(defaultValue = "moment") String bizType,
                                                    HttpServletRequest request) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExtension = extension == null || extension.isBlank() ? "jpg" : extension.toLowerCase();
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String relativePath = bizType + "/" + dateDir + "/" + UUID.randomUUID() + "." + safeExtension;
        Path target = Paths.get(localDir).toAbsolutePath().normalize().resolve(relativePath);
        Files.createDirectories(target.getParent());
        file.transferTo(target);

        String publicUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(relativePath.replace('\\', '/'))
                .toUriString();
        return ApiResponse.ok(new LocalUploadData(
                System.currentTimeMillis(),
                bizType,
                publicUrl,
                publicUrl,
                file.getOriginalFilename(),
                file.getSize(),
                LocalDateTime.now().toString()
        ));
    }

    public record UploadPolicyRequest(@NotBlank String bizType, @NotBlank String fileName, @NotBlank String contentType) {
    }

    public record UploadPolicyData(String uploadUrl, String storageKey, String method, String expireAt) {
    }

    public record LocalUploadData(Long mediaId, String bizType, String url, String coverUrl,
                                  String originName, Long size, String uploadedAt) {
    }
}
