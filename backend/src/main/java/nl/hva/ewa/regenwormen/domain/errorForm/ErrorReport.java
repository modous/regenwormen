package nl.hva.ewa.regenwormen.domain.errorForm;

import jakarta.persistence.*;
import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "error_reports")
public class ErrorReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column
    private String snapshotFileName;

    @Column
    private String snapshotFilePath;

    @Column(columnDefinition = "CLOB")
    private String gameStateJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private String userId;

    @Column
    private String gameId;

    // Constructors
    public ErrorReport() {
        this.createdAt = LocalDateTime.now();
        this.status = ErrorStatus.NEW;
    }

    public ErrorReport(String name, String topic, String category, String priority,
                       String details, String gameStateJson) {
        this();
        this.name = name;
        this.topic = topic;
        this.category = category;
        this.priority = priority;
        this.details = details;
        this.gameStateJson = gameStateJson;
    }

    // Getters and Setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getSnapshotFileName() { return snapshotFileName; }
    public void setSnapshotFileName(String snapshotFileName) { this.snapshotFileName = snapshotFileName; }

    public String getSnapshotFilePath() { return snapshotFilePath; }
    public void setSnapshotFilePath(String snapshotFilePath) { this.snapshotFilePath = snapshotFilePath; }

    public String getGameStateJson() { return gameStateJson; }
    public void setGameStateJson(String gameStateJson) { this.gameStateJson = gameStateJson; }

    public ErrorStatus getStatus() { return status; }
    public void setStatus(ErrorStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
}
