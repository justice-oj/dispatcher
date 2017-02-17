package plus.justice.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import plus.justice.demo.model.SubmissionEntity;

import java.util.Date;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
    /**
     * update submission's status by given judger's data
     * @param status int
     * @param runtime int
     * @param memory int
     * @param id int
     * @param updatedAt Date
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE SubmissionEntity s " +
            "SET s.status = :status, s.runtime = :runtime, s.memory = :memory, s.updated_at = :updatedAt " +
            "WHERE s.id = :id")
    @Transactional
    void updateSubmissionByID(
            @Param("status") int status,
            @Param("runtime") int runtime,
            @Param("memory") int memory,
            @Param("id") long id,
            @Param("updatedAt") Date updatedAt
    );
}