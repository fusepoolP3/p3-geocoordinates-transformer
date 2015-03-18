package eu.fusepool.p3.geo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class RdfCoordinatesConverterTest {
    
    Model model;    
    RdfCoordinatesConverter converter;

    @Before
    public void setUp() throws Exception {
        //File rdfFile = new File("trentino-architectural-heritage.ttl");        
        InputStream is = getClass().getResourceAsStream("trentino-architectural-heritage.ttl");
        model = ModelFactory.createDefaultModel();
        model.read(is, null, "TURTLE");
        converter = new RdfCoordinatesConverter();        
    } 

    @Test
    public void testGetPointList() {
        HashMap<String,String> pointList = converter.getPointList(model);
        Iterator<String> pointIter = pointList.keySet().iterator();
        Assert.assertTrue(pointIter.hasNext());
        
    }
    
    @Test
    public void testConvertToWGS84() {
        HashMap<String,String> pointList = converter.getPointList(model);
        HashMap<String,WGS84Point> wgs84Map = converter.convertToWGS84(pointList);
        Iterator<String> pointIter = wgs84Map.keySet().iterator();
        Assert.assertTrue(pointIter.hasNext());
        
    }
    
    @Test
    public void testEnrichModel() {
        HashMap<String,String> pointList = converter.getPointList(model);
        HashMap<String,WGS84Point> wgs84Map = converter.convertToWGS84(pointList);
        Model enrichedModel = converter.enrichModel(model, wgs84Map);
        Property wgs84_lat = enrichedModel.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
        Iterator<String> pointIter = wgs84Map.keySet().iterator();
        Resource subject = enrichedModel.createResource(pointIter.next());
        RDFNode object = null;
        SimpleSelector selector = new SimpleSelector(subject, wgs84_lat, object);  
        
        Assert.assertTrue(model.listStatements( selector ).hasNext());
    }
    
    private void printModel(Model model) {
        model.write(System.out);
    }
    

}
