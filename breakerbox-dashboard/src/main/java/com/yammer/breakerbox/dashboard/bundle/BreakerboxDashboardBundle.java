package com.yammer.breakerbox.dashboard.bundle;

import com.netflix.hystrix.dashboard.stream.MockStreamServlet;
import com.netflix.hystrix.dashboard.stream.ProxyStreamServlet;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class BreakerboxDashboardBundle implements ConfiguredBundle<Configuration> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        //TODO: NEED TO DISABLE GZIP for text/event-stream

        environment.servlets().addServlet("mock.stream", new MockStreamServlet()).addMapping("/tenacity/mock.stream");
        environment.servlets().addServlet("proxy.stream", new ProxyStreamServlet()).addMapping("/tenacity/proxy.stream");
    }
}