package cityrailgo.model;

public class Promo {
    private double diskonPercent = 0.10; 
    
    public double hitungDiskon(double harga) {
        return harga * diskonPercent;
    }
}