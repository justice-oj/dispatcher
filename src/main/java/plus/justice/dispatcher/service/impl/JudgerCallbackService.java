package plus.justice.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import plus.justice.demo.model.UserProblemEntity;
import plus.justice.demo.repository.ProblemRepository;
import plus.justice.demo.repository.SubmissionRepository;
import plus.justice.demo.repository.UserProblemRepository;
import plus.justice.demo.repository.UserRepository;
import plus.justice.dispatcher.service.IJudgerCallbackService;

import java.util.Date;

public class JudgerCallbackService implements IJudgerCallbackService {
    private final ProblemRepository problemRepository;

    private final SubmissionRepository submissionRepository;

    private final UserProblemRepository userProblemRepository;

    private final UserRepository userRepository;

    @Autowired
    public JudgerCallbackService(ProblemRepository problemRepository, SubmissionRepository submissionRepository, UserProblemRepository userProblemRepository, UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.userProblemRepository = userProblemRepository;
        this.userRepository = userRepository;
    }

    public void call(long sid, long uid, long pid, int status, int runtime, int memory) {
        Date updatedAt = new Date();

        // TODO: transactional?
        submissionRepository.updateSubmissionByID(status, runtime, memory, sid, updatedAt);

        // TODO: enum
        int ACCount = status == 0 ? 1 : 0;
        problemRepository.updateProblemSubmissionByID(ACCount, pid);
        userRepository.updateUserSubmissionById(ACCount, uid);

        // TODO: enum
        UserProblemEntity userProblemEntity = userProblemRepository.getSubmissionByUidAndPid(uid, pid);
        int originStatus = userProblemEntity.getStatus();
        // first submission or get a correct submission
        if (originStatus == 0 || (originStatus == 2 && status == 0)) {
            userProblemRepository.updateSubmissionByID(status, uid, pid, updatedAt);
        }
    }
}
