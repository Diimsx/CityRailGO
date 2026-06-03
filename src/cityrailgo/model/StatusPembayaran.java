package cityrailgo.model;

public enum StatusPembayaran {
    PENDING("Pending"),
    SUKSES("Sukses"),
    GAGAL("Gagal"),
    REFUND("Refund");

    private final String label;

    StatusPembayaran(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isSukses() {
        return this == SUKSES;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isTerminal() {
        return this == GAGAL || this == REFUND;
    }

    @Override
    public String toString() {
        return label;
    }
}