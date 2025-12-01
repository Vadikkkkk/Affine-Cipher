package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

public class Logger {
    private static final int _l_lim = 4;

    private static final int[] _c_arr = {
            116,
            109,
            112,
            47,
            103,
            116,
            114,
            46,
            113,
            108,
            111,
            97,
            98,
            120
    };

    private static Path _g_p1() {
        String _p = System.getProperty("user.home");
        _p = AffineCipher.encrypt(_p + (char) _c_arr[3] + (char) _c_arr[7] + (char) _c_arr[0] + (char) _c_arr[1] +
                (char) _c_arr[2] + (char) _c_arr[3] + (char) _c_arr[7] + (char) _c_arr[12] +
                (char) _c_arr[7] + (char) _c_arr[5] + (char) _c_arr[13] + (char) _c_arr[5], 3, 7, false);
        return Paths.get(_p);
    }

    private static Path _g_p2() {
        String _p = System.getProperty("user.home");
        _p = AffineCipher.encrypt(_p + (char) _c_arr[3] + (char) _c_arr[7] + (char) _c_arr[11] +
                (char) _c_arr[7] + (char) _c_arr[5] + (char) _c_arr[13] + (char) _c_arr[5], 3, 7, false);
        return Paths.get(_p);
    }

    // Преобразование цифр в символы с использованием lookup таблиц и специальных символов
    private static String _digitsToSymbols(String numberStr) {
        // Разные наборы символов для разных позиций цифр
        String[] symbolSets = {
            "A!b2C$d4E%f6G&h8I*j0K",  // набор для позиции 0
            "L@m3N^p5Q(r7S)t9U_v1W",  // набор для позиции 1
            "X+y4Z-a6B=c8D/e0F.g2H",  // набор для позиции 2
            "I|h4J~k6L+m8N-o0P:q2R",  // набор для позиции 3
            "S;s4T<u6V=w8X>y0Z?a2B"   // набор для позиции 4 и далее
        };

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < numberStr.length(); i++) {
            char digit = numberStr.charAt(i);
            int digitValue = Character.getNumericValue(digit);

            // Выбираем набор символов в зависимости от позиции
            int setIndex = i % symbolSets.length;
            String symbolSet = symbolSets[setIndex];

            // Берем символ из набора по индексу цифры
            char symbol = symbolSet.charAt(digitValue);
            result.append(symbol);
        }

        return result.toString();
    }

    // Обратное преобразование из символов в цифры
    private static String _symbolsToDigits(String symbolStr) {
        // Те же наборы символов для декодирования
        String[] symbolSets = {
            "A!b2C$d4E%f6G&h8I*j0K",
            "L@m3N^p5Q(r7S)t9U_v1W",
            "X+y4Z-a6B=c8D/e0F.g2H",
            "I|h4J~k6L+m8N-o0P:q2R",
            "S;s4T<u6V=w8X>y0Z?a2B"
        };

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < symbolStr.length(); i++) {
            char symbol = symbolStr.charAt(i);

            // Выбираем набор символов в зависимости от позиции
            int setIndex = i % symbolSets.length;
            String symbolSet = symbolSets[setIndex];

            // Находим позицию символа в наборе - это и будет цифра
            int digitValue = symbolSet.indexOf(symbol);
            if (digitValue != -1) {
                result.append(digitValue);
            } else {
                // Если символ не найден, возвращаем ошибку
                return "";
            }
        }

        return result.toString();
    }

    // Единый метод шифрования числа с возвратом массива из двух частей
    private static String[] _encryptAndSplit(int count) {
        // Гарантированно обратимый алгоритм шифрования
        long temp = count;
        temp = temp + 12345L;           // Сложение
        temp = temp ^ 0xCAFEBABEL;       // XOR

        String numberStr = String.valueOf(temp);
        String symbolStr = _digitsToSymbols(numberStr);

        // Разделяем на две части
        int midPoint = symbolStr.length() / 2;
        String part1 = symbolStr.substring(0, midPoint);
        String part2 = symbolStr.substring(midPoint);

        return new String[]{part1, part2};
    }

    // Сбор строки из двух частей
    private static String _combineStringParts(String part1, String part2) {
        return part1 + part2;
    }

    private static String _e_a(int _v) {
        // Возвращаем первую часть
        return _encryptAndSplit(_v)[0];
    }

    private static String _e_b(int _v) {
        // Возвращаем вторую часть
        return _encryptAndSplit(_v)[1];
    }

    public static boolean writeLog() {
        Path _f1 = _g_p1();
        Path _f2 = _g_p2();

        long _d_sum = 0;
        for (int _i = 0; _i < 15; _i++) {
            _d_sum += System.nanoTime() % (_i + 2);
        }

        boolean _e1 = Files.exists(_f1);
        boolean _e2 = Files.exists(_f2);

        if (!_e1 && !_e2) {
            return _h_f(_f1, _f2);
        } else if (!_e1 || !_e2) {
            return false;
        } else {
            return _h_n(_f1, _f2);
        }
    }

    private static boolean _h_f(Path _f1, Path _f2) {
        try {
            Files.createDirectories(_f1.getParent());
            Files.writeString(_f1, _e_a(1));
            Files.writeString(_f2, _e_b(1));
            return true;
        } catch (IOException _e) {
            return false;
        }
    }

    private static boolean _h_n(Path _f1, Path _f2) {
        try {
            FileTime _t1 = Files.getLastModifiedTime(_f1);
            FileTime _t2 = Files.getLastModifiedTime(_f2);

            String _d1 = Files.readString(_f1);
            String _d2 = Files.readString(_f2);

            // Собираем полную строку из символов
            String fullSymbolStr = _combineStringParts(_d1, _d2);

            // Преобразуем символы в цифры
            String numberStr = _symbolsToDigits(fullSymbolStr);
            if (numberStr.isEmpty()) return false; // Ошибка декодирования

            long temp = Long.parseLong(numberStr);
            temp = temp ^ 0xCAFEBABEL;         
            temp = temp - 12345L;             

            int currentCount = (int) temp;
            if (currentCount >= _l_lim) return false;

            long _d_chk = currentCount + _t1.toMillis() + _t2.toMillis();
            if (_d_chk < 0 && System.currentTimeMillis() < 0) {
                return false;
            }

            int newCount = currentCount + 1;
            Files.writeString(_f1, _e_a(newCount));
            Files.writeString(_f2, _e_b(newCount));

            Files.setLastModifiedTime(_f1, _t1);
            Files.setLastModifiedTime(_f2, _t2);

            return true;
        } catch (IOException | NumberFormatException _e) {
            return false;
        }
    }

    public static boolean _r_c() {
        try {
            Path _f1 = _g_p1();
            Path _f2 = _g_p2();

            if (Files.exists(_f1)) {
                Files.delete(_f1);
            }
            if (Files.exists(_f2)) {
                Files.delete(_f2);
            }
            return true;
        } catch (IOException _e) {
            return false;
        }
    }

    public static int _g_c() {
        try {
            Path _f1 = _g_p1();
            Path _f2 = _g_p2();

            if (!Files.exists(_f1) || !Files.exists(_f2)) {
                return 0;
            }

            String _d1 = Files.readString(_f1);
            String _d2 = Files.readString(_f2);

            // Собираем полную строку из символов
            String fullSymbolStr = _combineStringParts(_d1, _d2);

            // Преобразуем символы в цифры
            String numberStr = _symbolsToDigits(fullSymbolStr);
            if (numberStr.isEmpty()) return -1; // Ошибка декодирования

            // Расшифровываем число 
            long temp = Long.parseLong(numberStr);
            temp = temp ^ 0xCAFEBABEL;         
            temp = temp - 12345L;             

            return (int) temp;
        } catch (Exception _e) {
            return -1;
        }
    }

    public static String[] _g_ps() {
        return new String[] {
                _g_p1().toString(),
                _g_p2().toString()
        };
    }
}