
package google;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.*;

import javax.net.ssl.HttpsURLConnection;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import javax.json.*;

import mongodb.Config;

import util.UrlSigner;

/**
 * @author Yuan Luo (yuanluo@indiana.edu)
 *
 */
public class GeoCodingClient {
	
	String protocol=null;
	String baseUrl =null;
	String serviceUrl =null;
	String OutputFormat=null;
	String clientKey=null;
	String clientID=null;
	String clientSec=null;
	
	public GeoCodingClient(Config conf) {
		this.protocol=conf.protocol;
		this.baseUrl =conf.baseUrl;
		this.serviceUrl =conf.serviceUrl;		
		this.OutputFormat=conf.OutputFormat;
		this.clientKey=conf.clientKey;
		this.clientID=conf.clientID;
		this.clientSec=conf.clientSec;
	}
	
	/**
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	
	public DBObject getGeoCode(String location) throws InvalidKeyException, NoSuchAlgorithmException {
		DBObject geoCode = null;
		try {
			//reformat location
			//replace all whitespaces with "+";
			if(location==null||location.trim().equals("")){
				BasicDBObjectBuilder geobuilder= new BasicDBObjectBuilder();
				geobuilder.add("geocode", null);
				geoCode=geobuilder.get();
				System.out.println("Empty location. Return..");
				return geoCode;
			}
			
			String reformated_location=location.replaceAll("\\s+","+");
			System.out.println("reformated_location: "+reformated_location);
			//String value = myString;
			URI uri;
			URL myurl;
			if(clientID!=null&&clientSec!=null&&!clientID.equals("")&&!clientSec.equals("")){
				uri = new URI(
						protocol, 
						baseUrl, 
						serviceUrl+this.OutputFormat,
				        "address="+reformated_location+"&client="+clientID,
				        null);
				//Convert uri (may have different encoding) to ascii string so that the proper result can be return from google.
				String signedURL=UrlSigner.sign(uri.toASCIIString(), this.clientSec);
				myurl = new URL(signedURL);
				
			}else if(clientKey!=null&&!clientKey.equals("")){
				uri = new URI(
						protocol, 
						baseUrl, 
						serviceUrl+this.OutputFormat,
				        "address="+reformated_location+"&key="+clientKey,
				        null);
				//Convert uri (may have different encoding) to ascii string so that the proper result can be return from google.
				myurl = new URL(uri.toASCIIString());
			}else {
				uri = new URI(
						protocol, 
						baseUrl, 
						serviceUrl+this.OutputFormat,
				        "address="+reformated_location,
				        null);
				//Convert uri (may have different encoding) to ascii string so that the proper result can be return from google.
				myurl = new URL(uri.toASCIIString());
			}
			
			
			System.out.println("Geocoding URL: "+myurl.toString());
			
			try{
				HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
				InputStream ins = con.getInputStream();
				InputStreamReader isr = new InputStreamReader(ins);
				BufferedReader in = new BufferedReader(isr);
		 
				//String inputLine;
				//String message = new String();
				//while ((inputLine = in.readLine()) != null) {
				//	message=message+inputLine;
				//}
				//DBObject dbObject = (DBObject) JSON.parse(message);
			
				JsonReader jreader = Json.createReader(in);
				JsonObject jobj = jreader.readObject();
				//System.out.println(jobj.toString());
				JsonArray results = jobj.getJsonArray("results");
				JsonValue status = jobj.get("status");
				if(results==null||status==null){
					System.out.println("Geocoding Status: Query error");
				}else if(!results.isEmpty()){
					System.out.println("Geocoding Status: "+status);
					JsonValue result = results.get(0);
					DBObject dbObject = (DBObject) JSON.parse(result.toString());
					String formatedAddress=(String)dbObject.get("formatted_address");
					DBObject geometry = (DBObject)dbObject.get("geometry");
					if(geometry==null){
						System.out.println("GeoCoding Schema Error");
						BasicDBObjectBuilder geobuilder= new BasicDBObjectBuilder();
						geobuilder.add("geocode", null);
						geobuilder.add("status", status.toString());
						geoCode=geobuilder.get();
					}else{
						DBObject geoLocation = (DBObject)geometry.get("location");
						BasicDBObjectBuilder locationBuilder= new BasicDBObjectBuilder();
						locationBuilder.add("formatted_address", formatedAddress);
						locationBuilder.add("location", geoLocation);
					
						BasicDBObjectBuilder geobuilder= new BasicDBObjectBuilder();
						geobuilder.add("geocode", locationBuilder.get());
						geobuilder.add("status", status.toString());
						geoCode=geobuilder.get();
						System.out.println("Extracted geoCode: "+geoCode);
					}
				}else if(status.toString().contains("ZERO_RESULTS")){
					BasicDBObjectBuilder geobuilder= new BasicDBObjectBuilder();
					geobuilder.add("geocode", null);
					geobuilder.add("status", status.toString());
					geoCode=geobuilder.get();
				
				}else {
					BasicDBObjectBuilder geobuilder= new BasicDBObjectBuilder();
					geobuilder.add("status", status.toString());
					geoCode=geobuilder.get();
				}
				in.close();
				return geoCode;
			}catch(Exception e){
				BasicDBObjectBuilder geobuilder= new BasicDBObjectBuilder();
				geobuilder.add("status", "INVALID_REQUEST");
				geoCode=geobuilder.get();
				return geoCode;
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		//Config conf=new Config("/path/to/configuration/file");
		
		//GeoCodingClient gcc= new GeoCodingClient(conf);
		//gcc.getGeoCode("San \n  Diego");
		//$wget https://maps.googleapis.com/maps/api/geocode/json?address=qwertyuiop -O location.json
		//$cat location.json
		//{
		//	"results" : [],
		//	"status" : "ZERO_RESULTS"
		//}
		//gcc.getGeoCode("qwertyuiop");
		
	}

}
