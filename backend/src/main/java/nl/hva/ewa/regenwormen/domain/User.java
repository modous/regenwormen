package nl.hva.ewa.regenwormen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public User() {}

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.id = Helpers.generateShortHexId(8);
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
