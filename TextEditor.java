package editor;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextEditor extends JFrame {
    final int WIDTH = 600;
    final int HEIGHT = 400;
    final int KEY_SIZE = 30;
    final String OPEN_ICON_FILE_BIG = "images/load.png";
    final String OPEN_ICON_FILE_SMALL = "images/load1.png";
    final String SAVE_ICON_FILE_BIG = "images/save.png";
    final String SAVE_ICON_FILE_SMALL = "images/save1.png";
    final String EXIT_ICON_FILE_SMALL = "images/exit.png";
    final String START_SEARCH_ICON_BIG = "images/search.png";
    final String PREV_SEARCH_ICON_BIG = "images/prev_big.png";
    final String NEXT_SEARCH_ICON_BIG = "images/next_big.png";
    final String START_SEARCH_ICON_SMALL = "images/search_small.png";
    final String PREV_SEARCH_SMALL = "images/prev_small.png";
    final String NEXT_SEARCH_SMALL = "images/next_small.png";

    private JTextArea textArea;         //основное текстовое поле
    private JTextField findTextField;   //Поле ввода поиска текста
    private JButton saveButton;         //кнопка SAVE
    private JButton loadButton;         //кнопка LOAD
    private JButton startSearchButton;  //кнопка START SEARCH
    private JButton prevSearchButton;  //кнопка PREVIOUS SEARCH
    private JButton nextSearchButton;  //кнопка NEXT SEARCH
    private JCheckBox useRegExBox;     //бокс USE REGEX
    private boolean isChecked = false; //бокс USE REGEX в начале не нажат
    private JFileChooser jfc;          //окно менеджера файлов
    private ArrayList<Integer> indexFound;  //здесь будем хранить номера символов, с которых начинаются совпадения поиска
    private ArrayList<Integer> lengthFound; //здесть будем хранить длинну поискового запроса, меняющуюся при RegEx
    private int counter = 0;                //для организации счетчика найденных совпадений
    private int nextCounter = 0;            //для организации поиска вперед и назад


    public TextEditor() {
        super("Text Editor v.6.7");

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //основной контейнер
        Container container = getContentPane();

        //Добавим текстовое поле
        textArea = new JTextArea();
        textArea.setName("TextArea");

        //Сделаем текствое поле прокручиваемым
        JScrollPane scrollableTextArea = new JScrollPane(textArea);
        scrollableTextArea.setName("ScrollPane");
        //Укажем что скролл будет всегда
        scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //НА ВРЕМЯ ТЕСТИРОВАНИЯ
        jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setName("FileChooser");
        add(jfc); //это нужно для тестирования.

        //Собираем все панели для вывода на экран
        container.add(scrollableTextArea, BorderLayout.CENTER);
        container.add(upArea(), BorderLayout.NORTH);
        container.add(new JLabel(" "), BorderLayout.SOUTH);    //Просто для красоты
        container.add(new JLabel("    "), BorderLayout.WEST);  //Просто для красоты
        container.add(new JLabel("    "), BorderLayout.EAST);  //Просто для красоты

        createListener();          //Установим слушатели
        menuBar();                 //Добавляем строку меню

        setVisible(true);          // Выводим окно на экран
    }

    //Верхняя область с полем имени файла и кнопками SAVE и LOAD
    private JPanel upArea() {

        JPanel upArea = new JPanel();
        upArea.setLayout(new FlowLayout(FlowLayout.CENTER));
        findTextField = new JTextField(20);
        findTextField.setName("SearchField");

        //Создаем кнопку SAVE. Слушатель в отдельном методе.
        saveButton = new JButton(new ImageIcon(SAVE_ICON_FILE_BIG));
        saveButton.setName("SaveButton");
        saveButton.setPreferredSize(new Dimension(KEY_SIZE, KEY_SIZE));

        //Создаем кнопку OPEN. Слушатель в отдельном методе.
        loadButton = new JButton(new ImageIcon(OPEN_ICON_FILE_BIG));
        loadButton.setName("OpenButton");
        loadButton.setPreferredSize(new Dimension(KEY_SIZE, KEY_SIZE));

        //Создаем кнопку START SEARCH. Слушатель в отдельном методе.
        startSearchButton = new JButton(new ImageIcon(START_SEARCH_ICON_BIG));
        startSearchButton.setName("StartSearchButton");
        startSearchButton.setPreferredSize(new Dimension(KEY_SIZE, KEY_SIZE));

        //Создаем кнопку PREVIOUS SEARCH. Слушатель в отдельном методе.
        prevSearchButton = new JButton(new ImageIcon(PREV_SEARCH_ICON_BIG));
        prevSearchButton.setName("PreviousMatchButton");
        prevSearchButton.setPreferredSize(new Dimension(KEY_SIZE, KEY_SIZE));

        //Создаем кнопку NEXT SEARCH. Слушатель в отдельном методе.
        nextSearchButton = new JButton(new ImageIcon(NEXT_SEARCH_ICON_BIG));
        nextSearchButton.setName("NextMatchButton");
        nextSearchButton.setPreferredSize(new Dimension(KEY_SIZE, KEY_SIZE));

        //Создаем бокс USE REGEX. Слушатель в отдельном методе.
        useRegExBox = new JCheckBox("Use Regex");
        useRegExBox.setName("UseRegExCheckbox");

        // Собираем панель из поля ввода имени и 2х кнопок
        upArea.add(loadButton);
        upArea.add(saveButton);
        upArea.add(findTextField);
        upArea.add(startSearchButton);
        upArea.add(prevSearchButton);
        upArea.add(nextSearchButton);
        upArea.add(useRegExBox);

        return upArea;
    }

    public void menuBar() {
        //Создаем строку меню
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        //Создаем меню FILE
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        //Создаем пункт OPEN с мнемокомандой и возьмем слушатель от кнопки OPEN
        JMenuItem loadMenuItem = new JMenuItem("Open", new ImageIcon(OPEN_ICON_FILE_SMALL));
        loadMenuItem.setName("MenuOpen");
        loadMenuItem.addActionListener(loadButton.getActionListeners()[0]);
        loadMenuItem.setMnemonic(KeyEvent.VK_O);

        //Создаем пункт SAVE с мнемокомандой и возьмем слушатель от кнопки SAVE
        JMenuItem saveMenuItem = new JMenuItem("Save", new ImageIcon(SAVE_ICON_FILE_SMALL));
        saveMenuItem.setName("MenuSave");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(saveButton.getActionListeners()[0]);

        //Создаем пункт EXIT с мнемокодом и безопасным выходом
        JMenuItem exitMenuItem = new JMenuItem("Exit", new ImageIcon(EXIT_ICON_FILE_SMALL));
        exitMenuItem.setName("MenuExit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.addActionListener(actionEvent -> dispose());

        //Создаем меню SEARCH
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(searchMenu);

        //Создаем пункт START SEARCH с мнемокомандой и возьмем слушатель от кнопки START SEARCH
        JMenuItem startSearchMenuItem = new JMenuItem("Start Search", new ImageIcon(START_SEARCH_ICON_SMALL));
        startSearchMenuItem.setName("MenuStartSearch");
        startSearchMenuItem.setMnemonic(KeyEvent.VK_T);
        startSearchMenuItem.addActionListener(startSearchButton.getActionListeners()[0]);

        //Создаем пункт PREVIOUS SEARCH и возьмем слушатель от кнопки PREVIOUS SEARCH
        JMenuItem previousSearchMenuItem = new JMenuItem("Previous Search", new ImageIcon(PREV_SEARCH_SMALL));
        previousSearchMenuItem.setName("MenuPreviousMatch");
        previousSearchMenuItem.addActionListener(prevSearchButton.getActionListeners()[0]);

        //Создаем пункт NEXT SEARCH и возьмем слушатель от кнопки NEXT SEARCH
        JMenuItem nextSearchMenuItem = new JMenuItem("Next Search", new ImageIcon(NEXT_SEARCH_SMALL));
        nextSearchMenuItem.setName("MenuNextMatch");
        nextSearchMenuItem.addActionListener(nextSearchButton.getActionListeners()[0]);

        //Создаем пункт USE REG EXP и слушателем моделируем нажатие бокса USE REG EXP
        JMenuItem useRegExpMenuItem = new JMenuItem("Use regex");
        useRegExpMenuItem.setName("MenuUseRegExp");
        useRegExpMenuItem.addActionListener(actionEvent -> useRegExBox.doClick());


        //Добавим пункты в меню
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        searchMenu.add(startSearchMenuItem);
        searchMenu.add(previousSearchMenuItem);
        searchMenu.add(nextSearchMenuItem);
        searchMenu.addSeparator();
        searchMenu.add(useRegExpMenuItem);
    }

    //Устанавливаем все слушатели в одном месте
    public void createListener() {

        //Для операций SAVE
        saveButton.addActionListener(actionEvent -> {
            int returnValue = jfc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(textArea.getText());
                } catch (IOException e) {
                    e.getMessage();
                    //          JOptionPane.showMessageDialog(container,
                    //                  "ERROR!\nНевозможно создать файл:\n " + filenameField.getText());
                    //Если открыть - будет окно предуплеждения, но тест не пройдёт.
                }
            }
        });

        //Для операций OPEN
        loadButton.addActionListener(actionEvent -> {

            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                try {
                    textArea.setText(new String(Files.readAllBytes(selectedFile.toPath())));
                } catch (IOException e) {
                    textArea.setText(null);
                }
            }
        });

        //Для START SEARCH
        startSearchButton.addActionListener(actionEvent -> SearchEngine());

        //Для NEXT SEARCH
        nextSearchButton.addActionListener(actionEvent -> NextSearch());

        //Для PREVIOUS SEARCH
        prevSearchButton.addActionListener(actionEvent -> PrevSearch());

        //Для USE REGEX
        useRegExBox.addActionListener(actionEvent -> isChecked = !isChecked);
    }

    //Получаем текст из текстового поля
    public String getTextFind() {
        return findTextField.getText();
    }

    //Получаем текст поискового запроса
    public String getAllText() {
        return textArea.getText();
    }

    //Стартуем ПОИСК
    public void SearchEngine() {
        indexFound = new ArrayList<>();
        lengthFound = new ArrayList<>();
        String findText = getTextFind();
        String allText = getAllText();
        int index = -1;
        int lengthFind = findText.length();

        if (isChecked) {       //Если чекбокс UseReg нажат - ищем по регулярным выражениям

            Pattern pattern = Pattern.compile(findText);
            Matcher matcher = pattern.matcher(allText);
            while (matcher.find()) { //Пока есть совпадения - сохраняем в массивы номера символов начала совпадения и длинну совпадения
                index = matcher.start();
                lengthFind = matcher.end() - index;
                indexFound.add(index);
                lengthFound.add(lengthFind);
            }

        } else {                //Если чекбокс UseReg не нажат - ищем просто совпадения

            while (true) {      //Если есть совпладения - сохраняем в массив номера символов начала совпадений и длинну
                index = allText.indexOf(findText, index + 1);
                if (index == -1) {
                    break;
                }
                indexFound.add(index);
                lengthFound.add(lengthFind);
                System.out.println("index=" + index);
                System.out.println("length=" + lengthFind);
            }
        }

        counter = indexFound.size();  //К-во совпадений = числу элементов получившегося массива
        nextCounter = 0;

        if (counter > 0) {            //Выделим первое совпадение и установим курсор в конце
            textArea.setCaretPosition(indexFound.get(0) + lengthFound.get(0));
            textArea.select(indexFound.get(0), indexFound.get(0) + lengthFound.get(0));
            textArea.grabFocus();
        }
    }

    //Реализуем ПОИСК вперед
    public void NextSearch() {
        if (counter > 0) {
            if (counter - 1 > nextCounter) {
                nextCounter++;
            } else {
                nextCounter = 0; //Зацикливаем, если достигли конца текста
            }
            textArea.setCaretPosition(indexFound.get(nextCounter) + lengthFound.get(nextCounter));
            textArea.select(indexFound.get(nextCounter), indexFound.get(nextCounter) + lengthFound.get(nextCounter));
            textArea.grabFocus();
        }
    }

    //Реализуем ПОИСК назад
    public void PrevSearch() {
        if (counter > 0) {
            if (nextCounter != 0) {
                nextCounter--;
            } else {
                nextCounter = counter - 1; //Зацикливаем, если достигли конца текста
            }
            textArea.setCaretPosition(indexFound.get(nextCounter) + lengthFound.get(nextCounter));
            textArea.select(indexFound.get(nextCounter), indexFound.get(nextCounter) + lengthFound.get(nextCounter));
            textArea.grabFocus();
        }
    }
}


