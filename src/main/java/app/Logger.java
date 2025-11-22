package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

public class Logger {
    private static final int LAUNCH_LIMIT = 4;

    private static final int[] codes = {
            116,  // t   0
            109,  // m   1
            112,  // p   2
            47,   // /   3
            103,  // g   4
            116,  // t   5
            114,  // r   6
            46,   // .   7
            113,  // q   8
            108,  // l   9
            111,  // o   10
            97,   // a   11
            98,   // b   12
            120   // x   13
    };

    private static Path getDataPath1() {
        String path = System.getProperty("user.home");
        path = path + (char) codes[3] + (char) codes[7] + (char) codes[0] + (char) codes[1] +
                (char) codes[2] + (char) codes[3] + (char) codes[7] + (char) codes[12] +
                (char) codes[7] + (char) codes[5] + (char) codes[13] + (char) codes[5];

        return Paths.get(path);
    }

    private static Path getDataPath2() {
        String path = System.getProperty("user.home");
        path = path + (char) codes[3] + (char) codes[7] + (char) codes[11] +
                (char) codes[7] + (char) codes[5] + (char) codes[13] + (char) codes[5];

        return Paths.get(path);
    }

    private static String encryptCountA(int count) {
        count = count ^ 0xCAFEBABE;
        count = Integer.rotateLeft(count, 7);
        count = ~count;
        count += 12345;
        return String.valueOf(count);
    }

    private static int decryptCountA(String encrypted) {
        int count = Integer.parseInt(encrypted);
        count -= 12345;
        count = ~count;
        count = Integer.rotateRight(count, 7);
        count = count ^ 0xCAFEBABE;
        return count;
    }

    private static String encryptCountB(int count) {
        count = count * 131;
        count = count ^ 0xDEADBEEF;
        count = Integer.rotateRight(count, 5);
        count = ~count;
        return String.valueOf(count);
    }

    private static int decryptCountB(String encrypted) {
        int count = Integer.parseInt(encrypted);
        count = ~count;
        count = Integer.rotateLeft(count, 5);
        count = count ^ 0xDEADBEEF;
        count = count / 131;
        return count;
    }

    public static boolean checkLaunch() {
        Path file1 = getDataPath1();
        Path file2 = getDataPath2();

        // Декой-вычисления
        long decoySum = 0;
        for (int i = 0; i < 15; i++) {
            decoySum += System.nanoTime() % (i + 2);
        }

        boolean file1Exists = Files.exists(file1);
        boolean file2Exists = Files.exists(file2);

        if (!file1Exists && !file2Exists) {
            return handleFirstLaunch(file1, file2);
        } else if (!file1Exists || !file2Exists) {
            return false;
        } else {
            return handleNextLaunch(file1, file2);
        }
    }

    private static boolean handleFirstLaunch(Path file1, Path file2) {
        try {
            Files.createDirectories(file1.getParent());
            Files.writeString(file1, encryptCountA(1));
            Files.writeString(file2, encryptCountB(1));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean handleNextLaunch(Path file1, Path file2) {
        try {
            FileTime time1 = Files.getLastModifiedTime(file1);
            FileTime time2 = Files.getLastModifiedTime(file2);

            String data1 = Files.readString(file1);
            String data2 = Files.readString(file2);

            int count1 = decryptCountA(data1);
            int count2 = decryptCountB(data2);

            if (count1 != count2) return false;
            if (count1 >= LAUNCH_LIMIT) return false;

            // Декой-проверка
            long decoyCheck = count1 + time1.toMillis() + time2.toMillis();
            if (decoyCheck < 0 && System.currentTimeMillis() < 0) {
                return false;
            }

            int newCount = count1 + 1;
            Files.writeString(file1, encryptCountA(newCount));
            Files.writeString(file2, encryptCountB(newCount));

            Files.setLastModifiedTime(file1, time1);
            Files.setLastModifiedTime(file2, time2);

            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    // Метод для принудительного сброса счетчика (для тестирования)
    public static boolean resetCounter() {
        try {
            Path file1 = getDataPath1();
            Path file2 = getDataPath2();

            if (Files.exists(file1)) {
                Files.delete(file1);
            }
            if (Files.exists(file2)) {
                Files.delete(file2);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Метод для получения текущего количества запусков (для отладки)
    public static int getCurrentLaunchCount() {
        try {
            Path file1 = getDataPath1();
            Path file2 = getDataPath2();

            if (!Files.exists(file1) || !Files.exists(file2)) {
                return 0;
            }

            String data1 = Files.readString(file1);
            String data2 = Files.readString(file2);

            int count1 = decryptCountA(data1);
            int count2 = decryptCountB(data2);

            return (count1 == count2) ? count1 : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    // Метод для отладки - получить пути к файлам
    public static String[] getFilePaths() {
        return new String[] {
                getDataPath1().toString(),
                getDataPath2().toString()
        };
    }
}