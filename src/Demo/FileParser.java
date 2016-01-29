package Demo; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

import com.sun.xml.internal.fastinfoset.util.CharArray;

public class FileParser {
	//final static String HTML_TAGS = "HTML,BODY,P,DIV,FONT,I,TR,TD";
	final static String LOC = "/home/anarchy/work/TUM/study/IDP/FinancialandMarkets/Filedownloads/";
	final static String FILE_NAME = "1 - Copy.txt";
	
	public static void main(String[] args) {
		final File f = new File(LOC + FILE_NAME);
		final ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
		FileChannel fc =  null;
		BufferedReader bfr = null;
		int tempCharAsc = 0;
		int totalGrossChars = 0;
		Stack <String> tagsStack = new Stack();
		String fileContentsStr = "";
		StringBuilder fileContents = new StringBuilder();
		long time1 = System.currentTimeMillis();
		long time2 = 0;
		try {
			fc = new RandomAccessFile(f, "r").getChannel();
			final CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();
	        while (fc.read(byteBuffer) > 0) {	        	
	            byteBuffer.flip();
	            fileContents.append(charsetDecoder.decode(byteBuffer));
	            byteBuffer.clear();
	        }
						
	        totalGrossChars = fileContents.length() - 1;	        
	        fileContentsStr = fileContents.toString().trim();
	        
	        // Parsing of secondary header elements
	        String secHeaderContents = fileContents.substring(fileContents.indexOf("<SEC-HEADER>")+12, fileContents.indexOf("</SEC-HEADER>"));	       
	        HashMap<String, String> secHeaderValuesMap = parseSecHeader(secHeaderContents);

	        System.out.println("*****Conformed period ="+ secHeaderValuesMap.get("CONFORMED PERIOD OF REPORT").substring(0, 4));	
        	System.out.println("*****Filed as of date ="+ secHeaderValuesMap.get("FILED AS OF DATE"));
	        
//	        int indexConformedPeriod = fileContentsStr.indexOf("CONFORMED PERIOD OF REPORT:");
//	        int indexFiledDate = fileContentsStr.indexOf("FILED AS OF DATE:");
//	        
//	        String conformed_period = fileContentsStr.substring(indexConformedPeriod + 28, indexConformedPeriod + 32) ;
//	        
//	        //String filedDate = fileContentsStr.substring(indexFiledDate + 18, indexConformedPeriod + 26) ;
//	        if (indexConformedPeriod > 0)
//	        {
//	        	
        		
//	        }
//	        else
//	        {
//	        	System.out.println("*****Conformed period not found");
//	        }
	        
	        
	        // Removing attributes for XBRL tags with just XBRL
	        String xbrlTagsList = "xbrl|XBRL";
	        StringTokenizer xbrlTokens = new StringTokenizer(xbrlTagsList,"|");
	        while (xbrlTokens.hasMoreElements())
	        {
	        	String tokenTemp = xbrlTokens.nextToken().toString();
	        	fileContentsStr = fileContentsStr.replaceAll("<"+tokenTemp+"(.*?):(.*?)>",
	        			"<xbrl>").replaceAll("</"+tokenTemp+"(.*?):(.*?)>", "</"+tokenTemp+">");
	        	fileContentsStr = fileContentsStr.replaceAll("<"+tokenTemp+">", "");
		        fileContentsStr = fileContentsStr.replaceAll("</"+tokenTemp+">", "");
	        }
	        
	        // Point 5.5 Remove tables
	        // Point 5.5.1 Getting examples of files having where table tags are used to demark the paragraphs of texts. 
	        // Removing the attributes of table tag
	        fileContentsStr = fileContents.toString().replaceAll("<TABLE(.*?)>", "<TABLE>").replaceAll("<table(.*?)>", "<table>");
	        
	        //if ()
	        // removing HTML tags
	        fileContentsStr = new HtmlToPlainText().getPlainText(Jsoup.parse(fileContentsStr));
	        fileContentsStr = fileContentsStr.replaceAll("<>", "");
	        fileContentsStr = fileContentsStr.replaceAll("\n+", "\n");
	        fileContentsStr = fileContentsStr.replaceAll("\n\r", "\n");
	       //TODO
	        //writeToFile(fileContentsStr);
	        
//	        System.out.println("*****index of Filed Date ="+ indexFiledDate);
	        
			System.out.println("***************************************************");
			System.out.println("Total characters in Gross file=" + totalGrossChars);
			time2 = System.currentTimeMillis();
			System.out.println("Time taken for initial processing the file=" + (time2-time1) + "ms");
//			time1 = System.currentTimeMillis();
//			System.out.println("Parsing is in process for file " + FILE_NAME + ".....");
//			String parsedText = Jsoup.parse(fileContents.toString()).text();
//			System.out.println("parsed info: \n"+parsedText);
//			time2 = System.currentTimeMillis();
//			System.out.println(" Time take to parse=" + (time2 - time1) + "ms");
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}				
	}
	
	
	public String parseFileCore(String filePath)
	{
		final File f = new File(filePath);
		final ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
		FileChannel fc =  null;
		BufferedReader bfr = null;
		int tempCharAsc = 0;
		int totalGrossChars = 0;
		Stack <String> tagsStack = new Stack();
		String fileContentsStr = "";
		StringBuilder fileContents = new StringBuilder();
		long time1 = System.currentTimeMillis();
		long time2 = 0;
		String conformedPeriod = "";
		String filingDate = "";
		try {
			fc = new RandomAccessFile(f, "r").getChannel();
			final CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();
	        while (fc.read(byteBuffer) > 0) {	        	
	            byteBuffer.flip();
	            fileContents.append(charsetDecoder.decode(byteBuffer));
	            byteBuffer.clear();
	        }
						
	        totalGrossChars = fileContents.length() - 1;	        
	        fileContentsStr = fileContents.toString().trim();
	        
	        // Parsing of secondary header elements
	        int indexSecHeader = fileContents.indexOf("<SEC-HEADER>");
	        String secHeaderContents = "";
	        if(indexSecHeader < 0)
	        {
	        	secHeaderContents = fileContents.substring(fileContents.indexOf("<IMS-HEADER>")+12, fileContents.indexOf("</IMS-HEADER>"));
	        }
	        else
	        {
	        	secHeaderContents = fileContents.substring(fileContents.indexOf("<SEC-HEADER>")+12, fileContents.indexOf("</SEC-HEADER>"));
	        }
	        	       
	        HashMap<String, String> secHeaderValuesMap = parseSecHeader(secHeaderContents);
	        conformedPeriod =   secHeaderValuesMap.get("CONFORMED PERIOD OF REPORT").substring(0, 4);
	        filingDate = secHeaderValuesMap.get("FILED AS OF DATE");
	        System.out.println("*****Conformed period ="+ secHeaderValuesMap.get("CONFORMED PERIOD OF REPORT").substring(0, 4));	
        	System.out.println("*****Filed as of date ="+ secHeaderValuesMap.get("FILED AS OF DATE"));
	        
	        // Removing attributes for XBRL tags with just XBRL
	        String xbrlTagsList = "xbrl|XBRL";
	        StringTokenizer xbrlTokens = new StringTokenizer(xbrlTagsList,"|");
	        while (xbrlTokens.hasMoreElements())
	        {
	        	String tokenTemp = xbrlTokens.nextToken().toString();
	        	fileContentsStr = fileContentsStr.replaceAll("<"+tokenTemp+"(.*?):(.*?)>",
	        			"<xbrl>").replaceAll("</"+tokenTemp+"(.*?):(.*?)>", "</"+tokenTemp+">");
	        	fileContentsStr = fileContentsStr.replaceAll("<"+tokenTemp+">", "");
		        fileContentsStr = fileContentsStr.replaceAll("</"+tokenTemp+">", "");
	        }
	        
	        // Point 5.5 Remove tables
	        // Point 5.5.1 Getting examples of files having where table tags are used to demark the paragraphs of texts. 
	        // Removing the attributes of table tag
	        fileContentsStr = fileContents.toString().replaceAll("<TABLE(.*?)>", "<TABLE>").replaceAll("<table(.*?)>", "<table>");
	        
	        //if ()
	        // removing HTML tags
	        fileContentsStr = new HtmlToPlainText().getPlainText(Jsoup.parse(fileContentsStr));
	        fileContentsStr = fileContentsStr.replaceAll("<>", "");
	        fileContentsStr = fileContentsStr.replaceAll("\n+", "\n");
	        fileContentsStr = fileContentsStr.replaceAll("\n\r", "\n");
	       
	        //TODO
	        //writeToFile(fileContentsStr);
	        
//	        System.out.println("*****index of Filed Date ="+ indexFiledDate);
	        
			System.out.println("***************************************************");
			System.out.println("Total characters in Gross file=" + totalGrossChars);
			time2 = System.currentTimeMillis();
			System.out.println("Time taken for initial processing the file=" + (time2-time1) + "ms");
//			time1 = System.currentTimeMillis();
//			System.out.println("Parsing is in process for file " + FILE_NAME + ".....");
//			String parsedText = Jsoup.parse(fileContents.toString()).text();
//			System.out.println("parsed info: \n"+parsedText);
//			time2 = System.currentTimeMillis();
//			System.out.println(" Time take to parse=" + (time2 - time1) + "ms");
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}	
		finally
		{
			try {
				fc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conformedPeriod+","+filingDate;
	}
	
	
	static boolean checkForHtml()
	{
		boolean isPresent = false;
		
		
		
		return isPresent;		
	}
	
	static boolean checkForXBRL()
	{
		boolean isPresent = false;
		
		
		
		return isPresent;		
	}
	
	static boolean checkForASCIIEnc()
	{
		boolean isPresent = false;
		
		
		
		return isPresent;		
	}
	
	//static void writeToFile(ByteBuffer byteBufferStr)
	static void writeToFile(String byteBufferStr)
	{
		try {
//			final File fw = new File(LOC + "newformat.txt");
//			FileChannel wfc = new RandomAccessFile(fw, "w").getChannel();
//			wfc.write(byteBufferStr);
//			wfc.close();		
			final File fw = new File(LOC + "newformat.txt");
			BufferedWriter bfw = new BufferedWriter(new FileWriter(fw));
			bfw.write(byteBufferStr);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	  
		
	}
	
	static HashMap<String,String> parseSecHeader(String secHeaderRaw)
	{
		HashMap<String, String> hmap = new HashMap<String, String>();
		char[] charArr = secHeaderRaw.toCharArray();
		StringBuilder tempStr = new StringBuilder();
		for (int i = 0 ; i< charArr.length; i++)
		{
			tempStr.append(charArr[i]+"");
			if (charArr[i] == '\n')
			{
				if (tempStr.toString().contains(":"))
				{
					String [] strArr = tempStr.toString().split(":");
					if (strArr.length > 1)
					{
						hmap.put(strArr[0].trim(), strArr[1].trim());
					}
				}
				tempStr.delete(0, tempStr.capacity()-1);
			}
		}
		System.out.println("hashmap ----is"+hmap);
		return hmap;
	}
	
	
	
}