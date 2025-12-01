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
    private static final int M_RUSSIAN = ALPHABET_UPPER.length; // 33

    // Английский алфавит
    private static final char[] ENGLISH_ALPHABET_UPPER = (
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    ).toCharArray();
    private static final char[] ENGLISH_ALPHABET_LOWER = (
            "abcdefghijklmnopqrstuvwxyz"
    ).toCharArray();
    private static final int M_ENGLISH = ENGLISH_ALPHABET_UPPER.length; // 26

    private static final Map<Character, Integer> upperIndex = new HashMap<>();
    private static final Map<Character, Integer> lowerIndex = new HashMap<>();
    private static final Map<Character, Integer> englishUpperIndex = new HashMap<>();
    private static final Map<Character, Integer> englishLowerIndex = new HashMap<>();

    static {
        for (int i = 0; i < M_RUSSIAN; i++) {
            upperIndex.put(ALPHABET_UPPER[i], i);
            lowerIndex.put(ALPHABET_LOWER[i], i);
        }
        for (int i = 0; i < M_ENGLISH; i++) {
            englishUpperIndex.put(ENGLISH_ALPHABET_UPPER[i], i);
            englishLowerIndex.put(ENGLISH_ALPHABET_LOWER[i], i);
        }
    }

    // Расширенный алгоритм Евклида для поиска обратного по модулю
    private static int modInverse(int a, int m) {
        int originalM = m;
        int x0 = 0, x1 = 1;
        while (a > 1) {
            int q = a / m;
            int t = m;
            m = a % m;
            a = t;

            t = x0;
            x0 = x1 - q * x0;
            x1 = t;
        }
        if (x1 < 0) x1 += originalM;
        return x1;
    }

    public static String encrypt(String text, int a, int b, boolean isEnglish) {
        StringBuilder sb = new StringBuilder();
        int m = isEnglish ? M_ENGLISH : M_RUSSIAN;

        for (char ch : text.toCharArray()) {
            if (isEnglish) {
                // Английский алфавит
                if (englishUpperIndex.containsKey(ch)) {
                    int x = englishUpperIndex.get(ch);
                    int y = (a * x + b) % m;
                    sb.append(ENGLISH_ALPHABET_UPPER[y]);
                } else if (englishLowerIndex.containsKey(ch)) {
                    int x = englishLowerIndex.get(ch);
                    int y = (a * x + b) % m;
                    sb.append(ENGLISH_ALPHABET_LOWER[y]);
                } else {
                    sb.append(ch);
                }
            } else {
                // Русский алфавит
                if (upperIndex.containsKey(ch)) {
                    int x = upperIndex.get(ch);
                    int y = (a * x + b) % m;
                    sb.append(ALPHABET_UPPER[y]);
                } else if (lowerIndex.containsKey(ch)) {
                    int x = lowerIndex.get(ch);
                    int y = (a * x + b) % m;
                    sb.append(ALPHABET_LOWER[y]);
                } else {
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }

    public static String decrypt(String text, int a, int b, boolean isEnglish) {
        int m = isEnglish ? M_ENGLISH : M_RUSSIAN;
        int inv = modInverse(a, m);
        if (inv == -1) {
            String lang = isEnglish ? "английского" : "русского";
            throw new IllegalArgumentException("Ключ a не имеет обратного по модулю " + m + " (" + lang + " алфавит), дешифрование невозможно (gcd(a, m) != 1)");
        }

        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (isEnglish) {
                // Английский алфавит
                if (englishUpperIndex.containsKey(ch)) {
                    int y = englishUpperIndex.get(ch);
                    int x = (inv * (y - b + m)) % m;
                    sb.append(ENGLISH_ALPHABET_UPPER[x]);
                } else if (englishLowerIndex.containsKey(ch)) {
                    int y = englishLowerIndex.get(ch);
                    int x = (inv * (y - b + m)) % m;
                    sb.append(ENGLISH_ALPHABET_LOWER[x]);
                } else {
                    sb.append(ch);
                }
            } else {
                // Русский алфавит
                if (upperIndex.containsKey(ch)) {
                    int y = upperIndex.get(ch);
                    int x = (inv * (y - b + m)) % m;
                    sb.append(ALPHABET_UPPER[x]);
                } else if (lowerIndex.containsKey(ch)) {
                    int y = lowerIndex.get(ch);
                    int x = (inv * (y - b + m)) % m;
                    sb.append(ALPHABET_LOWER[x]);
                } else {
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }

    // Устаревшие методы для обратной совместимости (используют русский алфавит)
    public static String encrypt(String text, int a, int b) {
        return encrypt(text, a, b, false);
    }

    public static String decrypt(String text, int a, int b) {
        return decrypt(text, a, b, false);
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
        for (int a = 1; a < M_RUSSIAN; a++) {
            if (gcd(a, M_RUSSIAN) != 1) continue; // a должно быть взаимно простым с M
            for (int b = 0; b < M_RUSSIAN; b++) {
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