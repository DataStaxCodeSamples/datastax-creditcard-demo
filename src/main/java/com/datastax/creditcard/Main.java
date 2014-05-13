package com.datastax.creditcard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;
import com.datastax.transactions.dao.OrderDao;
import com.datastax.transactions.model.Order;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public Main() {

		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		String noOfThreadsStr = PropertyHelper.getProperty("noOfThreads", "5");
		String noOfOrdersStr = PropertyHelper.getProperty("noOfOrders", "10010");
		String noOfProductsStr = PropertyHelper.getProperty("noOfProducts", "10");
		String inStockStr =  PropertyHelper.getProperty("inStock", "1000");
		
		logger.info("Starting with " + noOfThreads + " threads, " + noOfOrders  + " orders, " + NO_OF_PRODUCTS 
				+ " products and " + totalStock + " quantity in stock.");
		
		OrderDao dao = new OrderDao(contactPointsStr.split(","));
				
		//Are we loading only or loading and running order processor
		if (System.getProperty("runBalanceUpdate") != null){
			dao.initializeProductTable(NO_OF_PRODUCTS, totalStock);
			return;
		}else if(System.getProperty("load") != null){
			dao.initializeProductTable(NO_OF_PRODUCTS, totalStock);
		}
		
		// Create shared queue
		BlockingQueue<Order> queueOrders = new ArrayBlockingQueue<Order>(1000);

		ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
		Timer timer = new Timer();
		timer.start();

		for (int i = 0; i < noOfThreads; i++) {
			executor.execute(new OrderWriter(dao, queueOrders));
		}

		startProcessingOrders(queueOrders, noOfOrders);

		while (!queueOrders.isEmpty()) {
			logger.info("Messages left to send " + queueOrders.size());
			sleep(1000);
		}
		timer.end();
		logger.info("Transactions demo took " + timer.getTimeTakenSeconds() + " secs. Total Products sold : " + TOTAL_PRODUCTS_SOLD.get());		
	}

	private void startProcessingOrders(BlockingQueue<Order> queueOrders, int noOfOrders) {

		for (int i = 0; i < noOfOrders; i++) {
			String randomOrderId = UUID.randomUUID().toString();
			int randomBuyerId = (int) (Math.random() * NO_OF_BUYERS);

			int randomProductId = (int) (Math.random() * NO_OF_PRODUCTS);
			String productId = "P" + randomProductId;

			while (this.outOfStock.contains(randomProductId)) {
				randomProductId = (int) (Math.random() * NO_OF_PRODUCTS);
				productId = "P" + randomProductId;
			}

			Order order = new Order(randomOrderId, productId, "B" + randomBuyerId);
			try {
				queueOrders.put(order);
			} catch (InterruptedException e) {
				e.printStackTrace();		
			}
			
			sleep(1);
		}
	}

	class OrderWriter implements Runnable {

		private OrderDao dao;
		private BlockingQueue<Order> queue;

		public OrderWriter(OrderDao dao, BlockingQueue<Order> queue) {
			this.dao = dao;
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				Order order = queue.poll();
				
				if (order != null) {
					
					logger.debug("Got order : " + order.toString());

					boolean succeeded = false;
					while (!succeeded) {

						int productsLeft = dao.findCapacity(order.getProductId());
						if (productsLeft > 0) {
							
							succeeded = this.dao.updateProductWithBuyer(order, productsLeft);							
							if (succeeded){
								this.dao.insertOrder(order);
								TOTAL_PRODUCTS_SOLD.incrementAndGet();
							}							
						}else{
							logger.info("OUT OF STOCK - Cannot process order for product Id " + order.getProductId());
							outOfStock.add(order.getProductId());
							break;
						}
					}
				}
			}
		}
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
		
		System.exit(0);
	}
}
