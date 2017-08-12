package plus.justice.dispatcher.models.database;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_problem")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "level")
    private Integer level;

    @Column(name = "runtime_limit")
    private Integer runtime_limit;

    @Column(name = "memory_limit")
    private Integer memory_limit;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getRuntimeLimit() {
        return runtime_limit;
    }

    public void setRuntimeLimit(Integer runtime_limit) {
        this.runtime_limit = runtime_limit;
    }

    public Integer getMemoryLimit() {
        return memory_limit;
    }

    public void setMemoryLimit(Integer memory_limit) {
        this.memory_limit = memory_limit;
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
        return "Problem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", level=" + level +
                ", runtime_limit=" + runtime_limit +
                ", memory_limit=" + memory_limit +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}