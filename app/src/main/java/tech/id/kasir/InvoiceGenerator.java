package tech.id.kasir;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class InvoiceGenerator {

    public static String generateInvoiceCode(String prefix) {
        String datePart = new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date());
        String kodeUnik = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        // Gabungkan
        return prefix + "-" + datePart + "-" + kodeUnik;
    }

    public static void main(String[] args) {
        System.out.println(generateInvoiceCode("KS")); // contoh output: KS-221025-4G8T
    }

//    public static String generateInvoiceNumber(int restoranId, int penggunaId, List<String> existingInvoices) {
//        String invoice;
//        Random random = new Random();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
//        String timestamp = dateFormat.format(new Date());
//
//        do {
//            int randomNumber = 100 + random.nextInt(900); // 6 digit acak
//            invoice = restoranId + "-" + penggunaId + "-" + timestamp + "-" + randomNumber;
//        } while (existingInvoices.contains(invoice)); // Hindari duplikat lokal
//
//        return invoice;
//    }
}
