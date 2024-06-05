package jp.co.metateam.library.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Abc {
    private Integer dailyCount;
    private Date expectedRentalOn;
    private String stockId;
}