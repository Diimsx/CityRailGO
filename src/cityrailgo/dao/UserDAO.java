package cityrailgo.dao;

import cityrailgo.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private List<User> daftarUser = new ArrayList<>();

    public User findById(int id) {

        for (User user : daftarUser) {
            if (user.getId() == id) {
                return user;
            }
        }

        return null;
    }

    public User findByUsername(String username) {

        for (User user : daftarUser) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public boolean save(User user) {
        return daftarUser.add(user);
    }

    public boolean update(User user) {
        return true;
    }

    public boolean delete(int id) {

        User user = findById(id);

        if (user != null) {
            return daftarUser.remove(user);
        }

        return false;
    }
}
