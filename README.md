Transactions Example
====================

NOTE - this example requires apache cassandra version > 2.0 and the cassandra-driver-core version > 2.0.0

## Scenario



## Schema Setup
Note : This will drop the keyspace "datastax_creditcard_demo" and create a new one. All existing data will be lost. 

To specify contact points use the contactPoints command line parameter e.g. '-DcontactPoints=192.168.25.100,192.168.25.101'
The contact points can take mulitple points in the IP,IP,IP (no spaces).

To create the a single node cluster with replication factor of 1 for standard localhost setup, run the following

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup"

To start the credit card processor, run

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.creditcard.Main" 
    
An example of running this with 10 threads and some custom contact points would be 

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.creditcard.Main" -Dload=true -DcontactPoints=cassandra1 
	
Other properties that can be added are noOfTransactions, noOfCreditCards

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.creditcard.Main"  -DnoOfTransactions=1000000 -DnoOfCreditCards=10000
	
To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
    

