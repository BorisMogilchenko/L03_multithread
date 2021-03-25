package ru.quazar.l03;

import java.util.Objects;

/**
 *
 * @version $Id: FileGetter.java,v 1.0 2021-01-15 23:30:42 Exp $
 * @author  <A HREF="mailto:boris.mogilchenko@yandex.ru">Boris Mogilchenko</A>
 */

public class Book {
    private String bookTitle;
    private boolean bookIsBusy;

    public Book(String title, boolean busy) {
        this.setBookTitle( title );
        this.setBookIsBusy( busy );
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public boolean getBookIsBusy() {
        return bookIsBusy;
    }

    public void setBookIsBusy(boolean bookIsBusy) {
        this.bookIsBusy = bookIsBusy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return bookTitle == book.bookTitle&&
                bookIsBusy == book.bookIsBusy;
    }

    @Override
    public int hashCode() {

        return Objects.hash(bookTitle, bookIsBusy);
    }

    @Override
    public String toString() {
        return String.format( "%s", getBookTitle(), getBookIsBusy() );
    }
}
