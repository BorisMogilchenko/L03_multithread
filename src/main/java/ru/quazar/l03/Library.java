package ru.quazar.l03;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.lang.Thread.sleep;

/**
 *
 * @version $Id: FileGetter.java,v 1.0 2021-01-15 23:30:42 Exp $
 * @author  <A HREF="mailto:boris.mogilchenko@yandex.ru">Boris Mogilchenko</A>
 *
 * In created Library every 250 milliseconds new thread calls free book.
 * After job finished, return count of free books and active threads.
 *
 */

@Data
@AllArgsConstructor
public class Library {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
    private static final int WORK_TIME = 30000;
    private static ScheduledFuture<?> findBook;
    private static int TASK_CYCLE = 250;
    private static Set< LibraryClientThread > setBook = new HashSet<>();
    private static int rndNumber;
    private static AtomicInteger threadNameSeq = new AtomicInteger(0);

    static void start(List<Book> booksCatalog) {
        int initialDelay = 0;

        System.out.println("");
        System.out.println("The Library opened!!!...");
        findBook = scheduler.scheduleAtFixedRate( new LibraryTask(booksCatalog), initialDelay, TASK_CYCLE, TimeUnit.MILLISECONDS );
    }

    static void stop(List<Book> booksCatalog) {
        int threadCount = 0;

/*        scheduler.schedule( new Runnable() {
            public void run() {findBook.cancel( true );}
        }, WORK_TIME,SECONDS);*/

        System.out.println("");
        System.out.println("The Library closed!!!...");

        threadCount = setBook.size();
        System.out.println("Количество потоков: " + threadCount);
        System.out.println("");

        long countIsAlive = setBook
                .stream()
                .filter( thread -> thread.isAlive() )
                .count();
        System.out.println("Число запущенных потоков: " + countIsAlive);
        System.out.println("");

        long countInterruptedThreads = setBook
                .stream()
                .filter( thread -> !thread.isAlive() )
                .count();
        System.out.println("Число завершенных потоков: " + countInterruptedThreads);
        System.out.println("");

        long checkIsBusy = booksCatalog
                .stream()
                .filter( book -> book.getBookIsBusy() == true)
                .count();
        System.out.println("Количество невыданных книг: " + checkIsBusy);
        System.out.println("");

        findBookNotBusy(booksCatalog);
        setThreadIsAlive(setBook);
        setThreadIsWaited(setBook);

        findBook.cancel(true);
        scheduler.shutdownNow();

    }

    /**
     * Start new thread LibraryClientThread
     */
    static class LibraryTask  implements Runnable {

        private List<Book> booksCatalog;
        private Thread taskThread;
        private String threadName;

        LibraryTask(List<Book> libCatalog) {
            this.booksCatalog = libCatalog;
        }

        @Override
        public void run() {
            String threadName = "Thread N" + threadNameSeq.incrementAndGet();
            LibraryClientThread libraryClientThread = new LibraryClientThread(threadName, booksCatalog);
            libraryClientThread.start();

            System.out.println( "Создание задачи " + threadName );
            System.out.println("");

            setBook.add( libraryClientThread );

            System.out.println( "Объем множества потоков: " + setBook.size() );
            System.out.println("");

        }

    }

    /**
     * Class LibraryClientThread find first free book, than changes its status 'isBusy' on 'true'.
     * After certain time changes its status 'isBusy' on 'false', and finished job.
     */
    static class LibraryClientThread extends Thread {
        private final int minRange = 1000;
        private final int maxRange = 3000;
        private List<Book> booksCatalog;
        private Thread t;
        private String threadName;

        LibraryClientThread(String threadName, List<Book> libCatalog) {
            this.threadName = threadName;
            this.booksCatalog = libCatalog;
        }

        @Override
        public void run() {
            Book book;
            int rndNumber;

            System.out.println( "Запуск потока " + threadName );
            System.out.println("");

            int i = 0;
            while (i < booksCatalog.size()) {
                book = booksCatalog.get( i );
                if (!book.getBookIsBusy()) {
                    synchronized (book) {
                        try {
                            Random rnd = new Random();
                            rndNumber = minRange + rnd.nextInt( maxRange - minRange + 1 );
                            booksCatalog.get( i ).setBookIsBusy( true );
                            System.out.println( "Имя потока: " + threadName );
                            System.out.printf( "Время жизни потока: %d%n", rndNumber );
                            System.out.println();
                            System.out.println( "Книга  " + book.getBookTitle() + " выдана" );
                            System.out.println( "Статус книги: " + (book.getBookIsBusy() ? "Нет" : "Да") );
                            System.out.println( "" );
                            threadSleep( (long) rndNumber );
                            throw new InterruptedException("Поток " +  threadName + " прерван");
                        } catch (InterruptedException ex) {
                            book.setBookIsBusy( false );
                            System.out.println( "Книга  " + book.getBookTitle() + " возвращена" );
                            System.out.println( "Статус книги: " + (book.getBookIsBusy() ? "Нет" : "Да") );
                            System.out.println( "" );
                            this.interrupt();
                            System.out.println(ex.getMessage());
                            book.notifyAll();
                        }
                    }
                    break;
                } else {
                    i++;
                }
            }
            System.out.printf( "Поток %s завершен%n", threadName );
        }

        /**
         * Start new thread of LibraryClientThread
         */
        @Override
        public synchronized void start() {
            System.out.println( "Старт потока " + threadName );
            System.out.println("");

            if (t == null) {
                t = new Thread (this, threadName);
                t.start ();
            }
        }

        @Override
        public String toString() {
            return String.format( "LibraryClientThread{booksCatalog}" );
        }
    }

    public static void main(String[] args) throws IOException {
        List<Book> booksCatalog;
        String inFileName = "";

        GettingFile gettingFile = new GettingFile();

        if (args.length == 0) {
            inFileName = "";
        } else {
            inFileName = args[0];
        }

        File inputFile = gettingFile.getFileWithConditions(inFileName);
        FileToBufStream fileToBufStream = new FileToBufStream( inputFile );
        booksCatalog = fileToBufStream.fileToStream();
        System.out.println("Количество книг в каталоге: " + booksCatalog.size());
        System.out.println();
        System.out.println("Перечень книг в каталоге:");

        booksCatalog.forEach(bk -> {
            System.out.println( "Название книги: " + bk.getBookTitle() );
            System.out.println( "Наличие книги: " + (bk.getBookIsBusy() ? "Нет" : "Да") );
        });

        Library myLibrary = new Library();
        try {
            myLibrary.start(booksCatalog);

            Thread.sleep(WORK_TIME);

            myLibrary.stop(booksCatalog);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param millis Time for sleeping for current thread
     */
    private static void threadSleep(long millis) {
        try {
            sleep( millis );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param bookList Count free books from List<Book> by Predicate<Book> predicate
     */
    private static void findBookNotBusy(List<Book> bookList) {
        System.out.println("Finding free books...");
        Predicate<Book> isBusy = x -> !x.getBookIsBusy();
        System.out.println("");
        System.out.println("Количество свободных книг: " + findAll( bookList, isBusy.negate() ).size());
        System.out.println("");
    }

    /**
     * @param threadSet Set of the started and live threads
     */
    private static void setThreadIsAlive(Set<LibraryClientThread> threadSet) {
        int countThreads = 0;

        System.out.println("Checking started threads...");
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getState()==Thread.State.RUNNABLE) countThreads++;
        }
        System.out.println("");
        System.out.println("Количество запущенных потоков: " + countThreads);
        System.out.println("");
    }

    /**
     * @param threadSet Set of the started and not interrupted threads
     */
    private static void setThreadIsWaited(Set<LibraryClientThread> threadSet) {
        int countThreads = 0;

        System.out.println("Checking waited threads...");
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getState()==Thread.State.WAITING) countThreads++;        }
        System.out.println("");
        System.out.println("Количество ожидающих потоков: " + countThreads);
        System.out.println("");
    }

    /**
     * @param bookList List of books
     * @param predicate condition for filteredList
     * @return filteredList by Predicate<Book> predicate
     */
    private static List<Book> findAll(List<Book> bookList, Predicate<Book> predicate) {
        List<Book> filteredList = new ArrayList<>();
        for (Book book : bookList) {
            if (predicate.test( book )) {
                filteredList.add( book );
            }
        }
        return filteredList;
    }

}
