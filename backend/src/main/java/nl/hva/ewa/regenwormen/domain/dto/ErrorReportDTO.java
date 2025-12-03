package nl.hva.ewa.regenwormen.domain.dto;

import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;
import java.time.LocalDateTime;

public class ErrorReportDTO {
    private String id;
    private String name;
    private String topic;
    private String category;
    private String priority;
    private String details;
    private String snapshotFileName;
    private String gameStateJson;
    private ErrorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private String gameId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getGameStateJson() { return gameStateJson; }
    public void setGameStateJson(String gameStateJson) { this.gameStateJson = gameStateJson; }

    public ErrorStatus getStatus() { return status; }
    public void setStatus(ErrorStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
}
