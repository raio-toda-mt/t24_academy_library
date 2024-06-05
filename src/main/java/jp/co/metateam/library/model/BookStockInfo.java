package jp.co.metateam.library.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookStockInfo {
    private String title;
    private int totalCount;
    public List<Abc> dailyDtail;
}
