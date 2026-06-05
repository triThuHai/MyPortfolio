package com.o3.server;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.net.ssl.SSLParameters;
import java.util.*;
import java.util.concurrent.Executors;
import java.sql.SQLException;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//myPass123 
//Generating 2048-bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 90 days
//for: CN=Tri Ho, OU=Stu, O=Univerity of Oulu, L=Oulu, ST=Pohjois-Pohjanmaa, C=FI


public class Server implements HttpHandler {
    
    List<ObservationRecord> recordsFromDatabase = Collections.synchronizedList(new ArrayList<>());
    private MessageDatabase myDb = new MessageDatabase();

    public Server() {
        String dbPath = System.getenv("DATABASE_PATH");
        if (dbPath == null || dbPath.trim().isEmpty()) {
            dbPath = "messages.db";
        }
        try {
            myDb.open(dbPath);
            this.recordsFromDatabase.addAll(myDb.readMessage());
        } catch (SQLException e) {
            System.err.println("Error opening database: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        System.out.println("Request handled in thread " + Thread.currentThread().threadId()); 
        if ("POST".equals(t.getRequestMethod())) {
            handlePost(t);
        } else if ("GET".equals(t.getRequestMethod())) {
            handleGet(t);
        } else {
            sendJsonRespone(t, 405, "Method not supported");
        }    
    }

    private void handlePost(HttpExchange httpEx) throws IOException {
        try {
            String contentType = httpEx.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                sendJsonRespone(httpEx, 400, "Content-Type must be application/json");
                return;
            }

            InputStream requestBody = httpEx.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader
            (requestBody, StandardCharsets.UTF_8));
            String requestBodyText = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            if (requestBodyText == null || requestBodyText.trim().isEmpty()) {
                sendJsonRespone(httpEx, 400, "Request body is empty. Expected JSON array.");
                return;
            }
            JSONObject receivedObject;
            try {
                receivedObject = new JSONObject(requestBodyText);

                if (receivedObject == null || receivedObject.isEmpty()) {
                    sendJsonRespone(httpEx, 400, "JSON object is empty");
                    return;
                }

                if (!receivedObjectValidation(receivedObject, httpEx)) {
                    return;
                }
            } catch (JSONException e) {
                sendJsonRespone(httpEx, 400, "Invalid JSON format: " + e.getMessage());
                return;
            }

            ObservationRecord record = new ObservationRecord();
            record.recordedFromJson(receivedObject);
            System.out.println("Received JSON object: " + receivedObject.toString(2));

            record.setRecordTimeReceivedNowUtc();
            record.setId(UUID.randomUUID().toString());

            String recordOwner = "DummyOwner";
            if (httpEx.getPrincipal() != null) {
                String username = httpEx.getPrincipal().getUsername();
                String nickname = myDb.getNickname(username);
                if (nickname != null && !nickname.isEmpty()) {
                        recordOwner = nickname; 
                    } else {
                        recordOwner = username; 
                    }
            }
            record.setRecordOwner(recordOwner);

            JSONArray observatoryArray = record.getObservatory();
            if (observatoryArray != null) {
                for (int i = 0; i < observatoryArray.length(); i++) {
                    JSONObject obervatoryObject = observatoryArray.getJSONObject(i);
                    if (obervatoryObject.has("latitude") && obervatoryObject.has("longitude")) {
                        JSONObject weather = getWeatherInfo(obervatoryObject.getDouble("latitude"), obervatoryObject.getDouble("longitude"));
                        if (weather != null && !weather.isEmpty()) {
                            obervatoryObject.put("weather", "[]");
                        }
                    }
                }
            }

            recordsFromDatabase.add(record);
            myDb.insertObservation(record);
            System.out.println("Record added to database and list.");
            sendJsonRespone(httpEx, 200, "Successfull"); 
        } catch (JSONException e) {
            sendJsonRespone(httpEx, 400, "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            byte[] bytes = ("Server error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
            OutputStream os = httpEx.getResponseBody();
            httpEx.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            httpEx.sendResponseHeaders(500, bytes.length);  
            os.write(bytes);
            os.flush();
            os.close();          
        } finally {
            httpEx.close();
        }
    }

    private void handleGet(HttpExchange httpEx) throws IOException {
        try {
            String responseString;
            JSONArray responseJsonArray = new JSONArray();
            for (ObservationRecord records : myDb.readMessage()) {
                responseJsonArray.put(records.toJson());
            }

            if (responseJsonArray.isEmpty()) {
                responseString = "[]";
                //sendJsonRespone(httpEx, 404, "No data found");
                return;
            } else {
                responseString = responseJsonArray.toString(2);
            }

            byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
            httpEx.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            httpEx.sendResponseHeaders(200, bytes.length);
            OutputStream os = httpEx.getResponseBody();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            sendJsonRespone(httpEx, 500, "Server error: " + e.getMessage());
        } finally {
            httpEx.close();
        }
    }

    private boolean receivedObjectValidation(JSONObject object, HttpExchange t) throws IOException {
        boolean hasOrbital = object.has("orbital_elements") && !object.isNull("orbital_elements");
        boolean hasState = object.has("state_vector") && !object.isNull("state_vector");
        
        if (!hasOrbital && !hasState) {
            sendJsonRespone(t, 400, "JSON object must contain 'orbital_elements' or 'state_vector' field.");
            return false;
        }

        if (hasOrbital) {
            JSONObject orbitalElements = object.optJSONObject("orbital_elements");
            if (!orbitalElementValidation(orbitalElements)) {
                sendJsonRespone(t, 400, "Invalid orbital elements in JSON object.");
            return false;
            }
        }

        if (hasState) {
            JSONObject stateVector = object.optJSONObject("state_vector");
            if (!stateVectorValidation(stateVector)) {
                sendJsonRespone(t, 400, "Invalid state vector in JSON object.");
                return false;
            }
        }

        JSONObject metaData = object.optJSONObject("metadata");
        if (metaData != null && metaData.has("observatory")) {
            JSONArray observatoryArray = metaData.optJSONArray("observatory");
            if (!observatoryValidation(observatoryArray)) {
                sendJsonRespone(t, 400, "Invalid observatory in JSON object.");
            return false;
        }
    }
        return true;
    }

    private boolean orbitalElementValidation(JSONObject orbitalElements) throws IOException {
        if (orbitalElements.isEmpty()) {
            return false;
        }

        String[] requiredFields = {
            "semi_major_axis_au",
            "eccentricity",
            "inclination_deg",
            "longitude_ascending_node_deg",
            "argument_of_periapsis_deg",
            "mean_anomaly_deg"
        };

        for (String key : requiredFields) {
            if (!orbitalElements.has(key) 
                || orbitalElements.isNull(key) 
                || orbitalElements.get(key).toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean stateVectorValidation(JSONObject stateVector) throws IOException {
        if (stateVector.isEmpty()) {
            return false;
        }

        String[] requiredFields = {
            "position_au",
            "velocity_au_per_day"
        };

        for (String key : requiredFields) {
            if (!stateVector.has(key) 
                || stateVector.isNull(key) 
                || stateVector.get(key).toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean observatoryValidation(JSONArray observatory) {
        if (observatory == null) return true;

        try {
            for (int i = 0; i < observatory.length(); i++) {
                JSONObject obs = observatory.getJSONObject(i);

                if (!obs.has("latitude") || !obs.has("longitude") || !obs.has("observatory_name")) {
                    return false;
                }

                if (!(obs.get("latitude") instanceof Number)) {
                    return false;
                }

                if (!(obs.get("longitude") instanceof Number)) {
                    return false;
                }

                // No need if do not exist 
                //if (!obs.has("weather") || obs.isNull("weather")) {
                    //obs.put("weather", new JSONObject());
                //}
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private JSONObject getWeatherInfo(double latitude, double longitude) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            URI uri = new URI("http://127.0.0.1:4001/wfs?latlon="+latitude+","+longitude);
            URL url = uri.toURL();
            InputStream inputStream = url.openStream();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("wfs:member");
            JSONObject weatherInfo = new JSONObject();
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Element bsWfsElement = (Element) element.getElementsByTagName("BsWfs:BsWfsElement").item(0);
                    String parameterName = bsWfsElement.getElementsByTagName("BsWfs:ParameterName").item(0).getTextContent();
                    String parameterValue = bsWfsElement.getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent();
                    
                    if (parameterName.equals("temperatureInKelvins")) weatherInfo.put("temperature_in_kelvins", parameterValue);
                    else if (parameterName.equals("cloudinessPercentance")) weatherInfo.put("cloudiness_percentage", parameterValue);
                    else if (parameterName.equals("bagroundLightVolume")) weatherInfo.put("background_light_volume", parameterValue);
                }
            }
            return weatherInfo.isEmpty() ? null : weatherInfo;
        } catch (Exception e) {
            return null;
        }
    }

    private static void sendJsonRespone(HttpExchange t, int status, String text) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", status);
        jsonResponse.put("message", text);
        String response = jsonResponse.toString();
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        t.getResponseHeaders().set("Content-Type", "application/json");
        t.sendResponseHeaders(status, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.flush();
        os.close();
    }
	
    private static SSLContext myServerSSLContext(String file, String pass) throws Exception {
        //String myPass = "myPass123";
        //String keystorePath = "keystore.jks";
        //args = new String[] {"keystore.jks", "myPass123"};

        char[] passphrase = pass.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(file), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

    
	public static void main(String[] args) throws Exception{
		try {
            String keystoreFile = "keystore.jks"; 
                String password = "myPass123";
                if (args.length >= 2) {
                    keystoreFile = args[0];
                    password = args[1];
                }
                
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
            SSLContext sslContext = myServerSSLContext(keystoreFile, password);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        InetSocketAddress remote = params.getClientAddress();
                        SSLContext c = getSSLContext();
                        SSLParameters sslParams = c.getDefaultSSLParameters();
                        params.setSSLParameters(sslParams);
                    } 
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Server myServer = new Server();
            UserAuthenticator authenticator = new UserAuthenticator("datarecord", myServer.myDb);
            HttpContext context = server.createContext("/datarecord", myServer);
            context.setAuthenticator(authenticator);
            server.createContext("/registration", new RegistrationHandler(authenticator));
            //server.setExecutor(null);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
	}
}
