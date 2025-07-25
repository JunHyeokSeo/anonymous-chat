package com.anonymouschat.anonymouschatserver.domain.message.repository;

import com.anonymouschat.anonymouschatserver.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long>, MessageRepositoryCustom { }
