package app;

import javax.servlet.Filter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

import app.util.DownloadDaemon;
import in.ac.iitb.ivrs.telephony.base.config.IVRConfigs;

@SpringBootApplication
@EnableCaching
public class RuralIvrsApplication {

    public static void main(String[] args) throws Exception {
    	if (IVRConfigs.Network.USE_PROXY && IVRConfigs.Network.PROXY_USER.isEmpty()) {
    		System.out.println("Please set the proxy user and password in IvrsConfig!");
    		return;
    	}

    	//To be turned on when we need to download files to iitb server
        SpringApplication.run(RuralIvrsApplication.class, args);
 
   //     (new Thread(new DownloadDaemon())).start();
    }

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	@Bean
	public Filter OpenEntityManagerInViewFilter() {
	   OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
	   return filter;
	}

}


