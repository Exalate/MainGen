import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Main {

    public static void main(String[] args) {

        File fileOptions = new File("", "settings.xml");
        File fileTextBefore = new File("", "source-data.tsv");

        FormatText ft = new FormatText(fileOptions, fileTextBefore);

        ft.launch();

        ArrayList<String> mainText = ft.getMainText();

        String finishText = "";

        for(int i = 0; i<mainText.size(); i++){
            finishText = finishText + mainText.get(i)+System.getProperty("line.separator");
        }

        try {

            OutputStream os = new FileOutputStream("\\D:/output.txt");
            OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_16);

            // create a new FileInputStream to read what we write
            FileInputStream in = new FileInputStream("\\D:/output.txt");

            writer.write(finishText);

            writer.flush();

            // get and print the encoding for this stream
            System.out.println("" + writer.getEncoding());

            // read what we write
            //System.out.println("" + (char) in.read());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        try (
//                // FileWriter writer = new FileWriter("\\D:/output.txt", false)) {
//                // запись всей строки
//
//                OutputStream os = new FileOutputStream("test.txt");
//                OutputStreamWriter writer = new OutputStreamWriter(os);
//
//
//                writer.write(finishText);
//                // запись по символам
//                //writer.append('\n');
//                //writer.append('E');
//
//                writer.flush();
//        } catch (Exception  ex) {
//            System.out.println(ex.getMessage());
//        }



    }
}

class Column{

    private String title;
    private int width;

    Column(String title, int width){
        this.title = title;
        this.width = width;
    }
    public String getTitle(){
        return this.title;
    }
    public int getWidth(){
        return this.width;
    }
}

class FormatText{

    private int remHeight = this.height;
    //private boolean isFirst = true;

    private ArrayList <String> mainText = new ArrayList<String>();

    private ArrayList <Column> columns = new ArrayList<Column>();

    private int width;
    private int height;

    private List<String[]> textBefore; //

    private String sepString;
    private TextString emptyString;

    private TextString headlineTS;
    //private TextString[] textStrings;

    public FormatText(File fileOptions, File fileTextBefore){
        readData(fileOptions, fileTextBefore);
    }
    public void readData(File fileOptions, File fileTextBefore){

        // ПОЛУЧЕНИЕ НАСТРОЕК СТРАНИЦЫ
        try {
            // ЧТЕНИЕ НАСТРОЕК ИЗ ФАЙЛА
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fileOptions);

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("page");
            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;

                    setWidth(Integer.parseInt(element.getElementsByTagName("width").item(0).getTextContent()));
                    setHeight(Integer.parseInt(element.getElementsByTagName("height").item(0).getTextContent()));

                }
            }
            nodeList = doc.getElementsByTagName("column");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;

                    String s1 = element.getElementsByTagName("title").item(0).getTextContent();
                    int i1 = Integer.parseInt(element.getElementsByTagName("width").item(0).getTextContent());

                    addColumn(new Column(s1,i1));
                }
            }
            //-----------------------------------------------------------
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Main.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        //---------------------------------------КОНЕЦ ПОЛУЧЕНИЯ НАСТРОЕК СТРАНИЦЫ

        // ЧТЕНИЕ ТЕКСТА ДЛЯ ПЕРЕФОРМАТИРОВАНИЯ
        TsvParserSettings settings = new TsvParserSettings();

        TsvParser parser = new TsvParser(settings);
        List<String[]> textBefore = parser.parseAll(fileTextBefore, "UTF-16");
        //-----------------------КОНЕЦ ЧТЕНИЯ ТЕКСТА ДЛЯ ПЕРЕФОРМАТИРОВНИЯ
        SetTextBefore(textBefore);

    }
    public void addColumn(Column column){
        this.columns.add(column);
    }
    public void setWidth(int width){
        if(width > 0) {
            this.width = width;
        }
        else{
            this.width = 0;
        }
    }
    public void setHeight(int height){
        if(height > 0) {
            this.height = height;
        }
        else{
            this.height = 0;
        }
    }
    public void SetTextBefore(List<String[]> textBefore){
        this.textBefore = textBefore;
    }

    public ArrayList<String> getMainText(){
        return this.mainText;
    }

    // ЗАПУСК
    public void launch() {

        setSepString();
        headlineTS = new TextString(columns);
        emptyString = new TextString(new String[]{""}, columns);
        remHeight = this.height;

        for(String[] i : textBefore) {
            outputText(new TextString(i, columns));
        }
        out();
    }

    //Вывод строки(или строк) в форматированный текст
    private void outputText(TextString text){//(ArrayList<String> text) {

        //Требуется вывести шапку и разделитель строк:
        if(remHeight == this.height){
            outputTextLine(headlineTS.getStringText());
        }

        //проверка что строка протискивается целиком в высоту строки:
        if (remHeight - (text.getAmLine()+1) < 0){    //НЕ УМЕЩАЕТСЯ, нужно создавать новый лист, остаток текущего листа закрывать пустышкой

            for (int i = 0; i <= remHeight; i++){
                outputTextLine(emptyString.getStringText());
            }
            outputText("~");
            remHeight = this.height;
            outputTextLine(headlineTS.getStringText());
        }

        outputText(sepString);
        outputTextLine(text.getStringText());
    }

    private void outputTextLine(ArrayList<String> text){

        for (int i = 0; i < text.size(); i++) {
            this.mainText.add(text.get(i));
            remHeight--;
        }

    }

    private void outputText(String text){
            this.mainText.add(text);
            remHeight--;
    }

    // ВЫВОД РЕЗУЛЬТАТА В КОНСОЛЬ:
    private void out(){

        for (int i = 0; i < mainText.size(); i++){
            System.out.println(mainText.get(i));
        }
    }

    // Расчет разделителя строк:
    private void setSepString(){

        int x = 0;
        String sepString = "";

        for (int i = 0; i < this.columns.size(); i++){
            x = x + this.columns.get(i).getWidth() + 3;
        }
        x++;

        for (int i = 0; i < x; i++){
            sepString = sepString + "-";
        }
        this.sepString = sepString;
    }
}

class TextString{

    private int amLine = 0;
    private ArrayList <String> stringText = new ArrayList<String>();

    public ArrayList <String> getStringText(){
        return stringText;
    }

    public TextString(String[] strLineBefore, ArrayList <Column> columns){

        int colWidth;
        String curText;
        String curLine = "";
        String sepCurText;
        boolean finish = false;

        String sepSpace = "[А-я0-9A-z]";//"\\w";
        int i = -1;
        while (finish == false) {

            i++;

            colWidth = columns.get(i).getWidth();

            if (strLineBefore.length > i) {
                curText = strLineBefore[i];    //Соответствующая колонка текста
            } else {
                curText = "";
            }

                if (curText.length() == 0) {
                    if (i == 0) {
                        curLine = curLine + "| " + spaceValue(colWidth) + " ";
                    } else if (i == columns.size() - 1) {
                        curLine = curLine + " " + spaceValue(colWidth) + " |";
                    } else {
                        curLine = curLine + "| " + spaceValue(colWidth) + " |";
                    }
                } else if (curText.length() <= colWidth) { //длина текста меньше возможной ширины, всё норм, можно выводить

                    strLineBefore[i] = "";

                    if (i == 0) {
                        curLine = curLine + "| " + curText + spaceValue(colWidth - curText.length()) + " ";
                    } else if (i == columns.size() - 1) {
                        curLine = curLine + " " + curText + spaceValue(colWidth - curText.length()) + " |";
                    } else {
                        curLine = curLine + "| " + curText + spaceValue(colWidth - curText.length()) + " |";
                    }
                } else {                             //длина текста больше возможного (плохо), нужно дербанить
                    sepCurText = curText.substring(0, colWidth+1);

                    //НАЧАЛО ДЕРБЕНТА ПО РАЗЛИЧНЫМ СИМВОЛАМ

                    String str = sepCurText;

                    for(int iS = str.length()-1; iS >= 0; iS--){

                        char bukva = str.charAt(iS);
                        String bukvaStrokoy = String.valueOf(bukva);

                        Pattern pattern = Pattern.compile(sepSpace);
                        Matcher matcher = pattern.matcher(bukvaStrokoy);
                        boolean isRazdelitel = !(matcher.matches());
//                        if(isRazdelitel){
//                            System.out.println("ЭТО РАЗДЕЛИТЕЛЬ, w = " + sepSpace + ", a bukvaStrokoy = "+ bukvaStrokoy);
//                        }
//                        else{
//                            System.out.println("ЭТО НЕ! РАЗДЕЛИТЕЛЬ, w = " + sepSpace + ", a bukvaStrokoy = "+ bukvaStrokoy);
//                        }
                        if(iS == str.length()-1 && isRazdelitel){   //следующий за пределом симовол это разделитель, поэтому смело берем все, что входит в предел - ВЫХОД ИЗ ЦИКЛА
                            sepCurText = sepCurText.substring(0, colWidth);
                            break;
                        }

                        if(isRazdelitel){
                            sepCurText = sepCurText.substring(0,iS+1);
                            break;
                        }
                    }

                    if(sepCurText.length() > colWidth){
                        sepCurText = sepCurText.substring(0,colWidth);
                    }

                    //КОНЕЦ ДЕРБЕНТА
                    strLineBefore[i] = strLineBefore[i].substring(sepCurText.length()).trim();

                    //System.out.println("spaceValue(colWidth - curText.length()) = " + spaceValue(colWidth - sepCurText.length()));
                    if (i == 0) {
                        curLine = curLine + "| " + sepCurText + spaceValue(colWidth - sepCurText.length()) +" ";
                    } else if (i == columns.size() - 1) {
                        curLine = curLine + " " + sepCurText + spaceValue(colWidth - sepCurText.length()) +" |";
                    } else {
                        curLine = curLine + "| " + sepCurText + spaceValue(colWidth - sepCurText.length()) +" |";
                    }

                }

            if (columns.size() == i + 1) {

                stringText.add(curLine);
                amLine++;
                curLine = "";
                i = -1;

                finish = true;
                for (String str : strLineBefore) {
                    if (str.length() != 0) {
                        finish = false;
                        break;
                    }
                }
            }

        }

    }

    public TextString(ArrayList <Column> columns){

        Column c;
        String curString = "";

        //обход настрок колонок (название и размер)
        for (int i = 0; i < columns.size(); i++){

            c = columns.get(i);
            if(c.getWidth()== c.getTitle().length() || c.getWidth() > c.getTitle().length()){
                if(i == 0) {
                    curString = curString + "| " + c.getTitle() + spaceValue(c.getWidth() - c.getTitle().length())+" ";
                }
                else if(i == columns.size()-1){
                    curString = curString +" " + c.getTitle() + spaceValue(c.getWidth() - c.getTitle().length())+" |";
                }
                else{
                    curString = curString + "| " + c.getTitle() + spaceValue(c.getWidth() - c.getTitle().length())+" |";
                }
                amLine = 1;
            }
            else{
            }
        }
        stringText.add(curString);

    }

    private String spaceValue(int x){
        String ret = "";
        for (int i = 0; i < x; i++){
            ret = ret + " ";
        }
        return ret;
    }

    public int getAmLine(){
        return amLine;
    }

}