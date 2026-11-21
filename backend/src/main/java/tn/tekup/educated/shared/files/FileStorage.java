package tn.tekup.educated.shared.files;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import tn.tekup.educated.shared.EducatedProperties;
import tn.tekup.educated.shared.tenant.TenantContext;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/**
 * Stores uploaded files in object storage and records them as {@link StoredFile}. There is no generic
 * download endpoint: each module serves its own files after checking its own permissions.
 */
@Service
public class FileStorage {

    /** Upload size limits seen in the portals: proofs 5 MB, messages 20 MB, course materials 50 MB. */
    public static final long MAX_PROOF_BYTES = 5L * 1024 * 1024;
    public static final long MAX_MESSAGE_ATTACHMENT_BYTES = 20L * 1024 * 1024;
    public static final long MAX_MATERIAL_BYTES = 50L * 1024 * 1024;

    public static final Set<String> PROOF_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");

    /** Course materials and message attachments: documents, slides, spreadsheets, archives and images. */
    public static final Set<String> DOCUMENT_TYPES = Set.of("application/pdf", "image/jpeg", "image/png",
            "text/plain", "application/zip", "application/msword", "application/vnd.ms-powerpoint",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final StoredFileRepository repository;
    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;
    private volatile boolean bucketChecked;

    FileStorage(StoredFileRepository repository, @Lazy S3Client s3, @Lazy S3Presigner presigner,
            EducatedProperties properties) {
        this.repository = repository;
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = properties.storage().bucket();
    }

    /** Validates and stores an upload; returns the new file's id. */
    @Transactional
    public UUID store(MultipartFile file, long maxBytes, Set<String> allowedContentTypes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("file.empty", "The file is empty.");
        }
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleException("file.tooLarge", "The file exceeds " + maxBytes / (1024 * 1024) + " MB.");
        }
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (allowedContentTypes != null && !allowedContentTypes.isEmpty() && !allowedContentTypes.contains(contentType)) {
            throw new BusinessRuleException("file.type", "File type not allowed: " + contentType);
        }
        try (InputStream in = file.getInputStream()) {
            return store(in, file.getSize(), safeName(file.getOriginalFilename()), contentType);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the uploaded file", e);
        }
    }

    @Transactional
    public UUID store(InputStream content, long sizeBytes, String originalName, String contentType) {
        ensureBucket();
        UUID establishment = TenantContext.currentEstablishment()
                .orElseThrow(() -> new BusinessRuleException("tenant.missing", "No establishment selected."));
        LocalDate today = LocalDate.now();
        String key = "%s/%d/%02d/%s".formatted(establishment, today.getYear(), today.getMonthValue(), UUID.randomUUID());
        MessageDigest sha256 = sha256();
        try (DigestInputStream digesting = new DigestInputStream(content, sha256)) {
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType)
                    .contentLength(sizeBytes).build(), RequestBody.fromInputStream(digesting, sizeBytes));
        } catch (IOException e) {
            throw new IllegalStateException("Upload failed", e);
        }
        String checksum = HexFormat.of().formatHex(sha256.digest());
        return repository.save(new StoredFile(originalName, contentType, sizeBytes, key, checksum)).getId();
    }

    /** Short-lived download link. Callers must check the user may see the file first. */
    @Transactional(readOnly = true)
    public String downloadUrl(UUID fileId, Duration validity) {
        StoredFile file = get(fileId);
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(validity)
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(file.getStorageKey())
                        .responseContentDisposition("attachment; filename=\"" + file.getOriginalName() + "\"")
                        .build())
                .build();
        return presigner.presignGetObject(request).url().toString();
    }

    @Transactional(readOnly = true)
    public StoredFile get(UUID fileId) {
        return repository.findById(fileId).orElseThrow(() -> new NotFoundException("StoredFile", fileId));
    }

    private void ensureBucket() {
        if (bucketChecked) {
            return;
        }
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException missing) {
            s3.createBucket(b -> b.bucket(bucket));
        }
        bucketChecked = true;
    }

    private static String safeName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\r\\n\"]", "_");
        return base.length() > 200 ? base.substring(base.length() - 200) : base;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
