package com.example.demo;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;




@Controller
@SessionAttributes("rb")


public class Controll {
	private static final Logger logger = LoggerFactory.getLogger(Controll.class);
@Autowired
private LoanCalculatorService loanCalculatorService;

@Autowired
private PdfExportService pdfExportService;

@RequestMapping("/")
public String start(Model model) {
model.addAttribute("rb", new RBean()); // ← ここが重要！
return "input";
}

@RequestMapping("/checkRule")
@ResponseBody
public String checkRulee(@RequestParam(value = "rule", required = false) String rule) {
// チェックされていない場合は null → "no" に補正
String result = (rule != null && rule.equals("yes")) ? "ルール適用" : "";
return result;
}
@RequestMapping("/calculate")
public ModelAndView calculate(@ModelAttribute RBean rb, ModelAndView m, HttpSession session) {
	session.setAttribute("rb", rb);
    // 未入力対策：loanNoがnullなら年数×12に設定
    if (rb.getLoanNo() == null) {
        rb.setLoanNo(rb.getLoanTerm() * 12);
    }

    // 繰り上げ返済のフィルタリングとViewへの受け渡し
    List<PrePayment> prepayments = rb.getPrepayments();
    if (prepayments != null) {
        prepayments.removeIf(p -> p == null || p.getMonth() == null || p.getAmount() == null);
        m.addObject("prepayments", prepayments);
    } else {
        logger.info("繰り上げ返済は未入力または空です");
    }

    // 計算処理
    List<MonthlyResult> results = loanCalculatorService.calculate(rb);

    // 金額のフォーマット
    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.JAPAN);
    m.addObject("formattedamount", formatter.format(rb.getLoanAmount()));
    m.addObject("formattedinitialPay", formatter.format(rb.getInitialPay()));
    m.addObject("formattedsHensai", formatter.format(rb.getSHensai()));
    m.addObject("formattedsGankin", formatter.format(rb.getSGankin()));
    m.addObject("formattedsRisoku", formatter.format(rb.getSRisoku()));

    // その他のデータをViewに渡す
    m.addObject("rb", rb);
    m.addObject("monthlyResults", results); // ← これを追加
    m.setViewName("confirm");


    return m;
}

//一時的なリセット処理（数値のみ初期化）
@RequestMapping("/clear")
public ModelAndView clear(SessionStatus status) {
    status.setComplete();

    ModelAndView mv = new ModelAndView("input");
    mv.addObject("rb", new RBean());
    return mv;
}

//金利変動・繰り上げ返済が表示されないので、
//「戻る」ボタンから「リセット」ボタンに変更。
//＊＊＊臨時措置(2025/09/10)

/*

@RequestMapping("/back")
public String back(HttpSession session, Model model) {
    RBean rb = (RBean) session.getAttribute("rb");
    if (rb == null) {
        rb = new RBean(); // セッション切れ時の初期化
    }

    model.addAttribute("rb", rb);

    // 繰り上げ返済データを渡す
    List<PrePayment> prepayments = rb.getPrepayments();
    if (prepayments != null && !prepayments.isEmpty()) {
        model.addAttribute("prepayments", prepayments);
    }

    // 金利変動データを渡す
    List<InterestChange> interestChanges = rb.getInterestChanges();
    if (interestChanges != null && !interestChanges.isEmpty()) {
        model.addAttribute("interestChanges", interestChanges);
    }

    return "input";
}
*/


//PDF化
//PDFはクライアント側で処理（確認画面のjavascript　2025/09/14)
@GetMapping("/confirm")
public String showConfirm(Model model,HttpSession session) {
	RBean rb = (RBean) session.getAttribute("rb");
    if (rb == null) {
        logger.warn("セッションが切れているため、確認画面を表示できません");
        return "redirect:/";
    }

    List<MonthlyResult> results = loanCalculatorService.calculate(rb);

    // 🔍 ログでサイズを確認
    logger.info("monthlyResults size: " + results.size());
    System.out.println("monthlyResults size: " + results.size());
    // 🔍 1件目の中身を確認（あれば）
    if (!results.isEmpty()) {
        MonthlyResult first = results.get(0);
        logger.info("1件目: month=" + first.getMonth() + ", rate=" + first.getRate() +
                    ", gaku=" + first.getGaku() + ", gan=" + first.getGan() +
                    ", risoku=" + first.getRisoku() + ", zan=" + first.getZan());
    }
    for (MonthlyResult r : results) {
        logger.info("月別結果: month=" + r.getMonth() + ", rate=" + r.getRate() +
                    ", gaku=" + r.getGaku() + ", gan=" + r.getGan() +
                    ", risoku=" + r.getRisoku() + ", zan=" + r.getZan());
    }

    model.addAttribute("rb", rb);
    model.addAttribute("monthlyResults", results);
    return "confirm";
}

/*
@GetMapping("/pdf")
public ResponseEntity<byte[]> exportPdf(HttpSession session) {
	
	RBean bean = (RBean) session.getAttribute("rb"); // ← ここで定義！
    if (bean == null) {
        logger.warn("セッションが切れているため、PDF出力できません。");
        return ResponseEntity.badRequest().build();
    }

List<MonthlyResult> results = loanCalculatorService.calculate(bean);
byte[] pdfBytes = pdfExportService.generatePdf(results);

return ResponseEntity.ok()
.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=repayment.pdf")
.contentType(MediaType.APPLICATION_PDF)
.body(pdfBytes);


	}
*/
}
