/*
 * Copyright 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.bsc.rmi.sample;

import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import com.google.common.net.MediaType;
import lombok.extern.java.Log;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import static java.lang.String.format;

/**
 * RMI client to invoke calls through the ServletHandler
 */
@Log
public class SampleRMIClient {

    static class HttpClientTransport implements Transport, Closeable {

        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final java.net.URI uri;

        private HttpClientTransport(@NotNull URI uri) {
            this.uri = uri;
        }

        @Override
        public @NotNull String pass(@NotNull String request) throws IOException {
            HttpPost post = new HttpPost(uri);
            post.setEntity(new StringEntity(request, Charsets.UTF_8));
            post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
            try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                return EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            }
        }

        @Override
        public void close() throws IOException {
            httpClient.close();
        }
    }

    public static void main(String args[]) throws Exception {

        final String host = (args.length < 1) ? "localhost" : args[0];

        final Transport transport = new HttpClientTransport(URI.create( format("http://%s/jsonrpc", host)));
        final JsonRpcClient client = new JsonRpcClient( transport );

        final String result = client.onDemand(SampleRMI.class).justPass( "Hello JSON-RPC");

        log.info( format( "SampleRMI.justPass() = %s", result));

        System.exit(0);
    }
}
