package cityrailgo.model;

public class User {

    private int id;
    private String username;
    private String password;
    private String email;
    private String namaLengkap;
    private String noTelepon;

    public User(String username,
                String password,
                String email,
                String namaLengkap,
                String noTelepon) {

        this.username = username;
        this.password = password;
        this.email = email;
        this.namaLengkap = namaLengkap;
        this.noTelepon = noTelepon;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public String getNoTelepon() {
        return noTelepon;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean login(String username, String password) {
        return this.username.equals(username)
                && this.password.equals(password);
    }

    public void logout() {
        System.out.println("Logout berhasil");
    }

    public String getRole() {
        return "User";
    }
}
