package jp.co.metateam.library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
 
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
import jakarta.validation.Valid;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
 
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.values.RentalStatus;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に遷移
        return "/rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {
        List<Account> accounts = this.accountService.findAll();

        List <Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
        
        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

         Optional<String> vaidErrOptional = rentalManageDto.addError(rentalManageDto.getStatus(), rentalManageDto.getExpectedRentalOn());
            if (vaidErrOptional.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","status",vaidErrOptional.get());
                result.addError(fieldError);
                throw new Exception("Validetion error");
            }

            Optional<String> a = rentalManageService.addrentalAble(rentalManageDto.getStockId(), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()),  new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
            if (a.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","status",a.get());
                result.addError(fieldError);
                throw new Exception("Validetion error");
            }
            

            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        } 
    } 

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findStockAvailableAll();
     
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
 
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
     
        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
     
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());
           
     
            model.addAttribute("rentalManageDto", rentalManageDto);
        }
        return "rental/edit";
    }

 
 @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id,@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
     try {
         if (result.hasErrors()) {
             throw new Exception("Validation error.");
         }

         RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
         Optional<String> vaidErrOptional = rentalManageDto.isStatusError(rentalManage.getStatus(),rentalManageDto.getStatus(), rentalManageDto.getExpectedRentalOn(), rentalManageDto.getExpectedReturnOn());
            if (vaidErrOptional.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","status",vaidErrOptional.get());
                result.addError(fieldError);
                throw new Exception("Validetion error");
            }

                Optional<String> a = rentalManageService.rentalAble(rentalManageDto.getStockId(), rentalManageDto.getId(), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()),  new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
                   if (a.isPresent()){
                       FieldError fieldError = new FieldError("rentalManageDto","status",a.get());
                       result.addError(fieldError);
                       throw new Exception("Validetion error");
                   }

        
         // 更新処理
         rentalManageService.update(Long.valueOf(id), rentalManageDto);

         return "redirect:/rental/index";
     } catch (Exception e) {
        List<Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findStockAvailableAll();
     
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        
         log.error(e.getMessage());

         ra.addFlashAttribute("rentalManageDto", rentalManageDto);
         ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

         //return "redirect:/rental/edit";
         return String.format("redirect:/rental/%s/edit", id);
     } 
    } 
}

 