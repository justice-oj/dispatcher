package plus.justice.dispatcher.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.repositories.SubmissionRepository;

@Controller
@RequestMapping(path = "/api")
public class IndexController {
    private final SubmissionRepository submissionRepository;

    public IndexController(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @GetMapping(path = "/submission")
    public @ResponseBody Submission getSubmission(@RequestParam Long id) {
        return submissionRepository.findOne(id);
    }
}