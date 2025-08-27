package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;

import java.util.List;

public interface ChatRoomRepositoryCustom {
	List<ChatRoomServiceDto.SummaryResult> findActiveChatRoomsByUser(Long userId);
}
