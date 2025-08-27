package com.anonymouschat.anonymouschatserver.common.util;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.file.EmptyFileException;
import com.anonymouschat.anonymouschatserver.common.exception.file.UnsupportedImageFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ImageValidatorTest {

	private final ImageValidator imageValidator = new ImageValidator();

	@Nested
	@DisplayName("이미지 유효성 검사")
	class ValidateImage {

		@Test
		@DisplayName("정상적인 이미지 파일은 예외가 발생하지 않는다")
		void shouldPassWhenValidImage() {
			MockMultipartFile file = new MockMultipartFile(
					"image", "test.jpg", "image/jpeg", "dummy".getBytes()
			);

			assertThatCode(() -> imageValidator.validate(file))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("빈 파일이면 예외가 발생한다")
		void shouldThrowWhenEmptyFile() {
			MockMultipartFile file = new MockMultipartFile(
					"image", "empty.jpg", "image/jpeg", new byte[0]
			);

			assertThatThrownBy(() -> imageValidator.validate(file))
					.isInstanceOf(EmptyFileException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_IS_EMPTY);
		}

		@Test
		@DisplayName("지원하지 않는 확장자이면 예외가 발생한다")
		void shouldThrowWhenInvalidExtension() {
			MockMultipartFile file = new MockMultipartFile(
					"image", "invalid.bmp", "image/bmp", "dummy".getBytes()
			);

			assertThatThrownBy(() -> imageValidator.validate(file))
					.isInstanceOf(UnsupportedImageFormatException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_IMAGE_FORMAT);
		}

		@Test
		@DisplayName("지원하지 않는 Content-Type이면 예외가 발생한다")
		void shouldThrowWhenInvalidContentType() {
			MockMultipartFile file = new MockMultipartFile(
					"image", "test.jpg", "application/octet-stream", "dummy".getBytes()
			);

			assertThatThrownBy(() -> imageValidator.validate(file))
					.isInstanceOf(UnsupportedImageFormatException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_IMAGE_FORMAT);
		}
	}
}