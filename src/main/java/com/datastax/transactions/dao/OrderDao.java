package com.datastax.transactions.dao;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.transactions.model.Order;

public class OrderDao {

	private static Logger logger = LoggerFactory.getLogger( OrderDao.class );
	
	private Session session;
	private static String keyspaceName = "datastax_transactions_demo";
	private static String tableNameOrder = keyspaceName + ".buyers_orders";
	private static String tableNameProduct = keyspaceName + ".products";

	private static final String INSERT_INTO_ORDER = "Insert into " + tableNameOrder
			+ " (buyerId, orderId, productId) values (?,?,?);";	
	
	
	private static final String INSERT_INTO_PRODUCT = "Insert into " + tableNameProduct
			+ " (productId, capacityleft, orderIds) values (?,?,?);";	
	
	private static final String GET_CAPACITY_PRODUCT = "SELECT capacityLeft from " + tableNameProduct + " WHERE productId = ?";
	
	private PreparedStatement insertStmtOrder;
	private PreparedStatement findCapacity;
	private PreparedStatement insertStmtProduct;

	public OrderDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();
		this.session = cluster.connect();

		this.insertStmtOrder = session.prepare(INSERT_INTO_ORDER);
		this.insertStmtProduct = session.prepare(INSERT_INTO_PRODUCT);
		this.findCapacity = session.prepare(GET_CAPACITY_PRODUCT);
		
		this.insertStmtOrder.setConsistencyLevel(ConsistencyLevel.QUORUM);
		this.insertStmtProduct.setConsistencyLevel(ConsistencyLevel.QUORUM);
		this.findCapacity.setConsistencyLevel(ConsistencyLevel.SERIAL);		
	}

	public void insertOrder(Order order){
		BoundStatement boundStmt = new BoundStatement(this.insertStmtOrder);
				
		this.session.execute(boundStmt.bind(order.getBuyerId(), order.getOrderId(), order.getProductId()));
	}

	public boolean updateProductWithBuyer(Order order, int lastCapacity){
		
		String UPDATE_PRODUCTS = "UPDATE " + tableNameProduct + " SET orderIds=orderIds + {'" + order.getOrderId() + "'},"
				+ " capacityleft = " + (lastCapacity-1) + " WHERE productId = '" + order.getProductId() + "' IF capacityleft = " + lastCapacity;
				
		Statement stmt = new SimpleStatement(UPDATE_PRODUCTS);
		stmt.setConsistencyLevel(ConsistencyLevel.QUORUM);
		
		ResultSet resultSet = this.session.execute(stmt);		
		logger.debug(UPDATE_PRODUCTS);
		
		if (resultSet != null){
			Row row = resultSet.one();
			boolean approved = row.getBool(0);
			
			if (!approved){
				int capacityLeft = row.getInt("capacityLeft");			
				logger.warn("Update failed as capacity left is " + capacityLeft + " not " + lastCapacity + " for product " + order.getProductId() + ". Retrying");			
				return false;				
			}			
		}
		return true;
	}
	
	public int findCapacity(String productId){
		BoundStatement findBoundStmt = new BoundStatement(findCapacity);	
		ResultSet resultSet = this.session.execute(findBoundStmt.bind(productId));		
		return resultSet.one().getInt("capacityLeft");
	}

	public void initializeProductTable(int noOfProducts, int totalStock) {
		BatchStatement batch = new BatchStatement();
		
		for (int i=0; i < noOfProducts; i++){
			
			batch.add(this.insertStmtProduct.bind("P" + i, totalStock, new HashSet<String>()));
		}
		this.session.execute(batch);
		
		logger.info("Inserted " + noOfProducts + " products.");
	}
}
