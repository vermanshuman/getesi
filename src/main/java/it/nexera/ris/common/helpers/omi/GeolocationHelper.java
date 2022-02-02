package it.nexera.ris.common.helpers.omi;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeolocationHelper extends BaseHelper {

    public static List<Pair<Double, Double>> checkCoordinates(String address) throws Exception {
        return GeocodeHelper.getCoordinates(address);
    }
    
    public static List<String> findZoneByAddress(String address, byte[] kmlFileContent
            ,List<Pair<Double, Double>> coordinates) throws Exception {
        List<SimpleFeature> placemarks = parseKMLFileAndGetPlacemarks(kmlFileContent);
        return find(coordinates, placemarks);
    }
    
    public static List<String> findZoneByAddress(String address, byte[] kmlFileContent) throws Exception {
        List<Pair<Double, Double>> coordinates = GeocodeHelper.getCoordinates(address);
        List<SimpleFeature> placemarks = parseKMLFileAndGetPlacemarks(kmlFileContent);
        return find(coordinates, placemarks);
    }

    public static List<String> findZoneByAddress(String address, File kmlFile) throws Exception {
        List<Pair<Double, Double>> coordinates = GeocodeHelper.getCoordinates(address);
        List<SimpleFeature> placemarks = parseKMLFileAndGetPlacemarks(kmlFile);
        return find(coordinates, placemarks);
    }
    
    public static List<String> findZoneByCoordinates(List<Pair<Double, Double>> coordinates, File kmlFile) throws Exception {
        List<SimpleFeature> placemarks = parseKMLFileAndGetPlacemarks(kmlFile);
        return find(coordinates, placemarks);
    }

    private static List<String> find(List<Pair<Double, Double>> coordinates, List<SimpleFeature> placemarks) {
        if (ValidationHelper.isNullOrEmpty(coordinates) || ValidationHelper.isNullOrEmpty(placemarks)) {
            return null;
        }

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        List<Point> pointList = coordinates.stream().map(c -> new Coordinate(c.getLeft(), c.getRight()))
                .map(geometryFactory::createPoint).collect(Collectors.toList());

        List<String> result = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(placemarks)) {
            for (SimpleFeature placemark : placemarks) {
                for (int i = placemark.getAttributes().size() - 1; i >= 0; i--) {
                    if (placemark.getAttribute(i) instanceof MultiPolygon) {
                        MultiPolygon multiPolygon = (MultiPolygon) placemark.getAttribute(i);
                        for (int boundary = 0; boundary < multiPolygon.getNumGeometries(); boundary++) {
                            Polygon polygon = (Polygon) multiPolygon.getGeometryN(boundary);
                            if (polygonContainsSomePoint(pointList, polygon)){
                                String zone = (String) placemark.getAttribute(0);
                                result.add(zone.substring(zone.lastIndexOf(" ") + 1));
                            }
                        }
                        break;
                    } else if (placemark.getAttribute(i) instanceof Polygon) {
                        Polygon polygon = (Polygon) placemark.getAttribute(i);
                        if (polygonContainsSomePoint(pointList, polygon)){
                            String zone = (String) placemark.getAttribute(0);
                            result.add(zone.substring(zone.lastIndexOf(" ") + 1));
                        }
                        break;
                    }
                }
            }
        }

        return result;
    }

    private static boolean polygonContainsSomePoint(List<Point> pointList, Polygon polygon) {
        for (Point point : pointList) {
            if (polygon.contains(point)) {
                return true;
            }
        }
        return false;
    }

    private static List<SimpleFeature> parseKMLFileAndGetPlacemarks(InputStream inputStream)
            throws ParserConfigurationException, SAXException, IOException {
        List<SimpleFeature> placemarks = null;
        Parser parser = new Parser(new KMLConfiguration());
        SimpleFeature f = (SimpleFeature) parser.parse(inputStream);
        placemarks = (List<SimpleFeature>) f.getAttribute("Feature");
        return placemarks;
    }


    private static List<SimpleFeature> parseKMLFileAndGetPlacemarks(File kmlFile)
            throws IOException, SAXException, ParserConfigurationException {
        return parseKMLFileAndGetPlacemarks(new FileInputStream(kmlFile));
    }

    private static List<SimpleFeature> parseKMLFileAndGetPlacemarks(byte[] kmlData)
            throws IOException, SAXException, ParserConfigurationException {
        return parseKMLFileAndGetPlacemarks(new ByteArrayInputStream(kmlData));
    }

}
