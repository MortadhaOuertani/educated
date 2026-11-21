package tn.tekup.educated.communication.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.api.AccountDirectory;
import tn.tekup.educated.communication.domain.Message;
import tn.tekup.educated.communication.domain.MessageAudienceFilter;
import tn.tekup.educated.communication.domain.MessageCategory;
import tn.tekup.educated.communication.domain.MessageRecipient;
import tn.tekup.educated.communication.domain.MessageRecipientRepository;
import tn.tekup.educated.communication.domain.MessageRepository;
import tn.tekup.educated.communication.domain.News;
import tn.tekup.educated.communication.domain.NewsRepository;
import tn.tekup.educated.communication.domain.NewsStatus;
import tn.tekup.educated.communication.domain.NotificationType;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.Audience;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Messages to learners, teachers or staff (optionally one class), and the news published in the apps. */
@Service
@Transactional
public class MessageService {

    public record MessageData(String subject, String body, Audience audience, MessageCategory category,
            UUID schoolClassId, boolean sendPush, boolean sendSms, boolean sendEmail) {
    }

    private final MessageRepository messages;
    private final MessageRecipientRepository recipients;
    private final NewsRepository news;
    private final NotificationService notifications;
    private final AccountDirectory accounts;
    private final StudentDirectory students;
    private final ParametrageDirectory parametrage;
    private final CurrentUserProvider currentUserProvider;

    public MessageService(MessageRepository messages, MessageRecipientRepository recipients, NewsRepository news,
            NotificationService notifications, AccountDirectory accounts, StudentDirectory students,
            ParametrageDirectory parametrage, CurrentUserProvider currentUserProvider) {
        this.messages = messages;
        this.recipients = recipients;
        this.news = news;
        this.notifications = notifications;
        this.accounts = accounts;
        this.students = students;
        this.parametrage = parametrage;
        this.currentUserProvider = currentUserProvider;
    }

    /** Sends the message to everyone in the audience (one class only when a class is given). */
    public Message send(MessageData data) {
        if (data.schoolClassId() != null && parametrage.findSchoolClass(data.schoolClassId()).isEmpty()) {
            throw new NotFoundException("SchoolClass", data.schoolClassId());
        }
        Set<UUID> userIds = audienceUsers(data.audience(), data.schoolClassId());
        userIds.remove(currentUserProvider.require().userId());
        if (userIds.isEmpty()) {
            throw new BusinessRuleException("message.noRecipient", "Nobody with an account matches this audience.");
        }
        Message message = new Message();
        message.setSubject(data.subject());
        message.setBody(data.body());
        message.setAudience(data.audience());
        message.setCategory(data.category() == null ? MessageCategory.OTHER : data.category());
        message.setSendPush(data.sendPush());
        message.setSendSms(data.sendSms());
        message.setSendEmail(data.sendEmail());
        message.setSentAt(Instant.now());
        MessageAudienceFilter filter = new MessageAudienceFilter();
        filter.setMessage(message);
        filter.setAllRecipients(data.schoolClassId() == null);
        filter.setSchoolClassId(data.schoolClassId());
        message.getFilters().add(filter);
        for (UUID userId : userIds) {
            MessageRecipient recipient = new MessageRecipient();
            recipient.setMessage(message);
            recipient.setRecipientUserId(userId);
            recipient.setDeliveredAt(message.getSentAt());
            message.getRecipients().add(recipient);
        }
        message = messages.save(message);
        notifications.notifyUsers(userIds, NotificationType.MESSAGE, data.subject(), "Nouveau message", "Message",
                message.getId());
        return message;
    }

    @Transactional(readOnly = true)
    public List<Message> sentByMe() {
        return messages.findAllByCreatedByUserIdOrderByCreatedAtDesc(currentUserProvider.require().userId());
    }

    @Transactional(readOnly = true)
    public List<MessageRecipient> inbox(UUID userId) {
        return recipients.findAllByRecipientUserIdAndArchivedFalseOrderByCreatedAtDesc(userId);
    }

    public MessageRecipient markRead(UUID recipientId, UUID userId) {
        MessageRecipient recipient = recipients.findByIdAndRecipientUserId(recipientId, userId)
                .orElseThrow(() -> new NotFoundException("Message", recipientId));
        if (recipient.getReadAt() == null) {
            recipient.setReadAt(Instant.now());
        }
        return recipient;
    }

    @Transactional(readOnly = true)
    public List<News> allNews() {
        return news.findAllByOrderByCreatedAtDesc();
    }

    public News createNews(String title, String body, Audience audience) {
        News item = new News();
        item.setTitle(title);
        item.setBody(body);
        item.setAudience(audience);
        item.setStatus(NewsStatus.DRAFT);
        return news.save(item);
    }

    /** Publishing notifies the audience once. */
    public News publishNews(UUID id) {
        News item = news.findById(id).orElseThrow(() -> new NotFoundException("News", id));
        if (item.getStatus() == NewsStatus.PUBLISHED) {
            throw new BusinessRuleException("news.published", "This news item is already published.");
        }
        Instant now = Instant.now();
        item.setStatus(NewsStatus.PUBLISHED);
        item.setPublishedAt(now);
        item.setPublishedByUserId(currentUserProvider.require().userId());
        item.setNotifiedAt(now);
        notifications.notifyUsers(audienceUsers(item.getAudience(), null), NotificationType.NEWS, item.getTitle(),
                "Nouvelle actualité", "News", item.getId());
        return item;
    }

    /** Published news whose audience includes the user. */
    @Transactional(readOnly = true)
    public List<News> newsFor(CurrentUser user) {
        List<Audience> audiences = new ArrayList<>(List.of(Audience.ALL));
        if (user.personType() == null || user.personType() == PersonType.STAFF) {
            audiences.add(Audience.STAFF);
        } else {
            audiences.add(switch (user.personType()) {
                case STUDENT -> Audience.STUDENTS;
                case TEACHER -> Audience.TEACHERS;
                case GUARDIAN -> Audience.GUARDIANS;
                case STAFF -> Audience.STAFF;
            });
        }
        return news.findAllByStatusAndAudienceInOrderByPublishedAtDesc(NewsStatus.PUBLISHED, audiences);
    }

    private Set<UUID> audienceUsers(Audience audience, UUID schoolClassId) {
        Set<UUID> userIds = new LinkedHashSet<>();
        boolean students = audience == Audience.ALL || audience == Audience.STUDENTS;
        if (students) {
            userIds.addAll(schoolClassId != null
                    ? accounts.userIdsOf(PersonType.STUDENT, this.students.studentIdsInClass(schoolClassId))
                    : accounts.usersOfEstablishment(PersonType.STUDENT));
        }
        if (audience == Audience.ALL || audience == Audience.TEACHERS) {
            userIds.addAll(accounts.usersOfEstablishment(PersonType.TEACHER));
        }
        if (audience == Audience.ALL || audience == Audience.STAFF) {
            userIds.addAll(accounts.usersOfEstablishment(PersonType.STAFF));
        }
        if (audience == Audience.GUARDIANS) {
            userIds.addAll(accounts.usersOfEstablishment(PersonType.GUARDIAN));
        }
        return userIds;
    }
}
