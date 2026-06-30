package util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AvatarManager {
    private static final String PROFILE_DIR = "data/profiles";

    public static void saveAvatar(String username, File sourceFile) {
        try {
            Path dir = Paths.get(PROFILE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path target = dir.resolve(username + "_avatar");
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Gagal menyimpan foto: " + e.getMessage());
        }
    }

    public static void loadAvatar(String username, ImageView imageView, FontIcon defaultIcon, double size) {
        try {
            File avatarFile = Paths.get(PROFILE_DIR, username + "_avatar").toFile();
            if (avatarFile.exists()) {
                Image img = new Image(avatarFile.toURI().toString());
                imageView.setImage(img);
                imageView.setFitWidth(size);
                imageView.setFitHeight(size);
                
                Circle clip = new Circle(size / 2, size / 2, size / 2);
                imageView.setClip(clip);
                
                imageView.setVisible(true);
                imageView.setManaged(true);
                
                if (defaultIcon != null) {
                    defaultIcon.setVisible(false);
                    defaultIcon.setManaged(false);
                }
            } else {
                imageView.setVisible(false);
                imageView.setManaged(false);
                if (defaultIcon != null) {
                    defaultIcon.setVisible(true);
                    defaultIcon.setManaged(true);
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat foto: " + e.getMessage());
        }
    }
}
