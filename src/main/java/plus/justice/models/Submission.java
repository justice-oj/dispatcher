package plus.justice.models;

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
    private Integer userId;

    @Column(name = "problem_id")
    private Integer problemId;

    @Column(name = "language")
    private Integer language;

    @Column(name = "code")
    private String code;

    @Column(name = "status")
    private Integer status;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "memory")
    private Integer memory;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
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

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
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
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}