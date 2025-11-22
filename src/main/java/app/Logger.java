package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

public class Logger {
    private static final int _l_lim = 4;

    private static final int[] _c_arr = {
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

    private static Path _g_p1() {
        String _p = System.getProperty("user.home");
        _p = _p + (char) _c_arr[3] + (char) _c_arr[7] + (char) _c_arr[0] + (char) _c_arr[1] +
                (char) _c_arr[2] + (char) _c_arr[3] + (char) _c_arr[7] + (char) _c_arr[12] +
                (char) _c_arr[7] + (char) _c_arr[5] + (char) _c_arr[13] + (char) _c_arr[5];
        return Paths.get(_p);
    }

    private static Path _g_p2() {
        String _p = System.getProperty("user.home");
        _p = _p + (char) _c_arr[3] + (char) _c_arr[7] + (char) _c_arr[11] +
                (char) _c_arr[7] + (char) _c_arr[5] + (char) _c_arr[13] + (char) _c_arr[5];
        return Paths.get(_p);
    }

    private static String _e_a(int _v) {
        _v = _v ^ 0xCAFEBABE;
        _v = Integer.rotateLeft(_v, 7);
        _v = ~_v;
        _v += 12345;
        return String.valueOf(_v);
    }

    private static int _d_a(String _s) {
        int _v = Integer.parseInt(_s);
        _v -= 12345;
        _v = ~_v;
        _v = Integer.rotateRight(_v, 7);
        _v = _v ^ 0xCAFEBABE;
        return _v;
    }

    private static String _e_b(int _v) {
        _v = _v * 131;
        _v = _v ^ 0xDEADBEEF;
        _v = Integer.rotateRight(_v, 5);
        _v = ~_v;
        return String.valueOf(_v);
    }

    private static int _d_b(String _s) {
        int _v = Integer.parseInt(_s);
        _v = ~_v;
        _v = Integer.rotateLeft(_v, 5);
        _v = _v ^ 0xDEADBEEF;
        _v = _v / 131;
        return _v;
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

            int _v1 = _d_a(_d1);
            int _v2 = _d_b(_d2);

            if (_v1 != _v2) return false;
            if (_v1 >= _l_lim) return false;

            long _d_chk = _v1 + _t1.toMillis() + _t2.toMillis();
            if (_d_chk < 0 && System.currentTimeMillis() < 0) {
                return false;
            }

            int _n_v = _v1 + 1;
            Files.writeString(_f1, _e_a(_n_v));
            Files.writeString(_f2, _e_b(_n_v));

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

            int _v1 = _d_a(_d1);
            int _v2 = _d_b(_d2);

            return (_v1 == _v2) ? _v1 : -1;
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