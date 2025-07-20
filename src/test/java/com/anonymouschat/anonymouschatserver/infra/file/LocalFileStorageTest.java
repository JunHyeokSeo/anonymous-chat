package com.anonymouschat.anonymouschatserver.infra.file;

import org.junit.jupiter.api.BeforeEach;
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
		fileStorage.uploadDir = tempDir.toString(); // 직접 주입
	}

	@Test
	void upload_ValidFile_ShouldStoreFile() throws IOException {
		// given
		MockMultipartFile mockFile = new MockMultipartFile(
				"file", "test-image.jpg", "image/jpeg", "dummy image data".getBytes()
		);

		// when
		String imageUrl = fileStorage.upload(mockFile);

		// then
		assertThat(imageUrl).startsWith("/images/");
		String filename = imageUrl.replace("/images/", "");
		File savedFile = tempDir.resolve(filename).toFile();
		assertThat(savedFile.exists()).isTrue();
	}

	@Test
	void delete_ExistingFile_ShouldRemoveFile() throws IOException {
		// given
		MockMultipartFile mockFile = new MockMultipartFile(
				"file", "test.jpg", "image/jpeg", "data".getBytes()
		);
		String imageUrl = fileStorage.upload(mockFile);
		String filename = imageUrl.replace("/images/", "");
		File targetFile = tempDir.resolve(filename).toFile();
		assertThat(targetFile.exists()).isTrue();

		// when
		fileStorage.delete(imageUrl);

		// then
		assertThat(targetFile.exists()).isFalse();
	}

	@Test
	void delete_NonExistentFile_ShouldDoNothing() {
		// when/then: 예외 없이 잘 넘어가는지만 확인
		fileStorage.delete("/images/nonexistent.jpg");
	}

	@Test
	void upload_InvalidFileName_ShouldThrowException() {
		// given
		MockMultipartFile file = new MockMultipartFile("file", "invalidfile", "text/plain", "data".getBytes());

		// when/then
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
			fileStorage.upload(file);
		});
	}
}
