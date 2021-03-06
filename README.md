Geocoordinates Transformer [![Build Status](https://travis-ci.org/fusepoolP3/p3-geocoordinates-transformer.svg)](https://travis-ci.org/fusepoolP3/p3-geocoordinates-transformer)
==========================

A geocoordinates transformer from UTM (x, y) to WGS84 (lat, long) for POINT geometries.

## Try it out

First, obtain the latest [release](https://github.com/fusepoolP3/p3-geocoordinates-transformer/releases/latest).

Next, start the transformer:

    java -jar p3-geocoordinates-transformer-v*-jar-with-dependencies.jar

To obtain the supported input/output-formats of the transformer, query it with the curl-utility:

    curl http://localhost:7100

For advanced testing of the transformer, refer to the section "Usage" just below.

## Compiling and Running
Compile the application running the command

    mvn install

Start the application using the command

    mvn exec:java

## Usage
The transformer adds wgs84:lat and wgs84:long properties to the subjects of gs:asWKT property
where wgs84 is the prefix of the namespace http://www.w3.org/2003/01/geo/wgs84_pos# and gs is the prefix for http://www.opengis.net/ont/geosparql#

As an example we consider the position of an architectural heritage in UTM coordinates serialized as WKT (Well-Known-Text)

    @prefix schema: <http://schema.org/> .
    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix gs: <http://www.opengis.net/ont/geosparql#> .
    @prefix wgs84: <http://www.w3.org/2003/01/geo/wgs84_pos#> .

    <http://fusepool.eu/res/trentino/39.0009> a schema:TouristAttraction ;
                                           rdfs:label "Chiesa della Madonna della Neve" ;
                                           gs:asWKT "POINT (712183.260172061738558 5150927.968433328904212)" .


The data file "trentino-architectural-heritage.ttl" is sent to the transformer

    curl -i -X POST -H "ContentType: text/turtle" -d @trentino-architectural-heritage.ttl http://localhost:7100

After the coordinates transformation from UTM to WGS84 two properties for the latitude and longitude are added to the subject of gs:asWKT property

    @prefix schema: <http://schema.org/> .
    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix gs: <http://www.opengis.net/ont/geosparql#> .
    @prefix wgs84: <http://www.w3.org/2003/01/geo/wgs84_pos#> .

    <http://fusepool.eu/res/trentino/39.0009> a schema:TouristAttraction ;
                                           rdfs:label "Chiesa della Madonna della Neve" ;
                                           gs:asWKT "POINT (712183.260172061738558 5150927.968433328904212)" ;
                                           wgs84:lat "46.47845602881098" ;
                                           wgs84:long "11.764220393196" .
