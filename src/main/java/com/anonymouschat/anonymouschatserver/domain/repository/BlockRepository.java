package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
	Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	@Query("SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :blockerId AND b.active = true")
	List<Long> findAllBlockedUserIdsByBlockerId(@Param("blockerId") Long blockerId);
}
