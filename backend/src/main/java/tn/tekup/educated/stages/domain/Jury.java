package tn.tekup.educated.stages.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A defense jury. */
@Entity
@Table(name = "jury", schema = "stages")
public class Jury extends TenantEntity {

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false)
    private LocalDate juryDate;

    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defense_session_id", nullable = false)
    private DefenseSession defenseSession;

    @OneToMany(mappedBy = "jury", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JuryMember> members = new ArrayList<>();

    public Jury() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getJuryDate() {
        return juryDate;
    }

    public void setJuryDate(LocalDate juryDate) {
        this.juryDate = juryDate;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public DefenseSession getDefenseSession() {
        return defenseSession;
    }

    public void setDefenseSession(DefenseSession defenseSession) {
        this.defenseSession = defenseSession;
    }

    public List<JuryMember> getMembers() {
        return members;
    }

}
