package cityrailgo.model;

public enum StatusTiket {
    DIPESAN("Dipesan"),
    DIBAYAR("Dibayar"),
    DIBATALKAN("Dibatalkan"),
    KADALUARSA("Kadaluarsa");

    private final String label;

    StatusTiket(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAktif() {
        return this == DIPESAN || this == DIBAYAR;
    }

    public boolean isBisaDibatalkan() {
        return this == DIPESAN;
    }

    @Override
    public String toString() {
        return label;
    }
}