package com.anonymouschat.anonymouschatserver.infra.file;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import com.anonymouschat.anonymouschatserver.common.exception.file.FileUploadException;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
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
			throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED);
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new BadRequestException(ErrorCode.INVALID_FILE_NAME);
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
			log.warn("{}File deletion failed: path={}", LogTag.IMAGE, file.getAbsolutePath());
		}
	}
}