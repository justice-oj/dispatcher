package plus.justice.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import plus.justice.demo.model.ProblemEntity;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {
    /**
     * update problem's submission_count and AC count
     *
     * @param ACCount   int
     * @param problemId int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ProblemEntity p " +
            "SET p.submission_count = p.submission_count + 1, p.accepted_count = p.accepted_count + :ACCount " +
            "WHERE p.id = :id")
    void updateProblemSubmissionByID(@Param("ACCount") int ACCount, @Param("id") long problemId);
}