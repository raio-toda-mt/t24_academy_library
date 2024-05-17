package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    public Optional<String> isStatusError(Integer preStatus, Integer newStatus, Date expectedRentalOn, Date expectedReturnOn){
        Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if(preStatus == RentalStatus.RENT_WAIT.getValue() && newStatus == RentalStatus.RETURNED.getValue()){
            return Optional.of("貸出待ちから返却済みにすることはできません");
        } else if((preStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.RENT_WAIT.getValue())||
                  (preStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.CANCELED.getValue())){
            return Optional.of("貸出中からこのステータスに変更することはできません");
        } else if((preStatus == RentalStatus.RETURNED.getValue() && newStatus == RentalStatus.RENT_WAIT.getValue())||
                  (preStatus == RentalStatus.RETURNED.getValue() && newStatus == RentalStatus.RENTAlING.getValue())||
                  (preStatus == RentalStatus.RETURNED.getValue() && newStatus == RentalStatus.CANCELED.getValue())){
            return Optional.of("返却済みからこのステータスに変更することはできません");
        } else if((preStatus == RentalStatus.CANCELED.getValue() && newStatus == RentalStatus.RENT_WAIT.getValue())||
                  (preStatus == RentalStatus.CANCELED.getValue() && newStatus == RentalStatus.RENTAlING.getValue())||
                  (preStatus == RentalStatus.CANCELED.getValue() && newStatus == RentalStatus.CANCELED.getValue())){
            return Optional.of("キャンセルからこのステータスに変更することはできません");
        } else if (preStatus == RentalStatus.RENT_WAIT.getValue() && newStatus == RentalStatus.RENTAlING.getValue() && !(expectedRentalOn.compareTo(now) == 0)){
            return Optional.of("貸出予定日を今日の日付に設定してください");
        } else if (preStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.RETURNED.getValue() && !(expectedReturnOn.compareTo(now) == 0)){
            return Optional.of("返却予定日を今日の日付に設定してください");
        }
            return Optional.empty();
    }

    public Optional<String> addError(Integer newStatus, Date expectedRentalOn){
        Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (newStatus == RentalStatus.RENTAlING.getValue() && !(expectedRentalOn.compareTo(now) == 0)){
            return Optional.of("貸出予定日を今日の日付に設定してください");
        }
        if (newStatus == RentalStatus.RETURNED.getValue()){
            return Optional.of("返却では登録できません");
        } else if (newStatus == RentalStatus.CANCELED.getValue()){
            return Optional.of("キャンセルでは登録できません");
        }
            return Optional.empty();
}
}