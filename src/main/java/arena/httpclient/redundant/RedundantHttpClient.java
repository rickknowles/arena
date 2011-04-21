package arena.httpclient.redundant;

import java.io.IOException;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.httpclient.AbstractHttpClient;
import arena.httpclient.HttpClient;
import arena.httpclient.HttpResponse;


public class RedundantHttpClient extends AbstractHttpClient {
    private final Log log = LogFactory.getLog(RedundantHttpClient.class);

    private HttpClient httpClient;
    private RedundantHttpClientSource owner;

    protected Integer connectTimeout;
    protected Integer readTimeout;
    
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }
    
    public RedundantHttpClient(HttpClient httpClient, RedundantHttpClientSource owner) {
        this.httpClient = httpClient;
        this.owner = owner;
    }

    public HttpResponse get() throws IOException {
        if (url.startsWith("serverpool://")) {
            String poolName = url.substring("serverpool://".length());
            if (poolName.indexOf("/") != -1) {
                poolName = poolName.substring(0, poolName.indexOf("/"));
            }
            HostPool pool = this.owner.getPoolByName(poolName);
            if (pool == null) {
                throw new RuntimeException("Unknown pool name: " + poolName);
            }
            String urlPath = url.substring(("serverpool://" + poolName).length());
            
            // If pooling is turned off, use the first in the pool
            return doFailoverRequest(pool, urlPath);
        } else {
            return this.httpClient.get();
        }
    }
    
    /**
     * Performs a failover lookup on each of the hosts in a pool until one of
     * them returns successfully.
     */
    protected HttpResponse doFailoverRequest(HostPool pool, String partialURL) {
        HostConfig current = pool.getHost();
        while (current != null) {
            log.debug("Trying host: " + current.getHostname() + " from pool: " + pool.getPoolName());
            this.httpClient.setUrl(current.buildFullURL(partialURL));
            this.httpClient.setPost(this.isPost);
            this.httpClient.setRequestEncoding(this.encoding);
            this.httpClient.setRequestParameters(this.requestParameters);
            if (this.connectTimeout != null) {
                this.httpClient.setConnectTimeout(this.connectTimeout);
            } else if (pool.getConnectTimeout() != null) {
                this.httpClient.setConnectTimeout(pool.getConnectTimeout());
            }
            if (this.readTimeout != null) {
                this.httpClient.setReadTimeout(this.readTimeout);
            } else if (pool.getReadTimeout() != null) {
                this.httpClient.setReadTimeout(pool.getReadTimeout());
            }

            try {
                return this.httpClient.get();
            } catch (Throwable err) {
                log.error("Error in pooled Http client: host=" + current.getHostname(), err);
                pool.flagHostAsFailed(current);
            }
            current = pool.getHost();
        }
        
        // If we reach here, we have run out of hosts
        throw new RuntimeException("Host pool: " + pool.getPoolName() + 
                " exhausted. All hosts are unavailable or failed");
    }

}
