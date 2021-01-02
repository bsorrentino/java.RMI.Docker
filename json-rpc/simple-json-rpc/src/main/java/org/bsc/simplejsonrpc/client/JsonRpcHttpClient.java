package org.bsc.simplejsonrpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import com.google.common.net.MediaType;
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
import java.net.URISyntaxException;
import java.util.Optional;

import static java.lang.String.format;

public class JsonRpcHttpClient extends JsonRpcClient  {

    static class HttpClientTransport implements Transport, Closeable {

        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final java.net.URI uri;

        @NotNull
        private HttpClientTransport(@NotNull URI uri) {
            this.uri = uri;
        }

        private java.net.URI ofService( @NotNull String serviceName ) {
            try {
                return new URI( uri.getScheme(),
                        uri.getUserInfo(),
                        uri.getHost(),
                        uri.getPort(),
                        format( "%s/%s", uri.getPath(), serviceName ),
                        uri.getQuery(),
                        uri.getFragment());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(format("error creating service URI for service %s", serviceName), e );
            }
        }
        @Override
        public @NotNull String pass(@NotNull Optional<String> serviceName, @NotNull String request) throws IOException {
            final HttpPost post = new HttpPost( serviceName.map( this::ofService ).orElse(uri) );
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

    public JsonRpcHttpClient( @NotNull java.net.URI baseUri ) {
        super( new HttpClientTransport(baseUri) );
    }

    public JsonRpcHttpClient(@NotNull java.net.URI baseUri , @NotNull ObjectMapper mapper) {
        super(new HttpClientTransport(baseUri), mapper);
    }
}
