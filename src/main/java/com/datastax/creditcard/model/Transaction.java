package com.datastax.creditcard.model;

import java.util.Date;
import java.util.Map;

public class Transaction {

	private String creditCardNo;
	private Date transactionTime;
	private String transactionId;
	private Map<String, Double> items;
	private String location;
	private String issuer;
	private Double amount;

	public Transaction() {
		super();
	}

	public String getCreditCardNo() {
		return creditCardNo;
	}

	public void setCreditCardNo(String creditCardNo) {
		this.creditCardNo = creditCardNo;
	}

	public Date getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Map<String, Double> getItems() {
		return items;
	}

	public void setItems(Map<String, Double> items) {
		this.items = items;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "Transaction [creditCardNo=" + creditCardNo + ", transactionTime=" + transactionTime
				+ ", transactionId=" + transactionId + ", items=" + items + ", location=" + location + ", issuer="
				+ issuer + ", amount=" + amount + "]";
	}	
}
