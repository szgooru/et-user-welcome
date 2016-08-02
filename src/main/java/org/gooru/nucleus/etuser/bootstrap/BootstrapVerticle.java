package org.gooru.nucleus.etuser.bootstrap;

import org.gooru.nucleus.etuser.bootstrap.startup.Initializer;
import org.gooru.nucleus.etuser.bootstrap.startup.Initializers;
import org.gooru.nucleus.etuser.processors.EmailProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapVerticle.class);
    private EventBus eventBus;

    @Override
    public void start(Future<Void> voidFuture) throws Exception {

        eventBus = vertx.eventBus();

        vertx.executeBlocking(blockingFuture -> {
            startApplication();
            blockingFuture.complete();
        }, future -> {

            if (future.succeeded()) {
                LOGGER.debug("Start application succeeded ...");
                EmailProcessor emailProcessor = new EmailProcessor(vertx);
                emailProcessor.process();

                voidFuture.complete();
            } else {
                LOGGER.debug("Start application failed ...");
                eventBus = null;
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
