package plus.justice.demo.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_problem")
public class ProblemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Basic
    @Column(name = "title", nullable = false)
    private String title;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "input")
    private String input;

    @Basic
    @Column(name = "output")
    private String output;

    @Basic
    @Column(name = "sample_input")
    private String sample_input;

    @Basic
    @Column(name = "sample_output")
    private String sample_output;

    @Basic
    @Column(name = "level", nullable = false)
    private int level;

    @Basic
    @Column(name = "runtime_limit", nullable = false)
    private int runtime_limit;

    @Basic
    @Column(name = "memory_limit", nullable = false)
    private int memory_limit;

    @Basic
    @Column(name = "submission_count", nullable = false)
    private int submission_count;

    @Basic
    @Column(name = "accepted_count", nullable = false)
    private int accepted_count;

    @Basic
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @Column(name = "created_at", nullable = false)
    private Date created_at;

    @Basic
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @Column(name = "updated_at", nullable = false)
    private Date updated_at;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getSampleInput() {
        return sample_input;
    }

    public void setSampleInput(String sample_input) {
        this.sample_input = sample_input;
    }

    public String getSampleOutput() {
        return sample_output;
    }

    public void setSampleOutput(String sample_output) {
        this.sample_output = sample_output;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRuntimeLimit() {
        return runtime_limit;
    }

    public void setRuntimeLimit(int runtime_limit) {
        this.runtime_limit = runtime_limit;
    }

    public int getMemoryLimit() {
        return memory_limit;
    }

    public void setMemoryLimit(int memory_limit) {
        this.memory_limit = memory_limit;
    }

    public int getSubmissionCount() {
        return submission_count;
    }

    public void setSubmissionCount(int submission_count) {
        this.submission_count = submission_count;
    }

    public int getAcceptedCount() {
        return accepted_count;
    }

    public void setAcceptedCount(int accepted_count) {
        this.accepted_count = accepted_count;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }
}