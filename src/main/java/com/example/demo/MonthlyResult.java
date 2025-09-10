package com.example.demo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MonthlyResult {
    private Integer month;		//回目
    private Double rate;	//金利
    private Integer gaku;		//返済額
    private Integer gan;		//うち元金
    private Integer risoku;		//うち利息
    private Integer zan;		//残額
    
    public MonthlyResult( ) {
    }
    
    
    public MonthlyResult(int month, double rate, int gaku, int gan, int risoku,int zan) {
        this.month = month;
        this.rate = rate;
        this.gaku = gaku;
        this.gan = gan;
        this.risoku = risoku;
        this.zan = zan;
    }


}
