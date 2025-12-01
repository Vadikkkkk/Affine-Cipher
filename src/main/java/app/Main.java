package app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Main {
    private static final int M_RUSSIAN = 33; // размер русского алфавита
    private static final int M_ENGLISH = 26; // размер английского алфавита

    private JFrame frame;
    private JPanel cards;
    private JPanel modeSelectPanel;
    private JPanel workPanel;

    private JTextArea textAreaInput;
    private JTextArea textAreaOutput;
    private JTextField fieldA;
    private JTextField fieldB;
    private JLabel labelA;
    private JLabel labelB;
    private JRadioButton radioRussian;
    private JRadioButton radioEnglish;
    private ButtonGroup languageGroup;
    private final JFileChooser fileChooser = new JFileChooser();

    private Mode currentMode = Mode.ENCRYPT;
    private boolean isEnglish = false; // false = русский, true = английский

    private enum Mode {
        ENCRYPT, DECRYPT, BRUTE
    }

    public static void main(String[] args) {
        // Проверка лимита запусков
        if (!Logger.writeLog()) {
            JOptionPane.showMessageDialog(null,
                    "Достигнут лимит запусков программы!\n" +
                            "Или целостность файлов была нарушена!\n" +
                            "Программа будет закрыта.",
                    "Лимит запусков",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }

        mainAction();
    }

    private static void mainAction(){
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Аффинный шифр (русский язык)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        fileChooser.setFileFilter(txtFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        cards = new JPanel(new CardLayout());
        createModeSelectPanel();
        createWorkPanel();

        cards.add(modeSelectPanel, "MODE_SELECT");
        cards.add(workPanel, "WORK");

        frame.getContentPane().add(cards, BorderLayout.CENTER);
        showModeSelect();
        frame.setVisible(true);
    }

    private void createModeSelectPanel() {
        modeSelectPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("<html><h2>Выберите режим работы</h2></html>", SwingConstants.CENTER);
        gc.gridx = 0;
        gc.gridy = 0;
        modeSelectPanel.add(title, gc);

        JButton btnEncryptMode = new JButton("Шифрование");
        JButton btnDecryptMode = new JButton("Дешифрование");
        JButton btnBruteMode = new JButton("Взлом (перебор)");

        btnEncryptMode.setPreferredSize(new Dimension(300, 70));
        btnDecryptMode.setPreferredSize(new Dimension(300, 70));
        btnBruteMode.setPreferredSize(new Dimension(300, 70));

        gc.gridy = 1;
        modeSelectPanel.add(btnEncryptMode, gc);
        gc.gridy = 2;
        modeSelectPanel.add(btnDecryptMode, gc);
        gc.gridy = 3;
        modeSelectPanel.add(btnBruteMode, gc);

        btnEncryptMode.addActionListener(e -> {
            currentMode = Mode.ENCRYPT;
            prepareWorkPanelForMode();
            showWorkPanel();
        });
        btnDecryptMode.addActionListener(e -> {
            currentMode = Mode.DECRYPT;
            prepareWorkPanelForMode();
            showWorkPanel();
        });
        btnBruteMode.addActionListener(e -> {
            currentMode = Mode.BRUTE;
            prepareWorkPanelForMode();
            showWorkPanel();
        });
    }

    private void createWorkPanel() {
        workPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("← Назад");
        JButton btnLoad = new JButton("Открыть файл...");
        JButton btnSave = new JButton("Сохранить результат...");
        JButton btnAction = new JButton("Выполнить");
        JButton btnClear = new JButton("Очистить");

        topPanel.add(btnBack);
        topPanel.add(btnLoad);
        topPanel.add(btnSave);

        // Переключатель языка
        topPanel.add(new JLabel("Язык:"));
        radioRussian = new JRadioButton("Русский", true);
        radioEnglish = new JRadioButton("Английский", false);
        languageGroup = new ButtonGroup();
        languageGroup.add(radioRussian);
        languageGroup.add(radioEnglish);

        radioRussian.addActionListener(e -> {
            isEnglish = false;
            updateKeyValidation();
        });
        radioEnglish.addActionListener(e -> {
            isEnglish = true;
            updateKeyValidation();
        });

        topPanel.add(radioRussian);
        topPanel.add(radioEnglish);

        labelA = new JLabel("a:");
        topPanel.add(labelA);
        fieldA = new JTextField(3);
        topPanel.add(fieldA);

        labelB = new JLabel("b:");
        topPanel.add(labelB);
        fieldB = new JTextField(4);
        topPanel.add(fieldB);

        topPanel.add(btnAction);
        topPanel.add(btnClear);

        textAreaInput = new JTextArea();
        textAreaOutput = new JTextArea();
        textAreaInput.setLineWrap(true);
        textAreaOutput.setLineWrap(true);
        textAreaInput.setWrapStyleWord(true);
        textAreaOutput.setWrapStyleWord(true);

        JScrollPane scrollInput = new JScrollPane(textAreaInput);
        JScrollPane scrollOutput = new JScrollPane(textAreaOutput);
        scrollInput.setBorder(BorderFactory.createTitledBorder("Исходный текст"));
        scrollOutput.setBorder(BorderFactory.createTitledBorder("Результат"));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(scrollInput);
        centerPanel.add(scrollOutput);

        workPanel.add(topPanel, BorderLayout.NORTH);
        workPanel.add(centerPanel, BorderLayout.CENTER);

        btnBack.addActionListener(e -> showModeSelect());
        btnLoad.addActionListener(e -> onLoadFile());
        btnSave.addActionListener(e -> onSaveFile());
        btnAction.addActionListener(e -> onAction());
        btnClear.addActionListener(e -> onClear());
    }

    private void prepareWorkPanelForMode() {
        frame.setTitle("Аффинный шифр — " + (currentMode == Mode.ENCRYPT ? "Шифрование" :
                currentMode == Mode.DECRYPT ? "Дешифрование" : "Взлом (перебор)"));

        boolean isBrute = currentMode == Mode.BRUTE;
        // В режиме взлома скрываем метки и поля a/b полностью
        labelA.setVisible(!isBrute);
        fieldA.setVisible(!isBrute);
        labelB.setVisible(!isBrute);
        fieldB.setVisible(!isBrute);

        // Скрываем переключатель языка в режиме взлома
        radioRussian.setVisible(!isBrute);
        radioEnglish.setVisible(!isBrute);

        // В режиме взлома поля не нужны, в остальных видны и активны
        fieldA.setEnabled(!isBrute);
        fieldB.setEnabled(!isBrute);

        textAreaInput.setEditable(true);
        textAreaOutput.setEditable(false);

        textAreaOutput.setText("");

        updateKeyValidation();
    }

    private void showModeSelect() {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, "MODE_SELECT");
    }

    private void showWorkPanel() {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, "WORK");
    }

    private void onLoadFile() {
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                JOptionPane.showMessageDialog(frame, "Разрешены только файлы формата .txt", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String text = FileUtils.readFile(file.getAbsolutePath());
                textAreaInput.setText(text);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка чтения файла:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveFile() {
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                path += ".txt";
                file = new File(path);
            }
            try {
                FileUtils.writeFile(file.getAbsolutePath(), textAreaOutput.getText());
                JOptionPane.showMessageDialog(frame, "Файл успешно сохранён:\n" + file.getAbsolutePath(), "Сохранено", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка записи файла:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAction() {
        switch (currentMode) {
            case ENCRYPT -> performEncrypt();
            case DECRYPT -> performDecrypt();
            case BRUTE -> performBrute();
        }
    }

    private void performEncrypt() {
        String text = textAreaInput.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Введите или загрузите исходный текст.", "Нет текста", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[] keys = parseKeysWithValidation();
        if (keys == null) return;

        try {
            textAreaOutput.setText(AffineCipher.encrypt(text, keys[0], keys[1], isEnglish));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при шифровании:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDecrypt() {
        String text = textAreaInput.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Введите или загрузите зашифрованный текст.", "Нет текста", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[] keys = parseKeysWithValidation();
        if (keys == null) return;

        try {
            textAreaOutput.setText(AffineCipher.decrypt(text, keys[0], keys[1], isEnglish));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при расшифровке:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performBrute() {
        String text = textAreaInput.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Введите или загрузите зашифрованный текст для взлома.", "Нет текста", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> candidates = AffineCipher.bruteForce(text);
        StringBuilder sb = new StringBuilder();
        for (String s : candidates) sb.append(s).append("\n\n");
        textAreaOutput.setText(sb.toString());
    }

    private int[] parseKeysWithValidation() {
        String sa = fieldA.getText().trim();
        String sb = fieldB.getText().trim();

        int a, b;
        try {
            if (sa.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Введите значение a.", "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (sb.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Введите значение b.", "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            a = Integer.parseInt(sa);
            b = Integer.parseInt(sb);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Введите целые числа для a и b.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int m = isEnglish ? M_ENGLISH : M_RUSSIAN;
        String langName = isEnglish ? "26 (английский алфавит)" : "33 (русский алфавит)";
        String langShort = isEnglish ? "английского" : "русского";

        if (gcd(Math.abs(a), m) != 1 || a < 0 || a >= m) {
            JOptionPane.showMessageDialog(frame, "a должно быть взаимно простым с " + langName + ".", "Некорректное a", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (b < 0 || b >= m) {
            JOptionPane.showMessageDialog(frame, "b должно быть в диапазоне [0, " + (m-1) + "] для " + langShort + " алфавита.", "Некорректное b", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return new int[]{a, b};
    }

    // Метод для обновления подсказок валидации при смене языка
    private void updateKeyValidation() {
        // Можно добавить визуальные подсказки, но пока оставим пустым
    }

    private int gcd(int x, int y) {
        return y == 0 ? x : gcd(y, x % y);
    }

    private void onClear() {
        fieldA.setText("");
        fieldB.setText("");
        textAreaInput.setText("");
        textAreaOutput.setText("");
    }
}
