package plus.justice.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import plus.justice.demo.model.UserProblemEntity;

import java.util.Date;

@Repository
public interface UserProblemRepository extends JpaRepository<UserProblemEntity, Long> {
    /**
     * get UserProblemEntity by user_id and problem_id
     *
     * @param userId    long
     * @param problemID long
     */
    @Modifying(clearAutomatically = true)
    @Query("SELECT up FROM UserProblemEntity up WHERE up.user_id = :userId AND up.problem_id = :problemID")
    UserProblemEntity getSubmissionByUidAndPid(@Param("userId") long userId, @Param("problemID") long problemID);


    /**
     * update submission's status by given judger's data
     *
     * @param status    int
     * @param userId    long
     * @param problemId long
     * @param updatedAt Date
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserProblemEntity up " +
            "SET up.status = :status, up.updated_at = :updatedAt " +
            "WHERE up.user_id = :userId AND up.problem_id = :problemId")
    void updateSubmissionByID(
            @Param("status") int status,
            @Param("userId") long userId,
            @Param("problemId") long problemId,
            @Param("updatedAt") Date updatedAt
    );
}