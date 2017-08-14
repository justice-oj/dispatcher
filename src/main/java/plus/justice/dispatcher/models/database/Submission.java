package plus.justice.dispatcher.models.database;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_submission")
public class Submission {
    public final static int LANGUAGE_C = 0;
    public final static int LANGUAGE_CPP = 1;
    public final static int LANGUAGE_PYTHON2 = 2;
    public final static int LANGUAGE_PYTHON3 = 3;
    public final static int LANGUAGE_JAVA = 4;

    public final static int STATUS_QUEUE = -1;
    public final static int STATUS_AC = 0;
    public final static int STATUS_CE = 1;
    public final static int STATUS_RE = 2;
    public final static int STATUS_TLE = 3;
    public final static int STATUS_MLE = 4;
    public final static int STATUS_WA = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "language")
    private Integer language;

    @Column(name = "code")
    private String code;

    @Column(name = "status")
    private Integer status;

    @Column(name = "runtime")
    private Long runtime;

    @Column(name = "memory")
    private Long memory;

    @Column(name = "error")
    private String error;

    @Column(name = "input")
    private String input;

    @Column(name = "output")
    private String output;

    @Column(name = "expected")
    private String expected;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Integer getLanguage() {
        return language;
    }

    public void setLanguage(Integer language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRuntime() {
        return runtime;
    }

    public void setRuntime(Long runtime) {
        this.runtime = runtime;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", userId=" + userId +
                ", problemId=" + problemId +
                ", language=" + language +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", runtime=" + runtime +
                ", memory=" + memory +
                ", error='" + error + '\'' +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", expected='" + expected + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}