package plus.justice.repositories;

import org.springframework.data.repository.CrudRepository;
import plus.justice.models.database.Submission;

public interface SubmissionRepository extends CrudRepository<Submission, Long> {
}
