package eu.fusepool.p3.geo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.RdfGeneratingTransformer;

public class Utm2Wgs84Transformer extends RdfGeneratingTransformer {
    
    final RdfCoordinatesConverter converter;
    
    private static final Logger log = LoggerFactory.getLogger(Utm2Wgs84Transformer.class);
    
    Utm2Wgs84Transformer() {
        this.converter = new RdfCoordinatesConverter(); 
    }
    
    /**
     * Set of client data formats supported.
     */
    @Override
    public Set<MimeType> getSupportedInputFormats() {
        Parser parser = Parser.getInstance();
        try {
            Set<MimeType> mimeSet = new HashSet<MimeType>();
            for (String mediaFormat : parser.getSupportedFormats()) {           
              mimeSet.add(new MimeType(mediaFormat));
            }
            return Collections.unmodifiableSet(mimeSet);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Set of transformer output data formats supported.
     */
    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        try {
          Set<MimeType> mimeSet = new HashSet<MimeType>();  
          mimeSet.add(new MimeType("text/turtle"));
          return Collections.unmodifiableSet(mimeSet);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
        SimpleMGraph enrichedGraph = new SimpleMGraph();
        String mediaType = entity.getType().toString();   
        InputStream is = entity.getData();
        Parser parser = Parser.getInstance();
        TripleCollection inputGraph = parser.parse(is, SupportedFormat.TURTLE);
        enrichedGraph.addAll(inputGraph);
        enrichedGraph.addAll(addWgs84Coordinates(is));            
        return enrichedGraph;
        
    }
    /**
     * Adds WGS84 coordinates to subjects of gs:asWKT property
     * @param is
     * @return
     */
    private TripleCollection addWgs84Coordinates(InputStream is) {
        TripleCollection resultGraph = null;
        Parser parser = Parser.getInstance();
        Model model = ModelFactory.createDefaultModel();
        // Reads the input rdf data into a Jena model 
        model.read(is, null, "TURTLE");
        // Gets a list of subjects of geo:asWKT property
        HashMap<String,String> pointList = converter.getPointList(model);
        // Converts the coordinates from UTM to WGS84 
        HashMap<String,WGS84Point> wgs84Map = converter.convertToWGS84(pointList);
        // Adds the wgs84:lat wgs84:long properties to the subjects
        Model enrichedModel = converter.enrichModel(model, wgs84Map);
        // Copy the rdf data from a Jena model to a Clerezza graph
        resultGraph = jenaModel2ClerezzaTc(enrichedModel);
        return resultGraph;
    }
    /**
     * Copies a Jena model in a Clerezza triple collection
     * @param model
     * @return
     */
    private TripleCollection jenaModel2ClerezzaTc(Model model) {
        TripleCollection resultGraph = null;
        Parser parser = Parser.getInstance();
        ByteArrayOutputStream osdata = new ByteArrayOutputStream();
        model.write(osdata, "TURTLE");
        byte[] dataOut = osdata.toByteArray();
        ByteArrayInputStream isdata = new ByteArrayInputStream(dataOut);
        resultGraph = parser.parse(isdata, SupportedFormat.TURTLE);
        return resultGraph;
    }
  
    @Override
    public boolean isLongRunning() {
        // downloading the dataset can be time consuming
        return false;
    }

}
