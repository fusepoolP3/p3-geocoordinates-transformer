package eu.fusepool.p3.geo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

public class RdfCoordinatesConverter {  
	
	
	/**
     * Returns a list of location URIs with their UTM coordinates. A location URI is in the domain of property
     * http://www.opengis.net/ont/geosparql#asWKT
     * vertice of a polygon. 
     * @param model
     * @return
     */
    protected HashMap<String,String> getPointList(Model model) {
        Property gs_asWKT = model.createProperty("http://www.opengis.net/ont/geosparql#asWKT");      
        HashMap<String,String> pointMap = new HashMap<String, String>();
        Resource subject = null;
        RDFNode object = null;
        SimpleSelector selector = new SimpleSelector(subject, gs_asWKT, object);
        for (StmtIterator i = model.listStatements( selector ); i.hasNext(); ) {
            Statement stmt = i.nextStatement();
            String pointString = stmt.getObject().toString();
            int start = pointString.indexOf( "(" );
            int end = pointString.indexOf( ")" );
            String coordString = pointString.substring(start + 1, end);            
            String [] coordArray = coordString.split(" ");
            String x = coordArray[0];
            String y = coordArray[1];
            String subjUri = stmt.getSubject().getURI();            
            pointMap.put(subjUri, "32 T " + x + " " + y);            
        }
        return pointMap;
    }
	
	/**
	 * Transforms the coordinates of locations from UTM to WGS84
	 * @param utmPointList
	 * @return
	 */
	protected HashMap<String,WGS84Point> convertToWGS84(HashMap<String,String> utmPointMap){
		HashMap<String,WGS84Point> wgs84pointMap = null;
		CoordinateConversion converter = new CoordinateConversion();
		if(utmPointMap != null) {
			wgs84pointMap = new HashMap<String,WGS84Point>();	
			Set<String> subjectUriSet = utmPointMap.keySet();
			Iterator<String> iutmPoint = subjectUriSet.iterator();
			while(iutmPoint.hasNext()) {	
				String subjectUri = iutmPoint.next();
				String utm = utmPointMap.get(subjectUri);
				double latlng [] = converter.utm2LatLon( utm );
				WGS84Point wgs84 = new WGS84Point();
				wgs84.setLat(latlng[0]);
				wgs84.setLong(latlng[1]);
				wgs84pointMap.put(subjectUri, wgs84);						
			}
		}
		return wgs84pointMap;
	}
	
	/**
	 * Enrich the original model with geo:lat and geo:long properties. 
	 * @param model
	 * @param wgs84Map
	 */
	protected Model enrichModel(Model model, HashMap<String,WGS84Point> wgs84Map) {
		if(wgs84Map != null) {
			Property wgs84_lat = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
			Property wgs84_long = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
			Set<String> subjectUriSet = wgs84Map.keySet();
			Iterator<String> wgs84PointIter = subjectUriSet.iterator();
			while(wgs84PointIter.hasNext()) {	
				String subjectUri = wgs84PointIter.next();
				double latitude = wgs84Map.get(subjectUri).getLat();
				double longitude = wgs84Map.get(subjectUri).getLong();
				model.getResource(subjectUri).addProperty(wgs84_lat, String.valueOf(latitude));
				model.getResource(subjectUri).addProperty(wgs84_long, String.valueOf(longitude));								
			}
		}
		
		return model;
		
	}

}
