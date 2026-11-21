package tn.tekup.educated.parametrage.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.domain.AcademicYearRepository;
import tn.tekup.educated.parametrage.domain.Cycle;
import tn.tekup.educated.parametrage.domain.CycleRepository;
import tn.tekup.educated.parametrage.domain.Program;
import tn.tekup.educated.parametrage.domain.ProgramRepository;
import tn.tekup.educated.parametrage.domain.SchoolClass;
import tn.tekup.educated.parametrage.domain.SchoolClassRepository;
import tn.tekup.educated.parametrage.domain.SiteRepository;
import tn.tekup.educated.parametrage.domain.Specialization;
import tn.tekup.educated.parametrage.domain.SpecializationRepository;
import tn.tekup.educated.parametrage.domain.StudyLevel;
import tn.tekup.educated.parametrage.domain.StudyLevelRepository;
import tn.tekup.educated.parametrage.domain.StudyRhythm;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.web.NotFoundException;

/** Programs, cycles, specializations, study levels and classes. */
@Service
@Transactional
public class StructureService {

    private final CycleRepository cycles;
    private final ProgramRepository programs;
    private final SpecializationRepository specializations;
    private final StudyLevelRepository studyLevels;
    private final SchoolClassRepository schoolClasses;
    private final AcademicYearRepository academicYears;
    private final SiteRepository sites;

    public StructureService(CycleRepository cycles, ProgramRepository programs,
            SpecializationRepository specializations, StudyLevelRepository studyLevels,
            SchoolClassRepository schoolClasses, AcademicYearRepository academicYears, SiteRepository sites) {
        this.cycles = cycles;
        this.programs = programs;
        this.specializations = specializations;
        this.studyLevels = studyLevels;
        this.schoolClasses = schoolClasses;
        this.academicYears = academicYears;
        this.sites = sites;
    }

    @Transactional(readOnly = true)
    public List<Cycle> cycles() {
        return cycles.findAllByOrderByCode();
    }

    public Cycle createCycle(String code, String name, Integer durationYears) {
        Cycle cycle = new Cycle();
        cycle.setCode(code);
        cycle.setName(LocalizedText.of(name));
        cycle.setDurationYears(durationYears);
        return cycles.save(cycle);
    }

    @Transactional(readOnly = true)
    public List<Program> programs() {
        return programs.findAllByOrderByCode();
    }

    public Program createProgram(String code, String name, String diplomaTitle) {
        Program program = new Program();
        program.setCode(code);
        program.setName(LocalizedText.of(name));
        program.setDiplomaTitle(diplomaTitle);
        program.setActive(true);
        return programs.save(program);
    }

    @Transactional(readOnly = true)
    public List<Specialization> specializations() {
        return specializations.findAllByOrderByCode();
    }

    public Specialization createSpecialization(String code, String name, UUID programId) {
        Specialization specialization = new Specialization();
        specialization.setCode(code);
        specialization.setName(LocalizedText.of(name));
        specialization.setProgram(programs.findById(programId).orElseThrow(() -> new NotFoundException("Program", programId)));
        return specializations.save(specialization);
    }

    @Transactional(readOnly = true)
    public List<StudyLevel> studyLevels() {
        return studyLevels.findAllByOrderByCode();
    }

    public StudyLevel createStudyLevel(String code, String name, int yearOfStudy, StudyRhythm rhythm, UUID programId,
            UUID cycleId, UUID specializationId) {
        StudyLevel level = new StudyLevel();
        level.setCode(code);
        level.setName(LocalizedText.of(name));
        level.setYearOfStudy(yearOfStudy);
        level.setRhythm(rhythm);
        level.setActive(true);
        level.setProgram(programs.findById(programId).orElseThrow(() -> new NotFoundException("Program", programId)));
        level.setCycle(cycles.findById(cycleId).orElseThrow(() -> new NotFoundException("Cycle", cycleId)));
        if (specializationId != null) {
            level.setSpecialization(specializations.findById(specializationId)
                    .orElseThrow(() -> new NotFoundException("Specialization", specializationId)));
        }
        return studyLevels.save(level);
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> schoolClasses(UUID academicYearId, UUID studyLevelId) {
        if (studyLevelId == null) {
            return schoolClasses.findAllByAcademicYear_IdOrderByCode(academicYearId);
        }
        return schoolClasses.findAllByAcademicYear_IdAndStudyLevel_IdOrderByCode(academicYearId, studyLevelId);
    }

    public SchoolClass createSchoolClass(String code, String name, Integer capacity, UUID studyLevelId,
            UUID academicYearId, UUID siteId) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setActive(true);
        schoolClass.setStudyLevel(studyLevels.findById(studyLevelId)
                .orElseThrow(() -> new NotFoundException("StudyLevel", studyLevelId)));
        schoolClass.setAcademicYear(academicYears.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("AcademicYear", academicYearId)));
        return updateSchoolClass(schoolClass, code, name, capacity, siteId);
    }

    public SchoolClass updateSchoolClass(UUID id, String code, String name, Integer capacity, UUID siteId) {
        SchoolClass schoolClass = schoolClasses.findById(id).orElseThrow(() -> new NotFoundException("SchoolClass", id));
        return updateSchoolClass(schoolClass, code, name, capacity, siteId);
    }

    private SchoolClass updateSchoolClass(SchoolClass schoolClass, String code, String name, Integer capacity, UUID siteId) {
        schoolClass.setCode(code);
        schoolClass.setName(name);
        schoolClass.setCapacity(capacity);
        schoolClass.setSite(siteId == null ? null
                : sites.findById(siteId).orElseThrow(() -> new NotFoundException("Site", siteId)));
        return schoolClasses.save(schoolClass);
    }
}
