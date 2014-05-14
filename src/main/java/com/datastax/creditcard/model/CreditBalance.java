package com.datastax.creditcard.model;

import java.util.Date;

public class CreditBalance {

	private String creditCardNo;
	private Date balanceAt;
	private Double amount;

	public CreditBalance(String creditCardNo, Date balanceAt, Double amount) {
		super();
		this.creditCardNo = creditCardNo;
		this.balanceAt = balanceAt;
		this.amount = amount;
	}

	public String getCreditCardNo() {
		return creditCardNo;
	}

	public Date getBalanceAt() {
		return balanceAt;
	}

	public Double getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "CreditBalance [creditCardNo=" + creditCardNo + ", balanceAt=" + balanceAt + ", amount=" + amount + "]";
	}
}
