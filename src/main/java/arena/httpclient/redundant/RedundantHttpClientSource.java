package arena.httpclient.redundant;

import java.util.Collections;
import java.util.Map;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.httpclient.HttpClient;
import arena.httpclient.HttpClientSource;


public class RedundantHttpClientSource implements HttpClientSource {
    private final Log log = LogFactory.getLog(RedundantHttpClientSource.class);

    private HttpClientSource httpClientSource;
    private Map<String,HostPool> serverPools;
    
    public void setHttpClientSource(HttpClientSource httpClientSource) {
        this.httpClientSource = httpClientSource;
    }

    public void setServerPools(Map<String, HostPool> serverPools) {
        this.serverPools = Collections.synchronizedMap(serverPools);
        log.info("Loading server pools: " + this.serverPools.keySet());
    }

    public HttpClient getClient() {
        return new RedundantHttpClient(this.httpClientSource.getClient(), this);
    }
    
    public HostPool getPoolByName(String name) {
        return this.serverPools.get(name);
    }

}
