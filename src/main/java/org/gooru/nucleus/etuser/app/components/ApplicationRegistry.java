package org.gooru.nucleus.etuser.app.components;

import org.gooru.nucleus.etuser.bootstrap.startup.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ApplicationRegistry implements Initializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistry.class);
    private volatile boolean initialized = false;
    private JsonObject configObject;
    
    private String emailUrl;
    private JsonObject emailTemplateConfig;
    private String inputFileName;
    private String authToken;
    
    private ApplicationRegistry() {
    }
    
    public static ApplicationRegistry getInstance() {
        return Holder.INSTANCE;
    }
    
    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        LOGGER.info("initializing ApplicationRegistry");
        
        if (!initialized) {
            LOGGER.debug("not already initialized..");
            synchronized (Holder.INSTANCE) {
                LOGGER.debug("Will initialize after double checking");
                if (!initialized) {
                  this.configObject = config.copy();

                  this.emailUrl = this.configObject.getString("email.url");
                  if (this.emailUrl == null || this.emailUrl.isEmpty()) {
                      throw new IllegalStateException("Email URL not provided");
                  }
                  
                  this.emailTemplateConfig = this.configObject.getJsonObject("email.template.config");
                  if (this.emailTemplateConfig == null || this.emailTemplateConfig.isEmpty()) {
                      throw new IllegalStateException("email template config not provided");
                  }
                  
                  this.inputFileName = this.configObject.getString("input.data");
                  if (this.inputFileName == null || this.inputFileName.isEmpty()) {
                      throw new IllegalStateException("input file not provided");
                  }
                  
                  this.authToken = this.configObject.getString("auth.token");
                  if (this.authToken == null || this.authToken.isEmpty()) {
                      throw new IllegalStateException("auth token not provided");
                  }

                  initialized = true;
                  LOGGER.debug("Initialization done.");
                }
              }
        }
    }
    
    public String getEmailUrl() {
        return this.emailUrl;
    }
    
    public JsonObject getEmailTemplateConfig() {
        return this.emailTemplateConfig;
    }
    
    public String getInputFileName() {
        return this.inputFileName;
    }
    
    public String getAuthToken() {
        return this.authToken;
    }

    private static class Holder {
        private static final ApplicationRegistry INSTANCE = new ApplicationRegistry();
    }
}
