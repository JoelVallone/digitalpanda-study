package application;

import org.glassfish.jersey.server.ResourceConfig;

import exceptions.CurrencyNotFoundMapper;
import exceptions.InternalErrorMapper;

import rates.RatesEndPoint;
import stronger.StrongerEndPoint;

public class ReactiveJavaApplication extends ResourceConfig {

    /**
     * Register JAX-RS application component
     */
    public ReactiveJavaApplication() {

    	register(RatesEndPoint.class);
    	register(StrongerEndPoint.class);
    	register(CurrencyNotFoundMapper.class);
    	register(InternalErrorMapper.class);
    }
}
