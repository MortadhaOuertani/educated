package tn.tekup.educated.communication.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            select p.conversation from ConversationParticipant p where p.userId = :userId
            order by p.conversation.lastMessageAt desc nulls last
            """)
    List<Conversation> findAllWithParticipant(@Param("userId") UUID userId);

    @Query("""
            select c from Conversation c
            where c.conversationType = tn.tekup.educated.communication.domain.ConversationType.DIRECT
              and exists (select 1 from ConversationParticipant a where a.conversation = c and a.userId = :first)
              and exists (select 1 from ConversationParticipant b where b.conversation = c and b.userId = :second)
            """)
    List<Conversation> findDirect(@Param("first") UUID first, @Param("second") UUID second);
}
