package app;


import java.util.*;
import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;

import java.util.Locale;


public class AffineCipher {
    private static final char[] ALPHABET_UPPER = (
            "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
    ).toCharArray();
    private static final char[] ALPHABET_LOWER = (
            "абвгдежзийклмнопрстуфхцчшщъыьэюя"
    ).toCharArray();
    private static final int M = ALPHABET_UPPER.length; // 33

    private static final Map<Character, Integer> upperIndex = new HashMap<>();
    private static final Map<Character, Integer> lowerIndex = new HashMap<>();

    static {
        for (int i = 0; i < M; i++) {
            upperIndex.put(ALPHABET_UPPER[i], i);
            lowerIndex.put(ALPHABET_LOWER[i], i);
        }
    }

    // Расширенный алгоритм Евклида для поиска обратного по модулю
    private static int modInverse(int a) {
        int m = M, x0 = 0, x1 = 1;
        while (a > 1) {
            int q = a / m;
            int t = m;
            m = a % m;
            a = t;

            t = x0;
            x0 = x1 - q * x0;
            x1 = t;
        }
        if (x1 < 0) x1 += M;
        return x1;
    }

    public static String encrypt(String text, int a, int b) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (upperIndex.containsKey(ch)) {
                int x = upperIndex.get(ch);
                int y = (a * x + b) % M;
                sb.append(ALPHABET_UPPER[y]);
            } else if (lowerIndex.containsKey(ch)) {
                int x = lowerIndex.get(ch);
                int y = (a * x + b) % M;
                sb.append(ALPHABET_LOWER[y]);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }


    public static String decrypt(String text, int a, int b) {
        int inv = modInverse(a);
        if (inv == -1) throw new IllegalArgumentException("Ключ a не имеет обратного по модулю, дешифрование невозможно (gcd(a, m) != 1)");
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (upperIndex.containsKey(ch)) {
                int y = upperIndex.get(ch);
                int x = (inv * (y + M - b)) % M;
                sb.append(ALPHABET_UPPER[x]);
            } else if (lowerIndex.containsKey(ch)) {
                int y = lowerIndex.get(ch);
                int x = (inv * (y + M - b)) % M;
                sb.append(ALPHABET_LOWER[x]);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static boolean isRussianWord(String word) {
        if (word == null || word.isEmpty()) return false;
        try {
            return !lookupForMeanings(word.toLowerCase(Locale.ROOT)).isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean looksLikeRussian(String text) {
        if (text == null || text.isEmpty()) return false;

        // разделяем по пробелам
        String[] tokens = text.split("\\s+");
        int limit = Math.min(tokens.length, 20);
        if (limit == 0) return false;

        int realWords = 0;
        for (int i = 0; i < limit; i++) {
            String w = tokens[i].trim();
            if (w.isEmpty()) continue;
            if (isRussianWord(w)) realWords++;
        }

        double threshold = 0.9; // 90%
        return ((double) realWords / limit) >= threshold;
    }

    // Перебор всех возможных комбинаций для взлома
    public static List<String> bruteForce(String cipherText) {
        List<String> results = new ArrayList<>();
        for (int a = 1; a < M; a++) {
            if (gcd(a, M) != 1) continue; // a должно быть взаимно простым с M
            for (int b = 0; b < M; b++) {
                try {
                    String dec = decrypt(cipherText, a, b);
                    if (looksLikeRussian(dec)) {
                        results.add(String.format("a = %d b = %d : %s", a, b, dec));
                    }
                } catch (Exception ex) {
                    // пропускаем комбинации, которые вызвали ошибки
                }
            }
        }
        if (results.isEmpty()) {
            System.out.println("Подходящих вариантов не найдено.");
        }
        return results;
    }


    private static int gcd(int x, int y) {
        if (y == 0) return x;
        return gcd(y, x % y);
    }
}