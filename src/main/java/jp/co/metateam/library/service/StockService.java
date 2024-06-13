package jp.co.metateam.library.service;

//import static org.mockito.Answers.values;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

//import org.antlr.v4.runtime.misc.Triple;
//import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.Lendable_date;
//import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;

import java.util.Date;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<CalendarDto> generateValues(Integer year, Integer month, Integer daysInMonth) {
        // FIXME ここで各書籍毎の日々の在庫を生成する処理を実装する
        // FIXME ランダムに値を返却するサンプルを実装している
        List<BookMst> books = this.bookMstRepository.findAll();

        List<CalendarDto> values = new ArrayList<>();

        for (int bookList = 0; bookList < books.size(); bookList++) {
            BookMst book = books.get(bookList);
            // values.put(book.getTitle()); // 対象の書籍名

            List<Stock> stockCount = this.stockRepository.findByBookMstIdAndStatus(book.getId(),
                    Constants.STOCK_AVAILABLE);
            // values.put(book.getTitle(),stockCount.size()); // 対象の書籍名 , // 対象書籍の在庫総数

            CalendarDto calendarDto = new CalendarDto();
            calendarDto.setTitle(book.getTitle());
            calendarDto.setTotalCount(stockCount.size());

            // List<Integer> a = new ArrayList<>();
            // a.add(stockCount.size());
            List<Lendable_date> dailyDuplication = new ArrayList<>();

            for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
                // LocalDate startDate = LocalDate.of(year, month, n);

                Calendar cl = Calendar.getInstance();
                cl.set(Calendar.YEAR, year);
                cl.set(Calendar.MONTH, month - 1);
                cl.set(Calendar.DATE, dayOfMonth);
                Date date = new Date();
                date = cl.getTime();

                Lendable_date dailyList = new Lendable_date();
                List<Object[]> stockList = stockRepository.calendar(book.getId(), date);
                stockList.size();

                dailyList.setDailyCount(stockList.size());
                dailyList.setExpectedRentalOn(date);
                // abc.setStockId(stockList.get(0));
                dailyList.setStockId(stockList.isEmpty() ? null : stockList.get(0)[0].toString());
                dailyDuplication.add(dailyList);
            }

            calendarDto.setDailyDetail(dailyDuplication);

            values.add(calendarDto);
        }
        return values;
    }
}
