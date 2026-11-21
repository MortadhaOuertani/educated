package tn.tekup.educated.communication.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.communication.domain.MessageCategory;
import tn.tekup.educated.communication.service.MessageService;
import tn.tekup.educated.communication.service.MessageService.MessageData;
import tn.tekup.educated.communication.web.CommunicationDtos.NewsDto;
import tn.tekup.educated.communication.web.CommunicationDtos.SentMessageDto;
import tn.tekup.educated.shared.domain.Audience;

/** Back-office screens of the Communication module: messages to an audience and news. */
@RestController
@RequestMapping("/api/communication")
class CommunicationController {

    record MessageRequest(@NotBlank @Size(max = 255) String subject, String body, @NotNull Audience audience,
            MessageCategory category, UUID schoolClassId, boolean sendPush, boolean sendSms, boolean sendEmail) {
    }

    record NewsRequest(@NotBlank @Size(max = 255) String title, String body, @NotNull Audience audience) {
    }

    private final MessageService messages;

    CommunicationController(MessageService messages) {
        this.messages = messages;
    }

    @GetMapping("/messages")
    @PreAuthorize("@perm.can('Message', 'READ')")
    @Transactional(readOnly = true)
    List<SentMessageDto> sent() {
        return messages.sentByMe().stream().map(SentMessageDto::from).toList();
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Message', 'CREATE')")
    @Transactional
    SentMessageDto send(@Valid @RequestBody MessageRequest r) {
        return SentMessageDto.from(messages.send(new MessageData(r.subject(), r.body(), r.audience(), r.category(),
                r.schoolClassId(), r.sendPush(), r.sendSms(), r.sendEmail())));
    }

    @GetMapping("/news")
    @PreAuthorize("@perm.can('News', 'READ')")
    List<NewsDto> news() {
        return messages.allNews().stream().map(NewsDto::from).toList();
    }

    @PostMapping("/news")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('News', 'CREATE')")
    NewsDto createNews(@Valid @RequestBody NewsRequest r) {
        return NewsDto.from(messages.createNews(r.title(), r.body(), r.audience()));
    }

    @PostMapping("/news/{id}/publish")
    @PreAuthorize("@perm.can('News', 'WRITE')")
    @Transactional
    NewsDto publishNews(@PathVariable UUID id) {
        return NewsDto.from(messages.publishNews(id));
    }
}
