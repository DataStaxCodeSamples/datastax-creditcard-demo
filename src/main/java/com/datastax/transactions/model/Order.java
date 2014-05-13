package com.datastax.transactions.model;

public class Order {

	private String orderId;
	private String productId;
	private String buyerId;
	public Order(String orderId, String productId, String buyerId) {
		super();
		this.orderId = orderId;
		this.productId = productId;
		this.buyerId = buyerId;
	}
	public String getOrderId() {
		return orderId;
	}
	public String getProductId() {
		return productId;
	}
	public String getBuyerId() {
		return buyerId;
	}
	
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", productId=" + productId + ", buyerId=" + buyerId + "]";
	}	
}
