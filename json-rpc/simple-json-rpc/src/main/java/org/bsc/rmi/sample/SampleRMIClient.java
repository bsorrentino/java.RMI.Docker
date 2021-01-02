package org.bsc.rmi.sample;

import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import lombok.extern.java.Log;
import org.bsc.simplejsonrpc.client.JsonRpcHttpClient;

import java.net.URI;

import static java.lang.String.format;

/**
 * RMI client to invoke calls through the ServletHandler
 */
@Log
public class SampleRMIClient {


    public static void main(String args[]) {

        final String host = (args.length < 1) ? "localhost" : args[0];

        final JsonRpcClient client = new JsonRpcHttpClient( URI.create( format("http://%s/jsonrpc", host)) );

        final SampleRMI remote = client.onDemand(SampleRMI.class).serviceName("test").build();

        final String result = remote.justPass( "Hello JSON-RPC");

        log.info( format( "SampleRMI.justPass() = %s", result));

        final String info = remote.getInfo();

        log.info( format( "SampleRMI.getInfo() = %s", info));

        System.exit(0);
    }
}
