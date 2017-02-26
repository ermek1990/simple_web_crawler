package hw2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import com.csvreader.CsvWriter;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	
	private final static String[] fileSizeArray = {"< 1KB", "1KB ~ <10KB", "10KB ~ <100KB", "100KB ~ <1MB", ">= 1MB"}; 
	
	private final static String[] fetchStatusArray = {"attempted", "succeeded", "aborted", "failed"};
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		Controller.storeUrls(url.getURL());
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith("http://www.cnn.com/");
	}
	
	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		Controller.storeStatusCodes(Integer.toString(statusCode) + " " + statusDescription);
		Controller.incrementFetchCounter();
		if (statusCode >= 200 && statusCode <= 299) {
			Controller.storeFetchAttempts(fetchStatusArray[1]);
		} else if (statusCode >= 300 && statusCode <= 399) {
			Controller.storeFetchAttempts(fetchStatusArray[2]);
		} else if (statusCode == 408 || statusCode == 598 || statusCode == 504 || statusCode == 599) {
			Controller.storeFetchAttempts(fetchStatusArray[2]);
		} else {
			Controller.storeFetchAttempts(fetchStatusArray[3]);
		}
    }
	
	@Override
	public void visit(Page page) {
		String outputFile = "fetch_CNN.csv";
		writeToFile(page, outputFile);
		outputFile = "visit_CNN.csv";
		writeToFile(page, outputFile);
	}
	
	public void writeToFile(Page page, String fileName) {
		CsvWriter csvOutput = null;
		try {
			csvOutput = new CsvWriter(new FileWriter(fileName, true), ',');
			String url = page.getWebURL().getURL();
			Integer statusCode = page.getStatusCode();
			switch (fileName) {
				case "fetch_CNN.csv":
					csvOutput.write(url);
					csvOutput.write(statusCode.toString());
					csvOutput.endRecord();
					break;
				case "visit_CNN.csv":
					if (statusCode >= 200 && statusCode <= 299) {
						Integer fileSize = 0, numUrls = 0;
						String contentType = "";
						if (page.getParseData() instanceof HtmlParseData) {
							HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
							fileSize = htmlParseData.getHtml().length();
							numUrls = htmlParseData.getOutgoingUrls().size();
							contentType = getTypeOfContent(page);
						} else if (page.getParseData() instanceof BinaryParseData) {
							BinaryParseData binParseData = (BinaryParseData) page.getParseData();
							fileSize = binParseData.getHtml().length();
							csvOutput.write(Integer.toString(binParseData.getOutgoingUrls().size()));
							contentType = getTypeOfContent(page);
						}
						csvOutput.write(url);
						if (fileSize < 1024){
							Controller.storeFileSize(fileSizeArray[0]);
						} else if (fileSize >= 1024 && fileSize < 10240){
							Controller.storeFileSize(fileSizeArray[1]);
						} else if (fileSize >= 10240 && fileSize < 102400){
							Controller.storeFileSize(fileSizeArray[2]);
						} else if (fileSize >= 102400 && fileSize < 1048576){
							Controller.storeFileSize(fileSizeArray[3]);
						} else {
							Controller.storeFileSize(fileSizeArray[4]);
						}
						csvOutput.write(Integer.toString(fileSize));
						csvOutput.write(Integer.toString(numUrls));
						Controller.storeContentTypes(contentType);
						csvOutput.write(contentType);
						csvOutput.endRecord();
					}
					break;
            }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			csvOutput.close();
		}
	}
	
	public String getTypeOfContent(Page page) {
		String type = "";
		if (page.getContentType() != null) {
			if (page.getContentType().indexOf(";") != -1) {
				type = page.getContentType().substring(0, page.getContentType().indexOf(";"));
			} else {
				type = page.getContentType();
			}
		} else {
			type = "text/html";
		}
		return type;
	}
}
