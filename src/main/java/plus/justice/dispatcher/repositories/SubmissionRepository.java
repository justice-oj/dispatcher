package plus.justice.dispatcher.repositories;

import org.springframework.data.repository.CrudRepository;
import plus.justice.dispatcher.models.database.Submission;

public interface SubmissionRepository extends CrudRepository<Submission, Long> {
}
