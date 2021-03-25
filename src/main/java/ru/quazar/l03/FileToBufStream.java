package ru.quazar.l03;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Get file from source.
 *
 * @version $Id: FileGetter.java,v 1.0 2021-01-15 23:30:42 Exp $
 * @author  <A HREF="mailto:boris.mogilchenko@yandex.ru">Boris Mogilchenko</A>
 */

class FileToBufStream {

    private File inFile;

    FileToBufStream(File inputFile) {
        this.inFile = inputFile;
    }

    /**
     * @return Catalog of books for Library loaded from external file
     */
    public List<Book> fileToStream() {
        List<Book> booksCatalog = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(inFile);
             InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
             BufferedReader bufRead = new BufferedReader(isr)
        ) {
            String name;
            String line;
            while ((line = bufRead.readLine()) != null) {
                name = makeName(line.toCharArray());
                booksCatalog.add( new Book(name, false) );
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
        return booksCatalog;
    }

    /**
     * @param lineSymbols
     * @return
     */
    private String makeName(char[] lineSymbols) {
        StringBuilder target = new StringBuilder();
        for (char ch : lineSymbols)
            if (isValidSymbol(ch))
                target.append(ch);
        return target.toString();
    }

    /**
     * @param source
     * @return
     */
    private static boolean isValidName(String source) {
        boolean hasLetter = false, hasDigit = false, hasSpecial = false, hasSpace = false;
        char line[] = source.toCharArray();

        for (char ch : line)
            if (Character.isLetter(ch))
                hasLetter = true;
            else if (Character.isDigit(ch))
                hasDigit = true;
            else if ("!@#$".indexOf(ch) != -1)
                hasSpecial = true;
            else if (Character.isSpaceChar(ch))
                hasSpace = true;
        return (hasLetter && hasDigit && hasSpecial && hasSpace);
    }

    /**
     * @param symbol
     * @return
     */
    private static boolean isValidSymbol(char symbol) {
        boolean hasChecked = false;
            if (Character.isLetter(symbol)) {
//                System.out.println("");
//                System.out.println("Буквенный символ: " + symbol);
//                System.out.println("");
                hasChecked = true;}
            else if (Character.isDigit(symbol)) {
//                System.out.println("");
//                System.out.println("Цифровой символ: " + symbol);
//                System.out.println("");
                hasChecked = true;}
            else if ("!@#$".indexOf(symbol) != -1) {
//                System.out.println("");
//                System.out.println("Специальный символ: " + symbol);
//                System.out.println("");
                hasChecked = true;}
            else if (Character.isSpaceChar(symbol)) {
//                System.out.println("");
//                System.out.println("Символ пробела: " + symbol);
                hasChecked = true;}

        return hasChecked;
    }

    /**
     * @param s
     * @return
     */
    private boolean isAlphaNumeric(String s){
        String pattern= "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$*()_+])[A-Za-z\\d][A-Za-z\\d!@#$*()_+]";
        if(s.matches(pattern)){
            return true;
        }
        return false;
    }
}
