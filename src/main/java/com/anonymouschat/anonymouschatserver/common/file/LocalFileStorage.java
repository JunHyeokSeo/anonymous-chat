package com.anonymouschat.anonymouschatserver.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class LocalFileStorage implements FileStorage{
	@Value("${file.upload-dir}")
	public String uploadDir;

	@Override
	public String upload(MultipartFile file) throws IOException {
		File dir = new File(uploadDir);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("업로드 디렉토리 생성 실패: " + uploadDir);
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new IllegalArgumentException("잘못된 파일 이름입니다.");
		}

		String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
		String filename = UUID.randomUUID() + ext;

		File targetFile = new File(uploadDir + File.separator + filename);
		file.transferTo(targetFile);

		return "/images/" + filename;
	}

	@Override
	public void delete(String fileUrl) {
		if (fileUrl == null || fileUrl.isBlank()) return;

		String filename = Paths.get(fileUrl).getFileName().toString();
		File file = new File(uploadDir, filename);
		if (file.exists() && !file.delete()) {
			log.warn("파일 삭제 실패: {}", file.getAbsolutePath());
		}
	}
}
