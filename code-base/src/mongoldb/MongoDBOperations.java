
/*
#
# Copyright 2014 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: I590-TwitterDataSet
# File:  MongoDBOperations.java
# Description:  Main class for query and update Twitter users dataset
#
# -----------------------------------------------------------------
# 
*/

package mongodb;

import com.mongodb.*;
import com.mongodb.util.JSON;

import google.GeoCodingClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import javax.json.stream.JsonParser;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Yuan Luo (yuanluo@indiana.edu)
 *
 */
public class MongoDBOperations {

	MongoClient mongoClient;
	Config conf;
	
	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	
	public MongoDBOperations(String ConfigPath) throws UnknownHostException{
		this.conf=new Config(ConfigPath);
		mongoClient = new MongoClient( this.conf.MongodbHost, this.conf.MongodbPort );
	}
	
	public List<String> listDB(){
		List<String> dblist=mongoClient.getDatabaseNames();
		return dblist;
	}
	
	public DB getDB(String dbname){
		return mongoClient.getDB(dbname);
	}
	
	public DBCollection getCollection(String dbname, String collname){
		return this.mongoClient.getDB(dbname).getCollection(collname);
	}
	
	public 	Set<String> getCollectionNames(DB db){
		Set<String> colls = db.getCollectionNames();
		return colls;
	}

	public void printOne(DBCollection coll){
		DBObject myDoc = coll.findOne();
		//System.out.println(myDoc);
	}
	
	public void printQueryResult(DBCollection coll, BasicDBObject queryObject){
		DBCursor cursor = coll.find(queryObject);
		int count=0;
		try {
		   while(cursor.hasNext()) {
			   Object loc=cursor.next().get("user_location");
			   if(loc!=null){
				   String user_location=loc.toString();
				   System.out.println(user_location);
			   }
		       count++;
		   }
		} finally {
		   cursor.close();
		}
		System.out.println(count + " records found.");
	}
	
	public void geoFindAndUpdate(DBCollection coll, BasicDBObject queryObject) {
		int query_count=0;
		int processed_count=0;
		int updated_count=0;
		DBCursor cursor = coll.find(queryObject);
		try {
			query_count=cursor.count();
			while(cursor.hasNext()) {
			   BasicDBObject myDoc = (BasicDBObject) cursor.next();
			   Object userLocationObj=myDoc.get("user_location");
			   String location = String.valueOf(userLocationObj);
			   
			   ObjectId ID= (ObjectId) myDoc.get("_id");
			   System.out.println("Queried Document: "+myDoc);
				
			   GeoCodingClient gcc= new GeoCodingClient(this.conf);
			   Thread.sleep(200);
			   DBObject update = gcc.getGeoCode(location);
			   //System.out.println(update);
			   
			   if(update==null){ 
				   System.out.print("No updates (update==null)");
				   break;
			   }else {
				   Set<String> keyset=update.keySet();
				   if(keyset==null){
					   System.out.print("No updates (keyset==null)");
				   }else if(keyset.contains("geocode")) {
					   updated_count++;
					   coll.update(new BasicDBObject().append("_id", ID), myDoc.append("geocode", update.get("geocode")));
					   System.out.println("Updated Doc:\n"+coll.find(new BasicDBObject().append("_id", ID)).next());
					   System.out.println("In this run: ("+processed_count + " records processed, "+updated_count + " records updated.)");
				   }else if(keyset.contains("status")&&update.get("status").toString().contains("OVER_QUERY_LIMIT")){
					   break;
				   }			   
			   }
			   processed_count++;
		   }
	   } catch (InterruptedException e) {
		   e.printStackTrace();
	   } catch (InvalidKeyException e) {
		e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
	} finally {
		   cursor.close();
		   System.out.println("In this run:");
                   System.out.println("Total:"+query_count + " record(s) found.");		
		   System.out.println("Total:"+processed_count + " record(s) processed.");		
		   System.out.println("Total:"+updated_count + " record(s) updated.");		
	   }
	   
	}
	
	public String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
	
	public static void main(String[] args) {
		if(args.length!=4){
			System.out.println("FindAndUpdate:\nargs[0]: Configuration File.\nargs[1]: DB Name.\nargs[2]: Collection Name.\nargs[3]: Query in JSON format.");
			System.out.println("Please check the parameters.");
			return;
		}
		String ConfigPath=args[0];
		String DBName=args[1];
		String CollectionName=args[2];
		String QueryFile=args[3];
		MongoDBOperations mdbops;
		
		try {
			mdbops = new MongoDBOperations(ConfigPath);
			
			mdbops.listDB();
			DBCollection coll=mdbops.getCollection(DBName,CollectionName);
			String queryString=mdbops.readFile(QueryFile);
			System.out.println(queryString);
			BasicDBObject queryObject = (BasicDBObject) JSON.parse(queryString);
			mdbops.geoFindAndUpdate(coll, queryObject);
			//mdbops.printQueryResult(coll, queryObject);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
