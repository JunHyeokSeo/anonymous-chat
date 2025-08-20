package com.anonymouschat.anonymouschatserver.domain.type;

import lombok.Getter;

@Getter
public enum Region {

	// 광역시
	UNKNOWN("선택 안 함"),
	SEOUL("서울"),
	BUSAN("부산"),
	DAEGU("대구"),
	INCHEON("인천"),
	GWANGJU("광주"),
	DAEJEON("대전"),
	ULSAN("울산"),
	SEJONG("세종"),

	// 도 단위
	GYEONGGI("경기"),
	GANGWON("강원"),
	CHUNGBUK("충북"),
	CHUNGNAM("충남"),
	JEONBUK("전북"),
	JEONNAM("전남"),
	GYEONGBUK("경북"),
	GYEONGNAM("경남"),
	JEJU("제주");

	private final String displayName;

	Region(String displayName) {
		this.displayName = displayName;
	}
}
