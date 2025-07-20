package com.anonymouschat.anonymouschatserver.common.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ImageValidator {

	private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
	private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/gif");

	public void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어 있습니다.");
		}

		if (!hasValidExtension(file) || !hasValidMimeType(file)) {
			throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
		}
	}

	private static boolean hasValidExtension(MultipartFile file) {
		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
		return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
	}

	private static boolean hasValidMimeType(MultipartFile file) {
		return ALLOWED_CONTENT_TYPES.contains(file.getContentType());
	}
}
