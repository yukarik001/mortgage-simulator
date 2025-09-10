package com.example.demo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RBean {

private Integer loanAmount = 0; //借入金額
private Integer loanTerm = 0; //返済（残）年数
private Integer loanNo = 0; //返済（残）回数
private Double initialRate = 0.0;
private String rule = "no"; //ルール適用

private List<InterestChange> interestChanges;
private List<PrePayment> prepayments;

//計算後
private Integer initialPay = 0; //初回返済額
private Integer sHensai = 0; //総返済額
private Integer sGankin = 0; //元金
private Integer sRisoku = 0; //総支払利息額
private List<MonthlyResult> results;
}
