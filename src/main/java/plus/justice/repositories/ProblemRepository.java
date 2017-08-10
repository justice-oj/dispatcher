package plus.justice.repositories;

import org.springframework.data.repository.CrudRepository;
import plus.justice.models.database.Problem;

public interface ProblemRepository extends CrudRepository<Problem, Long> {
}