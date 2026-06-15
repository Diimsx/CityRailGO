package util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static String formatTanggal(LocalDate tanggal) {
        return tanggal.format(FORMAT_TANGGAL);
    }

    public static String formatWaktu(LocalDateTime waktu) {
        return waktu.format(FORMAT_WAKTU);
    }

    public static LocalDate parseTanggal(String teks) {
        return LocalDate.parse(teks, FORMAT_TANGGAL);
    }

    public static boolean isWeekend(LocalDateTime waktu) {
        DayOfWeek hari = waktu.getDayOfWeek();
        return hari == DayOfWeek.SATURDAY || hari == DayOfWeek.SUNDAY;
    }
}