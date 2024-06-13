package jp.co.metateam.library.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/* 貸出可能在庫数とそれに紐づく貸出予定日、在庫管理番号 */
/* リンク押下時に貸出予定日と在庫管理番号をセットするため */
public class LendableDate {
    private Integer dailyCount;
    private Date expectedRentalOn;
    private String stockId;
}