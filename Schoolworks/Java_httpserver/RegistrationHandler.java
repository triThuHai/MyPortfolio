package com.o3.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;


public class RegistrationHandler implements HttpHandler {
    
    private final UserAuthenticator authenticator;

    public RegistrationHandler(UserAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    private static void send(HttpExchange t, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        t.sendResponseHeaders(status, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.flush();
        os.close();
    }

    private static String readRequestBody(InputStream requestBody) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader
            (requestBody, StandardCharsets.UTF_8));
        String requestBodyText = reader.lines().collect(Collectors.joining("\n")).trim();
        reader.close();
        return requestBodyText;
    }

    public void handle(HttpExchange t) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                send(t, 400, "Not supported request method");
                return;
            }

            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) {
                send(t, 405, "Not supported request method");
                return;
            }

            String contentType = t.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                send(t, 415, "Content-Type must be application/json");
                return;
            }

            String requestBody = readRequestBody(t.getRequestBody());
            if (requestBody == null || requestBody.trim().isEmpty()) {
            send(t, 400, "Request body is empty");
            return;
            }

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(requestBody);
            } catch (JSONException e) {
                send(t, 400, "Invalid JSON format: " + e.getMessage());
                return;
            }
            
            if (!jsonObject.has("username") 
                || !jsonObject.has("password") 
                || !jsonObject.has("email")
                || !jsonObject.has("nickname")) {
                send(t, 400, "Missing required fields: username, password, email, and nickname are required");
                return;
            }

            String userName = jsonObject.getString("username");
            String passWord = jsonObject.getString("password");
            String email = jsonObject.getString("email");
            String nickName = jsonObject.getString("nickname");

            if (userName == null || userName.trim().isEmpty()) { 
                send(t, 400, "Username must not be empty"); return; 
            }
            
            if (passWord == null || passWord.trim().isEmpty()) { 
                send(t, 400, "Password must not be empty"); return; 
            }

            if (email == null || email.trim().isEmpty()) { 
                send(t, 400, "Email must not be empty"); return; 
            }

            if (nickName == null || nickName.trim().isEmpty()) { 
                send(t, 400, "Nickname must not be empty"); return; 
            }
            
            if (!authenticator.addUser(userName, passWord, email, nickName)) {
                send(t, 409, "Username " + userName + " already exists or invalid input");
                return;
            }

            send(t, 200, "User registered successfully");
        } catch (JSONException e) {
            send(t, 400, "Error parsingJSON data: " + e.getMessage());
        } catch (Exception e) {
            send(t, 500, "Internal server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            t.close();
        }
    }
}
