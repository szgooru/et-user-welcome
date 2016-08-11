package org.gooru.nucleus.etuser.processors;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.gooru.nucleus.etuser.app.components.ApplicationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class EmailProcessorFinal {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProcessorFinal.class);
    private static final Logger LOGGER_FAIL = LoggerFactory.getLogger("org.gooru.nucleus.etuser.failure");

    private final String url;
    private final String token;
    private final JsonObject defaultRequest;
    int successCnt = 0;
    int failedCnt = 0;
    
    public EmailProcessorFinal() {
        this.url = ApplicationRegistry.getInstance().getEmailUrl();
        this.token = ApplicationRegistry.getInstance().getAuthToken();
        this.defaultRequest = ApplicationRegistry.getInstance().getEmailTemplateConfig();
    }

    public void process() {
        Set<String> emailIds = getEmailIds();
        LOGGER.debug("started processing emails");
        
        emailIds.forEach(email -> {
            String data = generateEmailRequest(email).toString();
            try {
                LOGGER.debug("data generated: {}", data.toString());
                int responseCode = sendEmail(data);

                if (responseCode == 200) {
                    LOGGER.info("SUCCESS : email sent to {}", email);
                    successCnt++;
                } else {
                    LOGGER_FAIL.error("FAIL : failed to send to {} with status code {}", email, responseCode);
                    failedCnt++;
                }
            } catch (Throwable t) {
                LOGGER.error("error while sending email to {}", email, t);
            }
        });
        
        LOGGER.info("Email Sent FINISH: Success({}), Failed ({})", successCnt, failedCnt);
    }

    private JsonObject generateEmailRequest(String email) {
        JsonObject request = this.defaultRequest.copy();
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

    private int sendEmail(String data) throws Throwable {
        URL url = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Token " + token);

        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();

        return connection.getResponseCode();
    }

}
