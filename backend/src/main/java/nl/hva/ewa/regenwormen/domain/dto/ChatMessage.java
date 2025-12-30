package nl.hva.ewa.regenwormen.domain.dto;

public class ChatMessage {
    private String sender;
    private String content;
    private String lobbyId;

    public ChatMessage() {
    }

    public ChatMessage(String sender, String content, String lobbyId) {
        this.sender = sender;
        this.content = content;
        this.lobbyId = lobbyId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }
}
