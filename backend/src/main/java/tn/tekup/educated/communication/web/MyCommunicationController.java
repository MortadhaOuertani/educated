package tn.tekup.educated.communication.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.communication.domain.NotificationType;
import tn.tekup.educated.communication.service.ChatService;
import tn.tekup.educated.communication.service.MessageService;
import tn.tekup.educated.communication.service.NotificationService;
import tn.tekup.educated.communication.web.CommunicationDtos.ChatMessageDto;
import tn.tekup.educated.communication.web.CommunicationDtos.ConversationDto;
import tn.tekup.educated.communication.web.CommunicationDtos.InboxDto;
import tn.tekup.educated.communication.web.CommunicationDtos.NewsDto;
import tn.tekup.educated.communication.web.CommunicationDtos.NotificationDto;
import tn.tekup.educated.communication.web.CommunicationDtos.PreferenceDto;
import tn.tekup.educated.communication.web.CommunicationDtos.UnreadDto;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** What every signed-in user sees: notifications, received messages, news and discussions. */
@RestController
@RequestMapping("/api/me")
class MyCommunicationController {

    record PreferenceRequest(@NotNull NotificationType type, boolean inApp, boolean push, boolean email, boolean sms) {
    }

    record NewConversationRequest(@NotNull UUID userId) {
    }

    record ChatRequest(@NotBlank String content) {
    }

    private final CurrentUserProvider currentUserProvider;
    private final NotificationService notifications;
    private final MessageService messages;
    private final ChatService chat;

    MyCommunicationController(CurrentUserProvider currentUserProvider, NotificationService notifications,
            MessageService messages, ChatService chat) {
        this.currentUserProvider = currentUserProvider;
        this.notifications = notifications;
        this.messages = messages;
        this.chat = chat;
    }

    @GetMapping("/notifications")
    List<NotificationDto> notifications() {
        return notifications.latest(me()).stream().map(NotificationDto::from).toList();
    }

    @GetMapping("/notifications/unread-count")
    UnreadDto unreadCount() {
        return new UnreadDto(notifications.unreadCount(me()));
    }

    @PostMapping("/notifications/{id}/read")
    NotificationDto markRead(@PathVariable UUID id) {
        return NotificationDto.from(notifications.markRead(id, me()));
    }

    @PostMapping("/notifications/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void markAllRead() {
        notifications.markAllRead(me());
    }

    @GetMapping("/notification-preferences")
    List<PreferenceDto> preferences() {
        return notifications.preferencesOf(me()).stream().map(PreferenceDto::from).toList();
    }

    @PutMapping("/notification-preferences")
    PreferenceDto setPreference(@Valid @RequestBody PreferenceRequest r) {
        return PreferenceDto.from(notifications.setPreference(me(), r.type(), r.inApp(), r.push(), r.email(), r.sms()));
    }

    @GetMapping("/messages")
    @Transactional(readOnly = true)
    List<InboxDto> inbox() {
        return messages.inbox(me()).stream().map(InboxDto::from).toList();
    }

    @PostMapping("/messages/{id}/read")
    @Transactional
    InboxDto markMessageRead(@PathVariable UUID id) {
        return InboxDto.from(messages.markRead(id, me()));
    }

    @GetMapping("/news")
    List<NewsDto> news() {
        return messages.newsFor(currentUserProvider.require()).stream().map(NewsDto::from).toList();
    }

    @GetMapping("/conversations")
    List<ConversationDto> conversations() {
        return chat.conversationsOf(me()).stream().map(ConversationDto::from).toList();
    }

    @PostMapping("/conversations")
    @Transactional
    ConversationDto startConversation(@Valid @RequestBody NewConversationRequest r) {
        return ConversationDto.from(chat.directConversation(me(), r.userId()));
    }

    @GetMapping("/conversations/{id}/messages")
    List<ChatMessageDto> conversationMessages(@PathVariable UUID id) {
        return chat.messages(id, me()).stream().map(ChatMessageDto::from).toList();
    }

    @PostMapping("/conversations/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ChatMessageDto sendChat(@PathVariable UUID id, @Valid @RequestBody ChatRequest r) {
        return ChatMessageDto.from(chat.send(id, me(), r.content()));
    }

    private UUID me() {
        return currentUserProvider.require().userId();
    }
}
