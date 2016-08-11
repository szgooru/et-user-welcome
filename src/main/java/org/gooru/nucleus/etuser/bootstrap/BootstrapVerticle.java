package org.gooru.nucleus.etuser.bootstrap;

import org.gooru.nucleus.etuser.bootstrap.startup.Initializer;
import org.gooru.nucleus.etuser.bootstrap.startup.Initializers;
import org.gooru.nucleus.etuser.processors.EmailProcessorFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapVerticle.class);

    @Override
    public void start(Future<Void> voidFuture) throws Exception {

        vertx.executeBlocking(blockingFuture -> {
            startApplication();
            blockingFuture.complete();
        }, future -> {

            if (future.succeeded()) {
                LOGGER.debug("Start application succeeded ...");
                EmailProcessorFinal emailProcessor = new EmailProcessorFinal();
                emailProcessor.process();

                voidFuture.complete();
            } else {
                LOGGER.debug("Start application failed ...");
                voidFuture.fail("Not able to initialize the Smoketest machinery properly");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        shutDownApplication();
        super.stop();
    }

    private void startApplication() {
        Initializers initializers = new Initializers();
        try {
            for (Initializer initializer : initializers) {
                initializer.initializeComponent(vertx, config());
            }
        } catch (IllegalStateException ie) {
            LOGGER.error("Error initializing application", ie);
            Runtime.getRuntime().halt(1);
        }
    }

    private void shutDownApplication() {

    }
}
