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
# File:  Config.java
# Description:  Utility class for configure application parameters
#
# -----------------------------------------------------------------
# 
*/

package mongodb;

import util.PropertyReader;
/**
 * @author Yuan Luo (yuanluo@indiana.edu)
 */
public class Config {
	public String MongodbHost=null;
	public int	MongodbPort; 
	
	public String protocol=null;
	public String baseUrl =null;
	public String serviceUrl =null;
	
	public String OutputFormat =null;
	public String clientKey=null;
	public String clientID=null;
	public String clientSec=null;
	private PropertyReader property = null;
	
	public Config(String propertiesPath){
		this.property = PropertyReader.getInstance(propertiesPath);
		this.MongodbHost=this.property.getProperty("mongodb.hostname")==null||this.property.getProperty("mongodb.hostname").trim().equals("")? "localhost": this.property.getProperty("mongodb.hostname");
		this.MongodbPort=Integer.parseInt(this.property.getProperty("mongodb.port")==null||this.property.getProperty("mongodb.port").trim().equals("")? "27017": this.property.getProperty("mongodb.port"));
		
		this.protocol=this.property.getProperty("geocode.protocol")==null||this.property.getProperty("geocode.protocol").trim().equals("")? "https": this.property.getProperty("geocode.protocol");
		this.baseUrl=this.property.getProperty("geocode.baseUrl")==null||this.property.getProperty("geocode.baseUrl").trim().equals("")? "maps.googleapis.com": this.property.getProperty("geocode.baseUrl");
		this.serviceUrl=this.property.getProperty("geocode.serviceUrl")==null||this.property.getProperty("geocode.serviceUrl").trim().equals("")? "/maps/api/geocode/": this.property.getProperty("geocode.serviceUrl");
		
		
		this.OutputFormat=this.property.getProperty("geocode.outputformat")==null||this.property.getProperty("geocode.outputformat").trim().equals("")? "json": this.property.getProperty("geocode.outputformat");
		this.clientKey=this.property.getProperty("geocode.clientKey")==null? "": this.property.getProperty("geocode.clientKey");
		this.clientID=this.property.getProperty("geocode.clientID")==null? "": this.property.getProperty("geocode.clientID");
		this.clientSec=this.property.getProperty("geocode.clientSec")==null? "": this.property.getProperty("geocode.clientSec");
	};
	

}
