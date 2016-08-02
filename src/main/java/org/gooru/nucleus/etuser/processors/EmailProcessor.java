package org.gooru.nucleus.etuser.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.gooru.nucleus.etuser.app.components.ApplicationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

public class EmailProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProcessor.class);
    private static final Logger LOGGER_FAIL = LoggerFactory.getLogger("org.gooru.nucleus.etuser.failure");
    
    private final Vertx vertx;

    public EmailProcessor(Vertx vertx) {
        this.vertx = vertx;
    }

    public void process() {
        LOGGER.debug("started processing emails");
        Set<String> emailIds = getEmailIds();
        emailIds.forEach(email -> {
            LOGGER.info("processing email: {}", email);
            HttpClientRequest emailRequest =
                getHttpClient().post(ApplicationRegistry.getInstance().getEmailAPIEndpoint(), responseHandler -> {
                    if (responseHandler.statusCode() == 200) {
                        LOGGER.info("SUCCESS : email sent to {}", email);
                    } else {
                        LOGGER_FAIL.error("FAIL : failed to send to {} with status code {}", email,
                            responseHandler.statusCode());
                    }
                });
            JsonObject data = generateEmailRequest(email);
            LOGGER.debug("data generated: {}", data.toString());
            emailRequest.putHeader("Content-Type", "application/json");
            emailRequest.putHeader("Content-Length", String.valueOf(data.toString().getBytes().length));
            emailRequest.putHeader("Authorization", getAuthorizationHeader());
            emailRequest.write(data.toString());
            emailRequest.end();
        });
    }

    private JsonObject generateEmailRequest(String email) {
        JsonObject request = ApplicationRegistry.getInstance().getEmailTemplateConfig();
        request.put("email_id", email);
        return request;
    }

    private Set<String> getEmailIds() {
        Set<String> emailIds = new HashSet<>();
        String fileName = ApplicationRegistry.getInstance().getInputFileName();
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(new File(fileName))); 
            while ((line = br.readLine()) != null) {
                String email = line.trim();
                if (emailIds.contains(email)) {
                    LOGGER.debug("duplicate: {}", email);
                }
                emailIds.add(email);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error reading input data:", e.getMessage());
        }
        
        LOGGER.debug("number of email ids : {}", emailIds.size());
        return emailIds;
    }

    private HttpClient getHttpClient() {
        return vertx
            .createHttpClient(new HttpClientOptions().setDefaultHost(ApplicationRegistry.getInstance().getAPIHost()));
    }

    private String getAuthorizationHeader() {
        return "Token " + ApplicationRegistry.getInstance().getAuthToken();
    }
}
