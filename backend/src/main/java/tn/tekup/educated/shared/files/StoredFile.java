package tn.tekup.educated.shared.files;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.ScanStatus;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Metadata of a file kept in object storage. Every {@code ...FileId} in the model points here. */
@Entity
@Table(name = "stored_file", schema = "shared")
public class StoredFile extends TenantEntity {

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, length = 150)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false, length = 64)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus scanStatus = ScanStatus.PENDING;

    protected StoredFile() {
    }

    StoredFile(String originalName, String contentType, long sizeBytes, String storageKey, String checksumSha256) {
        this.originalName = originalName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
        this.checksumSha256 = checksumSha256;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public ScanStatus getScanStatus() {
        return scanStatus;
    }

    public void markScanned(ScanStatus status) {
        this.scanStatus = status;
    }
}
