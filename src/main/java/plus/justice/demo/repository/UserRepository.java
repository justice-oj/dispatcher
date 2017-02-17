package plus.justice.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import plus.justice.demo.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    /**
     * update problem's submission_count and AC count
     *
     * @param ACCount int
     * @param userId  int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserEntity u " +
            "SET u.submission_count = u.submission_count + 1, u.accepted_count = u.accepted_count + :ACCount " +
            "WHERE u.id = :id")
    void updateUserSubmissionById(@Param("ACCount") int ACCount, @Param("id") long userId);
}