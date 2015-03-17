package eu.fusepool.p3.geo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        TripleCollection resultGraph = null;
        String mediaType = entity.getType().toString();   
        Parser parser = Parser.getInstance();
        InputStream is = entity.getData();
        
        
            
        return resultGraph;
        
    }
  
    @Override
    public boolean isLongRunning() {
        // downloading the dataset can be time consuming
        return false;
    }

}
