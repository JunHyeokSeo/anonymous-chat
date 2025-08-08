package com.anonymouschat.anonymouschatserver.common.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
TODO [Refactor-ImageValidation] Bean Validation 기반으로 이미지 검증 리팩토링하기
-----------------------------------------------------------------------------------
목표
- 현재 서비스 계층(UserService 등)에서 수동 호출하던 ImageValidator를 제거하고,
  컨트롤러 입력 시점에서 Bean Validation(@Valid/@Validated)으로 자동 검증되도록 전환한다.
- 단일 파일(MultipartFile)뿐 아니라 List<MultipartFile> 등 컬렉션 요소도 개별 검증한다.

작업 개요
1) 커스텀 제약 어노테이션/검증기 추가
   - 패키지: web.http.validation
   - 파일:
     - @ImageFile (annotation)  // 이미지 확장자/MIME, 선택 업로드 허용 여부 등 정책 보유
     - ImageFileValidator implements ConstraintValidator<ImageFile, MultipartFile>
   - 기능:
     - 허용 확장자: jpg, jpeg, png, gif
     - 허용 MIME: image/jpeg, image/png, image/gif
     - allowEmpty() 옵션 제공: 선택 업로드 필드에서 비어 있어도 통과 가능

2) DTO에 제약 적용
   - 단일 파일:
       public record UploadProfileImageRequest(
           @ImageFile MultipartFile image
       ) {}
   - 다중 파일(컨테이너 요소 제약):
       public record UploadProfileImagesRequest(
           List<@ImageFile MultipartFile> images
       ) {}
     ※ @ImageFile에 @Target(TYPE_USE) 포함하여 타입-유즈 제약 가능하게 할 것.

3) 컨트롤러에서 검증 트리거
   - 컨트롤러 클래스에 @Validated 추가(또는 메서드 파라미터에 @Valid 적용)
   - 예:
       @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
       public ApiResponse<Void> upload(@ModelAttribute @Valid UploadProfileImagesRequest req) { ... }

4) 서비스 계층 정리
   - UserService 등에서 이 유틸(현재 클래스) 직접 호출 제거.
   - 입력 검증 실패는 컨트롤러 진입 전에 400으로 반환되도록 GlobalExceptionHandler에서 처리 일원화.
   - 본 클래스(common.util.ImageValidator)는 리팩토링 완료 후 삭제 대상.

5) 예외/응답 정책
   - 검증 실패 메시지는 @ImageFile.message() 또는 Validator에서 구성.
   - 공통 응답 형식(ApiResponse 등)은 GlobalExceptionHandler에서 BindException/MethodArgumentNotValidException 매핑.

6) 테스트
   - 단위 테스트: ImageFileValidator에 대한 isValid() 케이스(정상/확장자 오류/MIME 오류/빈 파일/allowEmpty=true) 검증.
   - MVC 테스트: 잘못된 파일 업로드 시 400 + 필드 에러 메시지 검증.
   - 리스트 검증: List<@ImageFile MultipartFile>에서 일부 원소만 실패해도 전체 400이어야 함.

참고 구현 스케치
-----------------------------------------------------------------------------------
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = ImageFileValidator.class)
public @interface ImageFile {
    String message() default "지원하지 않는 이미지 형식입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean allowEmpty() default false;
}

public class ImageFileValidator implements ConstraintValidator<ImageFile, MultipartFile> {
    private static final Set<String> EXT = Set.of("jpg","jpeg","png","gif");
    private static final Set<String> MIME = Set.of("image/jpeg","image/png","image/gif");
    private boolean allowEmpty;

    @Override public void initialize(ImageFile ann) { this.allowEmpty = ann.allowEmpty(); }

    @Override public boolean isValid(MultipartFile file, ConstraintValidatorContext c) {
        if (file == null || file.isEmpty()) return allowEmpty;
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        return ext != null && EXT.contains(ext.toLowerCase()) && MIME.contains(file.getContentType());
    }
}
-----------------------------------------------------------------------------------
완료 기준
- 컨트롤러에 @Valid 적용만으로 이미지 형식 검증이 동작하고, 서비스 계층에서 별도 유틸 호출이 없다.
- 단일/다중 업로드 모두 동일 정책으로 검증되며, 실패 시 400 + 표준 에러 응답으로 귀결된다.
*/

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
