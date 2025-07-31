package com.anonymouschat.anonymouschatserver.common.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorage {
	String upload(MultipartFile file) throws IOException;

	void delete(String fileUrl);
}
