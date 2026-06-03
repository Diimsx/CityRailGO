package cityrailgo.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 
public abstract class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }
 
    public User(int id, String username, String email, String password) {
        this.id        = id;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.createdAt = LocalDateTime.now();
    }
 
    public User(int id, String username, String email, String password, LocalDateTime createdAt) {
        this.id        = id;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.createdAt = createdAt;
    }

    public abstract String getRole();
 
    public abstract String getInfo();

    public boolean verifikasiPassword(String inputPassword) {
        String inputHash = hashSHA256(inputPassword);
        return this.password != null && this.password.equals(inputHash);
    }

    public static String hashSHA256(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[User] Gagal melakukan hashing: " + e.getMessage());
            return null;
        }
    }

    public void setPasswordPlain(String plainText) {
        this.password = hashSHA256(plainText);
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public String getUsername() {
        return username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
 
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
 
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + getRole() + '\'' +
                ", createdAt=" + (createdAt != null ? createdAt.format(fmt) : "-") +
                '}';
    }
}
