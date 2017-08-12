package plus.justice.dispatcher.repositories;

import org.springframework.data.repository.CrudRepository;
import plus.justice.dispatcher.models.database.Problem;

public interface ProblemRepository extends CrudRepository<Problem, Long> {
}