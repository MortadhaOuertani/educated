package tn.tekup.educated.parametrage.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.domain.AcademicPeriod;
import tn.tekup.educated.parametrage.domain.AcademicPeriodRepository;
import tn.tekup.educated.parametrage.domain.AcademicYear;
import tn.tekup.educated.parametrage.domain.AcademicYearRepository;
import tn.tekup.educated.parametrage.domain.ProgramRepository;
import tn.tekup.educated.parametrage.domain.Room;
import tn.tekup.educated.parametrage.domain.RoomRepository;
import tn.tekup.educated.parametrage.domain.SchoolClass;
import tn.tekup.educated.parametrage.domain.SchoolClassRepository;
import tn.tekup.educated.parametrage.domain.StudyLevelRepository;

/** Read-only lookups other modules use to check the ids they store. */
@Service
@Transactional(readOnly = true)
public class ParametrageDirectory {

    private final SchoolClassRepository schoolClasses;
    private final StudyLevelRepository studyLevels;
    private final AcademicYearRepository academicYears;
    private final ProgramRepository programs;
    private final AcademicPeriodRepository periods;
    private final RoomRepository rooms;

    ParametrageDirectory(SchoolClassRepository schoolClasses, StudyLevelRepository studyLevels,
            AcademicYearRepository academicYears, ProgramRepository programs, AcademicPeriodRepository periods,
            RoomRepository rooms) {
        this.schoolClasses = schoolClasses;
        this.studyLevels = studyLevels;
        this.academicYears = academicYears;
        this.programs = programs;
        this.periods = periods;
        this.rooms = rooms;
    }

    public Optional<SchoolClassInfo> findSchoolClass(UUID id) {
        return schoolClasses.findById(id).map(ParametrageDirectory::toInfo);
    }

    public List<SchoolClassInfo> schoolClassesOf(UUID academicYearId) {
        return schoolClasses.findAllByAcademicYear_IdOrderByCode(academicYearId).stream()
                .map(ParametrageDirectory::toInfo)
                .toList();
    }

    public Optional<AcademicYearInfo> currentAcademicYear() {
        return academicYears.findFirstByCurrentTrue().map(ParametrageDirectory::toInfo);
    }

    public Optional<AcademicYearInfo> findAcademicYear(UUID id) {
        return academicYears.findById(id).map(ParametrageDirectory::toInfo);
    }

    public boolean studyLevelExists(UUID id) {
        return id != null && studyLevels.existsById(id);
    }

    public boolean programExists(UUID id) {
        return id != null && programs.existsById(id);
    }

    public Optional<AcademicPeriodInfo> findAcademicPeriod(UUID id) {
        return periods.findById(id).map(ParametrageDirectory::toInfo);
    }

    public List<AcademicPeriodInfo> periodsOf(UUID academicYearId) {
        return periods.findAllByAcademicYear_IdOrderByOrderIndex(academicYearId).stream()
                .map(ParametrageDirectory::toInfo)
                .toList();
    }

    public List<RoomInfo> rooms() {
        return rooms.findAllByOrderByCode().stream().filter(Room::isActive).map(ParametrageDirectory::toInfo).toList();
    }

    public Optional<RoomInfo> findRoom(UUID id) {
        return rooms.findById(id).map(ParametrageDirectory::toInfo);
    }

    private static AcademicPeriodInfo toInfo(AcademicPeriod p) {
        return new AcademicPeriodInfo(p.getId(), p.getCode(), p.getName() == null ? p.getCode() : p.getName().fr(),
                p.getOrderIndex() == null ? 0 : p.getOrderIndex(), p.getStartDate(), p.getEndDate(),
                p.getAcademicYear().getId());
    }

    private static RoomInfo toInfo(Room r) {
        return new RoomInfo(r.getId(), r.getCode(), r.getName(), r.getCapacity(),
                r.getRoomType() == null ? null : r.getRoomType().name());
    }

    private static SchoolClassInfo toInfo(SchoolClass c) {
        return new SchoolClassInfo(c.getId(), c.getCode(), c.getName(), c.getStudyLevel().getId(),
                c.getAcademicYear().getId());
    }

    private static AcademicYearInfo toInfo(AcademicYear y) {
        return new AcademicYearInfo(y.getId(), y.getCode(), y.getStartDate(), y.getEndDate());
    }
}
