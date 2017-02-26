package hw2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	public static Map<String, Integer> fetchMap = new HashMap<String, Integer>();
	
	public static Map<String, Integer> statusMap = new HashMap<String, Integer>();
	
	public static Map<String, Integer> fileSizeMap = new HashMap<String, Integer>();
	
	public static Map<String, Integer> contentTypeMap = new HashMap<String, Integer>();
	
	private static Set<String> urlSet = new HashSet<String>();
	
	private static int urlCounter = 0;
	
	private static int fetchCounter = 0;
	
	public static void incrementUrlCounter() {
		Controller.urlCounter++;
	}
	
	public static int getUrlCounter() {
		return urlCounter;
	}

	public static void incrementFetchCounter() {
		Controller.fetchCounter++;
	}
	
	public static int getFetchCounter() {
		return fetchCounter;
	}
	
	public static void storeFetchAttempts(String fetchStatus)
	{
		if (fetchMap.containsKey(fetchStatus))
		{
			int value = fetchMap.get(fetchStatus);
			fetchMap.put(fetchStatus, value + 1);
		}
		else
		{
			fetchMap.put(fetchStatus, 1);
		}
	}

	public static String listFetchAttempts()
	{
		String output = "Fetch Statistics:\n================\n# fetches attempted: " + Controller.getFetchCounter() + "\n";
	    Iterator<String> iter = fetchMap.keySet().iterator();
	    while(iter.hasNext())
	    {
	        String fetchStatus = iter.next();
	        output += "# fetches " + fetchStatus + ": " + fetchMap.get(fetchStatus) + "\n";
	    }
	    return output;
	}
	
	public static void storeUrls(String url)
	{
		urlSet.add(url);
		Controller.incrementUrlCounter();
	}
	
	public static String listUrlStats()
	{
		String output = "Outgoing URLs:\n==============\nTotal URLs extracted: " + urlCounter +"\n";
	    output += "# unique URLs extracted: " + urlSet.size() + "\n";
	    Integer countIn = 0, countOut = 0;
	    Iterator<String> iter = urlSet.iterator();
	    while(iter.hasNext()) {
	        String element = iter.next();
	        if (element.startsWith("http://www.cnn.com/")) countIn++; else countOut++;
	    }
	    output += "# unique URLs within News Site: " + countIn + "\n";
	    output += "# unique URLs outside News Site: " + countOut + "\n";
	    return output;
	}

	public static void storeStatusCodes(String code)
	{
		if (statusMap.containsKey(code))
		{
			int value = statusMap.get(code);
			statusMap.put(code, value + 1);
		}
		else
		{
			statusMap.put(code, 1);
		}
	}

	public static String listStatusCodes()
	{
		String output = "Status Codes:\n=============\n";
	    Iterator<String> iter = statusMap.keySet().iterator();
	    while(iter.hasNext())
	    {
	        String code = iter.next();
	        output += code + ": " + statusMap.get(code) + "\n";
	    }
	    return output;
	}
	
	public static void storeFileSize(String fileSize)
	{
		if (fileSizeMap.containsKey(fileSize))
		{
			int value = fileSizeMap.get(fileSize);
			fileSizeMap.put(fileSize, value + 1);
		}
		else
		{
			fileSizeMap.put(fileSize, 1);
		}
	}

	public static String listFileSize()
	{
		String output = "File Sizes:\n===========\n";
	    Iterator<String> iter = fileSizeMap.keySet().iterator();
	    while(iter.hasNext())
	    {
	        String fileSize = iter.next();
	        output += fileSize + ": " + fileSizeMap.get(fileSize) + "\n";
	    }
	    return output;
	}
	
	public static void storeContentTypes(String contentType)
	{
		if (contentTypeMap.containsKey(contentType))
		{
			int value = contentTypeMap.get(contentType);
			contentTypeMap.put(contentType, value + 1);
		}
		else
		{
			contentTypeMap.put(contentType, 1);
		}
	}

	public static String listContentTypes()
	{
		String output = "Content Types:\n==============\n";
	    Iterator<String> iter = contentTypeMap.keySet().iterator();
	    while(iter.hasNext())
	    {
	        String contentType = iter.next();
	        output += contentType + ": " + contentTypeMap.get(contentType) + "\n";
	    }
	    return output;
	}

	public static void main(String[] args) throws Exception {
		// This is the magical number
		Integer maxPagesToFetch = 20000;
		
		String crawlStorageFolder = "crawl";
		int numberOfCrawlers = 1;
		CrawlConfig config = new CrawlConfig();
		config.setIncludeBinaryContentInCrawling(true);
		config.setCrawlStorageFolder(crawlStorageFolder);
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		Integer maxDepthOfCrawling = 16, politenessDelay = 200;
		config.setPolitenessDelay(politenessDelay);
		config.setMaxDepthOfCrawling(maxDepthOfCrawling);
		config.setMaxPagesToFetch(maxPagesToFetch);
		controller.addSeed("http://www.cnn.com/");
		controller.start(MyCrawler.class, numberOfCrawlers);
		BufferedWriter writer = null;
        try {
            File logFile = new File("CrawlReport_CNN.txt");
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("Name: Yermek Zhanabayev\nUSC ID: 5173409634\nNews site crawled: www.cnn.com\n\n");
            writer.write(Controller.listFetchAttempts() + "\n");
            writer.write(Controller.listUrlStats() + "\n");
            writer.write(Controller.listStatusCodes() + "\n");
            writer.write(Controller.listFileSize() + "\n");
            writer.write(Controller.listContentTypes() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}

}
