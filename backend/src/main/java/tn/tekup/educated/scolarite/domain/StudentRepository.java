package tn.tekup.educated.scolarite.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByMatricule(String matricule);

    @Query("""
            select s from Student s
            where s.archivedAt is null
              and (:text = ''
                or lower(s.matricule) like lower(concat('%', :text, '%'))
                or lower(s.person.firstName) like lower(concat('%', :text, '%'))
                or lower(s.person.lastName) like lower(concat('%', :text, '%')))
            """)
    Page<Student> search(@Param("text") String text, Pageable pageable);
}
