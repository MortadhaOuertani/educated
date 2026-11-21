package tn.tekup.educated.examens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "exam_room_allocation", schema = "examens")
public class ExamRoomAllocation extends TenantEntity {

    @Column(nullable = false)
    private UUID roomId;

    private UUID schoolClassId;

    private Integer seatCount;

    @Column(length = 100)
    private String seatingRange;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public ExamRoomAllocation() {
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public UUID getSchoolClassId() {
        return schoolClassId;
    }

    public void setSchoolClassId(UUID schoolClassId) {
        this.schoolClassId = schoolClassId;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public String getSeatingRange() {
        return seatingRange;
    }

    public void setSeatingRange(String seatingRange) {
        this.seatingRange = seatingRange;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

}
