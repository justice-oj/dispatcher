package plus.justice.models;

public class QueueMessage {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "QueueMessage{" +
                "id=" + id +
                '}';
    }
}
