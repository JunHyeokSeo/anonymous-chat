package com.anonymouschat.anonymouschatserver.infra.file;

import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileStorageTest {

	private LocalFileStorage fileStorage;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		fileStorage = new LocalFileStorage();
		fileStorage.uploadDir = tempDir.toString();
	}

	@Test
	@DisplayName("파일 업로드: 유효한 파일은 저장되어야 한다")
	void upload_ValidFile_ShouldStoreFile() throws IOException {
		// given
		MockMultipartFile mockFile = new MockMultipartFile(
				"file", "test-image.jpg", "image/jpeg", "dummy image data".getBytes()
		);

		// when
		String imageUrl = fileStorage.upload(mockFile);

		// then
		assertThat(imageUrl).startsWith("/uploads/");
		String filename = imageUrl.replace("/uploads/", "");
		File savedFile = tempDir.resolve(filename).toFile();
		assertThat(savedFile.exists()).isTrue();
	}

	@Test
	@DisplayName("파일 삭제: 존재하는 파일은 삭제되어야 한다")
	void delete_ExistingFile_ShouldRemoveFile() throws IOException {
		// given
		MockMultipartFile mockFile = new MockMultipartFile(
				"file", "test.jpg", "image/jpeg", "data".getBytes()
		);
		String imageUrl = fileStorage.upload(mockFile);
		String filename = imageUrl.replace("/uploads/", "");
		File targetFile = tempDir.resolve(filename).toFile();
		assertThat(targetFile.exists()).isTrue();

		// when
		fileStorage.delete(imageUrl);

		// then
		assertThat(targetFile.exists()).isFalse();
	}

	@Test
	@DisplayName("파일 삭제: 존재하지 않는 파일을 삭제해도 예외가 발생하지 않아야 한다")
	void delete_NonExistentFile_ShouldDoNothing() {
		fileStorage.delete("/uploads/nonexistent.jpg");
	}

	@Test
	@DisplayName("파일 업로드: 확장자가 없는 파일은 예외를 발생시켜야 한다")
	void upload_InvalidFileName_ShouldThrowException() {
		// given
		MockMultipartFile file = new MockMultipartFile("file", "invalidfile", "text/plain", "data".getBytes());

		// when/then
		org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class, () -> fileStorage.upload(file));
	}
}