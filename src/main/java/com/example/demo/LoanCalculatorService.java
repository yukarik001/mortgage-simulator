package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;


@Service



public class LoanCalculatorService {
public List<MonthlyResult> calculate(RBean bean) {
	//Beanから入力値の取り出し
	int loanAmount = bean.getLoanAmount(); //借入金額
	int loanTerm = bean.getLoanTerm(); //年数
	int loanNo = bean.getLoanNo(); //回数
	double ritu = bean.getInitialRate(); //初回金利
	String rule = bean.getRule(); //ルール

	//int sHensai = 0; //総返済額
	int sGankin = loanAmount;
	int sRisoku = 0; //総支払利息
	//PDF編集用項目
	double rate = ritu / 100; //計算用金利
	int repayment = 0; //返済額
	int principal = 0; //うち元金
	int interest = 0; //うち利息
	int zan = loanAmount; //残額
	//回数が未入力なら年数より算出
	if (loanNo == 0) {
		loanNo = loanTerm * 12; 
		bean.setLoanNo(loanNo);
	}
	//金利変動ロジック用
	int month1 = 0;
	double rate1 = 0.0;
	int tugi = 9999;
	int newRepay = 0;
	int j = 0;
	int cnt1 = 0;
	if (bean.getInterestChanges() != null) {
		cnt1 = bean.getInterestChanges().size(); //要素数
	}

	//繰り上げ返済ロジック用
	int month2 = 0; 
	int repay = 0;
	int k = 0;
	int cnt2 = 0;
	if (bean.getPrepayments() != null) {
		cnt2 = bean.getPrepayments().size(); //要素数
	}

	//**********************************
	//初回返済額の算出
	repayment = (int)(Math.round(zan*(((rate/12)/(Math.pow((1+rate/12),(loanNo)) - 1))+rate/12))); //小数点第一位で四捨五入」
	bean.setInitialPay(repayment);
	//初回金利変動情報の取り出し
	if (j < cnt1) {
		month1 = change1(bean,j);
		rate1 = change2(bean,j);
		if ((rule.equals("yes"))&&(rate1 > ritu)) {
			tugi = month1+60; //金利上昇なら上昇時から５年後に返済額を変更
		}
		j++;
	} else {
		month1 = 9999;
	}
	//初回繰り上げ返済情報の取り出し
	if (k < cnt2) {
		month2 = earlypay1(bean,k);
		repay = earlypay2(bean,k);
		k++;
	} else {
		month2 = 9999;
	} 
	//返済月額表リスト生成
	List<MonthlyResult> results = new ArrayList<>();
	//毎月の返済処理
	for (int i = 1; i <= loanNo; i++) { //返済回数分
		//繰り上げ返済
		if (i == month2 ) {
			if (repay < zan) {
				zan -= repay;
			} else {
				zan = 0;
			}
			//次の繰り上げ返済を取り出し
			if (k < cnt2) {
				month2 = earlypay1(bean,k);
				repay = earlypay2(bean,k);
				k++;
			} else {
				month2 = 9999;
			} 
		}
		//金利変動処理
		if (i == month1) {
			if (rule.equals("yes")) { //５年ルール
				if (ritu <rate1) { //金利上昇
					//tugi = month1 + 60; //５年後(147行目で調整により削除）
				} else { //金利下落
					//返済額再計算
					newRepay = recalc(i-1,zan,rate1/100,loanNo);
					if(newRepay < repayment) { //返済額が下がったら
						repayment = newRepay; //返済額更新 
					}
				}
			} else {
				//返済額再計算
				newRepay = recalc(i-1,zan,rate1/100,loanNo);
				repayment = newRepay; //返済額更新
			}
			//次の金利変動を取り出し
			ritu = rate1;
			if (j < cnt1) {
				month1 = change1(bean,j);
				rate1 = change2(bean,j);
				j++;
			} else {
				month1 = 9999;
			}
		}
		//５年後（５年ルールで返済額アップさせる）
		if (i == tugi ) {
			//返済額再計算
			newRepay = recalc(i-1,zan,ritu/100,loanNo);
			if (newRepay > (int)(repayment * 1.25)) { //125%ルール
				newRepay = (int)(repayment * 1.25); 
			}
			tugi += 60; //次の見直しは５年後
			//途中繰り上げ返済で返済額が下がってしまうケースの対応
			if (repayment < newRepay) {		
				repayment = newRepay; //返済額更新（ダウンさせない）
			}
		}
		rate = ritu/100;
		//うち利息の計算
		interest = (int) (Math.round((zan * (rate / 12))));
		//うち元金の計算
		principal = repayment - interest;
		//最終回調整（マイナスにならないように。）
		if (zan < principal) {
			principal = zan;
			repayment = principal + interest;
		}
		if (zan <= 0) { //繰り上げ返済によりすでに残額０（期間短縮型）
			repayment = 0;
			interest = 0;
			principal = 0;
		}else {
			//ローン残高を更新
			zan = zan - principal;
			//総利息加算
			sRisoku += interest;
		}
		//リストに追加
		MonthlyResult result = new MonthlyResult(i,ritu,repayment,principal,interest,zan);
		results.add(result);
	}
	//総額更新
	bean.setSGankin(sGankin);
	bean.setSRisoku(sRisoku);
	bean.setSHensai(sGankin + sRisoku);
	return results;
	}
	//メソッド
	//金利変動
	public int change1(RBean bean,int j ) {
		// interestChangesリストがnullでなく、かつ空でないことを確認
		if (bean.getInterestChanges() != null && !bean.getInterestChanges().isEmpty()) {
			int r = bean.getInterestChanges().get(j).getMonth();
			return r;
		}
		return 9999; // ← 追加：条件を満たさない場合のデフォルト値
	} 
	public double change2(RBean bean,int j ) {
		// interestChangesリストがnullでなく、かつ空でないことを確認
		if (bean.getInterestChanges() != null && !bean.getInterestChanges().isEmpty()) {
			Double r = bean.getInterestChanges().get(j).getRate();
			return r;
		}
		return 0.0; // ← 追加：条件を満たさない場合のデフォルト値
	}
	//繰り上げ返済
	public int earlypay1(RBean bean, int k) {
	    if (bean.getPrepayments() != null && !bean.getPrepayments().isEmpty()) {
	        PrePayment p = bean.getPrepayments().get(k);
	        if (p != null && p.getMonth() != null) {
	            return p.getMonth();
	        }
	    }
	    return 9999; // ← nullでも安全に処理できる
	}
 
	public int earlypay2(RBean bean,int k ) {
		// interestChangesリストがnullでなく、かつ空でないことを確認
		if (bean.getPrepayments() != null && !bean.getPrepayments().isEmpty()) {
			int r = bean.getPrepayments().get(k).getAmount();
			return r;
		}
		return 0; // ← 追加：条件を満たさない場合のデフォルト値
	}
	//返済額計算
	public int recalc(int i,int zan, double rate, int no) {
		if (zan > 0) {
			int hen = (int)(Math.round(zan*(((rate/12)/(Math.pow((1+rate/12),(no - i)) - 1))+rate/12))); //小数点第一位で四捨五入
			return hen;
		}
		return 0;
	}


}
