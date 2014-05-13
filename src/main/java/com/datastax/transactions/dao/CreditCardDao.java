package com.datastax.transactions.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.creditcard.model.Transaction;
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
import com.datastax.driver.core.TableMetadata.Order;

public class CreditCardDao {

	private static Logger logger = LoggerFactory.getLogger( CreditCardDao.class );
	
	private Session session;
	
	private DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
	private static String keyspaceName = "datastax_creditcard_demo";
	private static String transactionTable = keyspaceName + ".credit_card_transactions_balance";
	private static String issuerTable = keyspaceName + ".credit_card_transactions_by_issuer_date";
	private static String transactionCounterTable = keyspaceName + ".transaction_date_minute_counter";

	private static final String INSERT_INTO_TRANSACTION = "Insert into " + transactionTable
			+ " (credit_card_no, transaction_time, transaction_id, items, location, issuer, amount) values (?,?,?,?,?,?,?);";	
	
	private static final String INSERT_INTO_ISSUER = "insert into " + issuerTable +  
				"(issuer, date, transaction_id, credit_card_no, transaction_time, items, location, amount) values (?,?,?,?,?,?,?)"; 	
	
	private static final String UPDATE_COUNTER = "update " + transactionCounterTable + " "
			+ "set  total_for_minute = total_for_minute + 1 where date=? AND minute=?";
				
	private static final String UPDATE_BALANCE = "update credit_card_transactions_balance set balance = ?, balance_at = ? "
			+ " where credit_card_no = ?";
	
	private PreparedStatement insertTransactionStmt;
	private PreparedStatement insertIssuerStmt;
	private PreparedStatement updateCounter;
	
	private PreparedStatement updateBalance;
	
	public CreditCardDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();
		this.session = cluster.connect();

		this.insertTransactionStmt = session.prepare(INSERT_INTO_TRANSACTION);
		this.insertIssuerStmt = session.prepare(INSERT_INTO_ISSUER);
		this.updateCounter = session.prepare(UPDATE_COUNTER);
		
		this.updateBalance = session.prepare(UPDATE_BALANCE);
		
		this.insertTransactionStmt.setConsistencyLevel(ConsistencyLevel.QUORUM);
		this.insertIssuerStmt.setConsistencyLevel(ConsistencyLevel.QUORUM);
		this.updateCounter.setConsistencyLevel(ConsistencyLevel.QUORUM);
		
		this.updateBalance.setConsistencyLevel(ConsistencyLevel.ALL);
	}

	public void insertTransaction (Transaction transaction){
		
		DateTime dateTime = new DateTime();
		int minute = dateTime.getMinuteOfDay();
		String date = dateFormatter.format(dateTime.toDate());
				
		BatchStatement batch = new BatchStatement();
		
		batch.add(this.insertTransactionStmt.bind(transaction.getCreditCardNo(), transaction.getTransactionTime(), transaction.getTransactionId(),
				transaction.getItems(), transaction.getLocation(), transaction.getIssuer(), transaction.getAmount()));
		batch.add(this.insertIssuerStmt.bind(transaction.getIssuer(), date, transaction.getTransactionId(), transaction.getCreditCardNo(),
				transaction.getTransactionTime(), transaction.getItems(), transaction.getLocation(), transaction.getAmount()));
		batch.add(this.updateCounter.bind(date, minute));
		
		ResultSet rs = session.execute(batch);
	}

	public boolean updateCreditCardWithBalance(){
		
	}
	
}
