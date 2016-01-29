package Demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logger.LogSetup;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class FileParserCore {

	final static String LOC = "D:/work/TUM/study/IDP/";
//	final static String folderName = "Extracted Files";
	final static String folderName = "trial";

	final static String DEST_LOC = "D:/work/TUM/study/IDP/";
//	final static String DEST_FOLDER = "ParseFiles";
	final static String DEST_FOLDER = "trialParsed";

	private static Logger logger = Logger.getRootLogger();
	static String compFileName = "";
	static String neatFileName = "";
	static String newParsedFileName = "";
	static boolean isHeaderPresent = true;
	int totalGrossChars = 0;
	int netChars = 0;
	int asciiEncChars = 0;
	int htmlChars = 0;
	int xbrlChars = 0;
	int tableChars = 0;
	int numPages = 0;
	double grossFileSize = 0;
	double netFileSize = 0;
	String secHeaderContents = "";
	static String gvKey = "";
	static String cik = "";
	static String fyear = "";
	static String fyear_end = "";
	File fileTraversal = null;
	
	public FileParserCore(Logger log) {
		// TODO Auto-generated constructor stub
		logger = log;
	}
	public FileParserCore() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args) {
		try {
//			LogSetup log = new LogSetup(LOC + "ParsingHistory.log", Level.INFO);
			LogSetup log = new LogSetup(LOC + "ParsingHistoryTrial.log", Level.INFO);
			FileParserCore fparserCore = new FileParserCore();
			fparserCore.traverse( new File(LOC+folderName));
//			fparserCore.traverse(new File("D:/work/TUM/study/IDP/trial"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Main | *****" + e);
			e.printStackTrace();
		}

	}

	public void traverse(File file) {
		try {
			if (!file.isDirectory()
					&& file.getAbsoluteFile().toString().contains(".txt")) {
				ExecutorService executor = Executors.newSingleThreadExecutor();
			     Future<String> future = executor.submit(new Task());
			     try {			            
			            fileTraversal = file; 
			            logger.info("Started.." + fileTraversal);
			            logger.info(future.get(60, TimeUnit.SECONDS));
			            //logger.info("Finished!");
			        } catch (TimeoutException e) {
			        	logger.error(compFileName + "|Timed out.");
			            System.out.println("Terminated!");
			        }

			        executor.shutdownNow();
//			        try {
//			        	executor.awaitTermination(15, TimeUnit.NANOSECONDS);
//			        	} catch (InterruptedException e) {
//			        	  e.printStackTrace();
//			        	}
//			     parseFileCore(file);
			}
			if (file.isDirectory()) {
				String entries[] = file.list();
				if (entries != null) {
					for (String entry : entries) {
						traverse(new File(file, entry));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("traverse | *****" + e);
			e.printStackTrace();
		}
	}
	
	class Task implements Callable<String> {
	    @Override
	    public String call() throws Exception {
	    	parseFileCore(fileTraversal);// Just to demo a long running task of 4 seconds.
	        return compFileName + " Task completed..... !!!!!";
	    }
	}

	public String parseFileCore(File filePath) {
		compFileName = filePath.getAbsolutePath();

		logger.info("parseFileCore | *****" + compFileName);
		final File f = filePath;
		final ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
		long grossBytesLength = f.length();
		
		FileChannel fc = null;
		BufferedReader bfr = null;
		int tempCharAsc = 0;
		String headerRemovedString = "";
		String processingTime = "";
		String fileContentsStr = "";
		String fileFinalContents = "";
		StringBuilder fileContents = new StringBuilder();
		long time1 = System.currentTimeMillis();
		long time2 = 0;
		String conformedPeriod = "";
		String filingDate = "";
		int tempGrossChars = 0;
		int countHtmlPresChars = 0;
		String sicNumStr = "";

		StringBuilder secHeaderLogStr = new StringBuilder();
		StringBuilder calculationsStr = new StringBuilder();
		try {
			neatFileName = filePath.getName();
			logger.info("just the file name= " + neatFileName);
			fc = new RandomAccessFile(f, "r").getChannel();
			final CharsetDecoder charsetDecoder = Charset.forName("UTF-8")
					.newDecoder();
			while (fc.read(byteBuffer) > 0) {
				byteBuffer.flip();
				fileContents.append(charsetDecoder.decode(byteBuffer));
				byteBuffer.clear();
			}
//			grossFileSize = grossBytesLength;
			
			int descIndex = fileContents.indexOf("<DESCRIPTION>");
			String descString = fileContents.substring(descIndex, descIndex+25);
			if(!(descString.toUpperCase().contains("10-K") || descString.toUpperCase().contains("10K")))
			{
				logger.info("WRONG FILE.... Returning........." + compFileName);
				logger.info("Completeinformation|" + compFileName + "|" + "File Dropped" + "|" + gvKey + "|"
						+ cik + "|" + fyear + "|" + fyear_end + "|" + gvKey + "_" + cik + "_" + fyear
						+ "|" + secHeaderLogStr.toString() + calculationsStr.toString()
						+ processingTime);
				return "Wrong File";
			}
			
			double fileSizeinKB = grossBytesLength / 1024.00 ;
			BigDecimal bd = new BigDecimal(fileSizeinKB);
		    bd = bd.setScale(2, RoundingMode.HALF_UP);
		    grossFileSize = bd.doubleValue();
			totalGrossChars = fileContents.length();
			logger.info("Gross size in KB = " + grossFileSize);
			logger.info("Gross chars nos.  = " + totalGrossChars);
			
			fileContentsStr = fileContents.toString().trim();
			numPages = StringUtils.countMatches(fileContentsStr,
					"page-break-before:always");
			// Removing ASCII file segments for EXCEL, PDF, ZIP, GRAPHIC

			fileContentsStr = escapeFileTypeSegments(fileContentsStr);
			asciiEncChars = totalGrossChars - fileContentsStr.length();

			// logger.info(" After removing segments---------------" +
			// fileContentsStr);

			
			
			// Parsing of secondary header elements
			int indexSecHeader = fileContents.indexOf("<SEC-HEADER>");

			try {
				if (indexSecHeader < 0) {
					secHeaderContents = fileContents.substring(
							fileContents.indexOf("<IMS-HEADER>") + 12,
							fileContents.indexOf("</IMS-HEADER>"));
					fileContentsStr = fileContentsStr.substring(0,
							fileContentsStr.indexOf("<IMS-HEADER>"))
							+ fileContentsStr.substring(fileContentsStr
									.indexOf("</IMS-HEADER>")
									+ "</IMS-HEADER>".length() + 1);
					fileContentsStr = fileContentsStr.substring(fileContentsStr
							.indexOf("<IMS-DOCUMENT>"));
					fileContentsStr = fileContentsStr.substring(0,
							fileContentsStr.indexOf("</IMS-DOCUMENT>")
									+ "</IMS-DOCUMENT>".length());
				} else {
					secHeaderContents = fileContents.substring(
							fileContents.indexOf("<SEC-HEADER>") + 12,
							fileContents.indexOf("</SEC-HEADER>"));
					fileContentsStr = fileContentsStr.substring(0,
							fileContentsStr.indexOf("<SEC-HEADER>"))
							+ fileContentsStr.substring(fileContentsStr
									.indexOf("</SEC-HEADER>")
									+ "</SEC-HEADER>".length() + 1);
					fileContentsStr = fileContentsStr.substring(fileContentsStr
							.indexOf("<SEC-DOCUMENT>"));
					fileContentsStr = fileContentsStr.substring(0,
							fileContentsStr.indexOf("</SEC-DOCUMENT>")
									+ "</SEC-DOCUMENT>".length());
				}

				HashMap<String, String> headerValuesMap = parseSecHeader(secHeaderContents);
				conformedPeriod = headerValuesMap.get(
						"CONFORMED PERIOD OF REPORT").substring(0, 4);
				filingDate = headerValuesMap.get("FILED AS OF DATE");
				if (headerValuesMap.get("STANDARD INDUSTRIAL CLASSIFICATION") != null || "".equals(headerValuesMap.get("STANDARD INDUSTRIAL CLASSIFICATION")))
				{
					String sicNUMArr [] =  headerValuesMap.get("STANDARD INDUSTRIAL CLASSIFICATION").split("\\[");
					
					if (sicNUMArr.length > 1)
					{
						sicNumStr = sicNUMArr[1].substring(0, sicNUMArr[1].length() - 1);
						//logger.info("Have alphanumeric " + sicNumStr);
					}
					else
					{
						sicNumStr = sicNUMArr[0];
						//logger.info("Have numeric " + sicNUMArr[0]);
					}
				}
				
				String businessZip = headerValuesMap.get("BUSINESS_ZIP");
				if (businessZip != null && !"".equals(businessZip))
				{
					businessZip = businessZip.trim();
					if (businessZip.charAt(0)=='-')
						businessZip = businessZip.substring(1, businessZip.length());
					else if ("00000".equals(businessZip))
						businessZip = "null";
				}
				else
				{
					businessZip = ("".equals(businessZip) ? "null" : businessZip);
				}
				logger.info(" Business zip = " + businessZip);
				
				String mailZip = headerValuesMap.get("MAIL_ZIP");				
				
				if (mailZip != null && !"".equals(mailZip))
				{
					mailZip = mailZip.trim();
					if (mailZip.charAt(0)=='-')
						mailZip = mailZip.substring(1, mailZip.length());
					else if ("00000".equals(mailZip))
						mailZip = "null";
				}
				else
				{
					mailZip = ("".equals(mailZip) ? "null" : mailZip);
				}			
				
				logger.info(" mail zip = " + mailZip);
				
				/*logger.info("business address====" + headerValuesMap.get("BUSINESS_STREET 1")
						+ "|"
						+ headerValuesMap.get("BUSINESS_STREET 2")
						+ "|"
						+ headerValuesMap.get("BUSINESS_CITY")
						+ "|"
						+ headerValuesMap.get("BUSINESS_STATE")
						+ "|"
						+ headerValuesMap.get("BUSINESS_ZIP"));*/
				secHeaderLogStr = secHeaderLogStr.append(headerValuesMap
						.get("FILED AS OF DATE")
						+ "|"
						+ headerValuesMap.get("CONFORMED PERIOD OF REPORT")
						+ "|"
						+ headerValuesMap.get("CONFORMED PERIOD OF REPORT")
								.substring(0, 4)
						+ "|"
						+ getFyear2(headerValuesMap.get("CONFORMED PERIOD OF REPORT"))
						+ "|"
						+ headerValuesMap.get("CONFORMED SUBMISSION TYPE")
						+ "|"
						+ headerValuesMap.get("ACCESSION NUMBER")
						+ "|"
						+ headerValuesMap.get("PUBLIC DOCUMENT COUNT")
						+ "|"
						+ headerValuesMap.get("COMPANY CONFORMED NAME")
						+ "|"
						+ headerValuesMap.get("CENTRAL INDEX KEY")
						+ "|"
						+ headerValuesMap
								.get("STANDARD INDUSTRIAL CLASSIFICATION")
						+ "|"
						+ sicNumStr
						+ "|"
						+ headerValuesMap.get("IRS NUMBER")
						+ "|"
						+ headerValuesMap.get("STATE OF INCORPORATION")
						+ "|"
						+ headerValuesMap.get("FISCAL YEAR END")
						+ "|"
						+ headerValuesMap.get("SEC ACT")
						+ "|"
						+ headerValuesMap.get("SEC FILE NUMBER")
						+ "|"
						+ headerValuesMap.get("FILM NUMBER")
						+ "|"
						+ headerValuesMap.get("FORMER CONFORMED NAME")
						+ "|"
						+ headerValuesMap.get("DATE OF NAME CHANGE")
						+ "|"
						+ headerValuesMap.get("BUSINESS_STREET 1")
						+ "|"
						+ headerValuesMap.get("BUSINESS_STREET 2")
						+ "|"
						+ headerValuesMap.get("BUSINESS_CITY")
						+ "|"
						+ headerValuesMap.get("BUSINESS_STATE")
						+ "|"
						+ businessZip
						+ "|"
						+ headerValuesMap.get("BUSINESS PHONE")
						+ "|"
						+ headerValuesMap.get("MAIL_STREET 1")
						+ "|"
						+ headerValuesMap.get("MAIL_STREET 2")
						+ "|"
						+ headerValuesMap.get("MAIL_CITY")
						+ "|"
						+ headerValuesMap.get("MAIL_STATE")
						+ "|"
						+ mailZip + "|");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isHeaderPresent = false;
				logger.error("No headers found in " + compFileName + "   " + e);
				String[] tempNameArr = compFileName.split("_");
				// System.out.println("tempNameArr[0] : " + tempNameArr[0]);
				int lastIndex = tempNameArr[0].lastIndexOf("\\");
				tempNameArr[0] = tempNameArr[0].substring(lastIndex + 1);
				// System.out.println("tempNameArr[0] now : " + tempNameArr[0]);
				String newNameFile = "";
				for (int i = 0; i < tempNameArr.length - 1; i++) {
					newNameFile += tempNameArr[i] + "_";
					switch (i) {
					case 0:
						gvKey = tempNameArr[i];
						break;
					case 1:
						cik = tempNameArr[i];
						break;
					case 3:
						fyear = tempNameArr[i];
						break;
					}
				}
				logger.info("Completeinformation|" + compFileName + "|No headers found|" + gvKey + "|"
						+ cik);
				throw e;
			}

			tempGrossChars = fileContentsStr.length();

			
			String preservedChars = "&LT;|&GT;|&NBSP;|&QUOT;|&APOS;|&AMP;|";
			StringTokenizer preservedCharsTokenizer = new StringTokenizer(
					preservedChars, "|");
			while (preservedCharsTokenizer.hasMoreElements()) {
				String tokenTemp = preservedCharsTokenizer.nextToken()
						.toString();
				switch (tokenTemp) {
				case "&LT;":
					fileContentsStr = fileContentsStr.replaceAll("(?i)"
							+ tokenTemp, "<");
					break;
				case "&GT;":
					fileContentsStr = fileContentsStr.replaceAll("(?i)"
							+ tokenTemp, ">");
					break;
				case "&NBSP;":
					fileContentsStr = fileContentsStr.replaceAll("(?i)"
							+ tokenTemp, " ");
					break;
				case "&QUOT;":
					fileContentsStr = fileContentsStr.replaceAll("(?i)"
							+ tokenTemp, "\"");
					break;
				case "&APOS;":
					fileContentsStr = fileContentsStr.replaceAll("(?i)"
							+ tokenTemp, "'");
					break;
				case "&AMP;":
					fileContentsStr = fileContentsStr.replaceAll("(?i)"
							+ tokenTemp, "&");
					break;

				}
			}

			fileContentsStr = fileContentsStr.replaceAll("&#160;", " ");
			fileContentsStr = fileContentsStr.replaceAll("&#60;", "<");
			fileContentsStr = fileContentsStr.replaceAll("&#62;", ">");
			fileContentsStr = fileContentsStr.replaceAll("&#38;", "&");
			fileContentsStr = fileContentsStr.replaceAll("&#39;", "'");
			fileContentsStr = fileContentsStr.replaceAll("&#34;", "\"");
			fileContentsStr = fileContentsStr.replaceAll("&#151;", " "); // Newly added rule by Daniel Bias 
			fileContentsStr = fileContentsStr.replaceAll("&#xA0;", ""); // Present
																		// in
																		// the
																		// file
																		// -
																		// means
																		// no
																		// breaking
																		// spaces
			fileContentsStr = fileContentsStr.replaceAll("&#(\\d+?);", "");

			fileContentsStr = fileContentsStr.replaceAll("\t", "");
			// fileContentsStr = fileContentsStr.replaceAll("(\n)+" , "\n");
			countHtmlPresChars =  tempGrossChars - fileContentsStr.length();
			tempGrossChars = fileContentsStr.length();
			logger.info("Removed reserved characters");	
			
			// For the fiscal year ended
						int fyear_start_index = fileContentsStr.toUpperCase().indexOf("YEAR ENDED");
						int fyear_end_index = fileContentsStr.indexOf("</B", fyear_start_index);
						fyear_end = fileContentsStr.substring(fyear_start_index+"YEAR ENDED".length(), fyear_end_index);
						if(fyear_end.contains("\n"))
							fyear_end = fyear_end.replace("\n", " ");
						fyear_end = fyear_end.trim();
						logger.info("fyear_end before================ :" + fyear_end);
						fyear_end = fyear_end.replaceAll("<(.*?)>", "");
						
						
						logger.info("fyear_end ================ :" + fyear_end);
			
			
			// Removing attributes for XBRL and related tags

			String finalText = "";
			String xbrlTagsList = "XBRL|";
			StringTokenizer xbrlTokens = new StringTokenizer(xbrlTagsList, "|");
			while (xbrlTokens.hasMoreElements()) {
				String tokenTemp = xbrlTokens.nextToken().toString();
				String reservedCharRegex = "<" + tokenTemp + "((.|\n+)*?)>";
				Pattern pattern = Pattern.compile(reservedCharRegex);
				Matcher matcher = pattern.matcher(fileContentsStr);
				String closingTag = "";
				while (matcher.find()) {
					String tempTag = matcher.group();
					int indexExcel = fileContentsStr.indexOf(tempTag);
					String finalTxtPart1 = fileContentsStr.substring(0,
							indexExcel);
					String tempTxt = fileContentsStr.substring(indexExcel);

					if (tempTag.indexOf(" ") >= 0) {
						closingTag = "</"
								+ tempTag.substring(1, tempTag.indexOf(" "))
								+ ">";
					} else {
						closingTag = "</" + tempTag.substring(1);
					}

					String finalTxtPart2 = tempTxt.substring(tempTxt
							.indexOf(closingTag) + (closingTag).length());

					finalText = finalTxtPart1 + finalTxtPart2;
					fileContentsStr = finalText;
				}
			}

			xbrlChars = tempGrossChars - fileContentsStr.length();
			tempGrossChars = fileContentsStr.length();

			// Point 5.5 Remove tables
			long timeRemoveTableStart = System.currentTimeMillis();
			fileContentsStr = removeTableTag(fileContentsStr);
			long timeRemoveTableStop = System.currentTimeMillis();
			tableChars = tempGrossChars - fileContentsStr.length();
			logger.info("Time taken to remove table = "
					+ (timeRemoveTableStop - timeRemoveTableStart) + "ms");
			// Point 5.5.1 Getting examples of files having where table tags are
			// used to demark the paragraphs of texts.
			// Removing the attributes of table tag

			tempGrossChars = fileContentsStr.length();
			// Removing reserved HTML characters

		

			// Removing HTML tags
//			logger.info(" intermediate result = " + fileContentsStr);
			String htmlTagsList = "DIV|TR|TD|FONT|t|a|html|body|th|p|span|head|link|script|br|B|I|hr|ul|dd|dt|dl|sup|u|strong|nobr|CAPTION|FN|S|C|"
					+ "small|center|h5|style|pre|big|title|h1|h2|h3|s|li|h4|sub|ol|dir|em|table";
			StringTokenizer htmlTagTokenizer = new StringTokenizer(
					htmlTagsList, "|");
			while (htmlTagTokenizer.hasMoreElements()) {
				String tokenTemp = htmlTagTokenizer.nextToken().toString()
						.trim();
				fileContentsStr = fileContentsStr.replaceAll("(?i)<"
						+ tokenTemp + "((.|\n+)*?)>", "");
				
				
				fileContentsStr = fileContentsStr.replaceAll("(?i)<"
						+ tokenTemp + "((.|\n+)*?)/>", "");
				
					

				fileContentsStr = fileContentsStr.replaceAll("(?i)</"
						+ tokenTemp + "((.|\n+)*?)>", "");
				
			}

			htmlChars = tempGrossChars - fileContentsStr.length();
			htmlChars = htmlChars + countHtmlPresChars;
			logger.info("Removed html characters");

			// Removing ASCII Encoded Segments for EXCEL, XML, PDF,
			String asciiEncodedSegmentsList = "TEXT|DOCUMENT|xsd|?xml";
			StringTokenizer asciiTokenizer = new StringTokenizer(
					asciiEncodedSegmentsList, "|");
			while (asciiTokenizer.hasMoreElements()) {
				String tokenTemp = asciiTokenizer.nextToken().toString();
				fileContentsStr = fileContentsStr.replaceAll("(?i)<"
						+ tokenTemp + "((.|\n+)*?)>", "");

				fileContentsStr = fileContentsStr.replaceAll("(?i)<"
						+ tokenTemp.toLowerCase() + "((.|\n+)*?)>", "");

				fileContentsStr = fileContentsStr.replaceAll("(?i)</"
						+ tokenTemp + ">", "");

			}

			// Point 5.10
			fileContentsStr = fileContentsStr.replaceAll("(\n)+-", ""); // 5.10.1
			fileContentsStr = fileContentsStr.replaceAll("- ", ""); // 5.10.2
			fileContentsStr = fileContentsStr
					.replaceAll("(?i)and/or", "and or"); // 5.10.3
			fileContentsStr = fileContentsStr.replaceAll("(--| \\.|==)\\s*", ""); // 5.10.4
			fileContentsStr = fileContentsStr.replaceAll("_", ""); // 5.10.5
			fileContentsStr = fileContentsStr.replaceAll("\\u0020+", " "); // 5.10.6
			fileContentsStr = fileContentsStr.replaceAll("(\n\\s*){3,}", "\n\n"); // 5.10.7
			fileContentsStr = fileContentsStr.replaceAll("(\n){3,}", "\n\n"); // Newly added rule by Daniel Bias
//			fileContentsStr = fileContentsStr.replaceAll("(^|[^\n])\n{1}(?!\n|\\s+)" , " "); // 5.10.8

			logger.info("Removed point 5.10 characters");

			// Few extra tunings - Not sure
			fileContentsStr = fileContentsStr.replaceAll("<>", "");
			fileContentsStr = fileContentsStr.replaceAll("<!((.|\n+)*?)>", ""); // Removing
																				// the
																				// html
																				// comments
			logger.info("Removed extra tuning characters");

			// Remove extra tags which are unclassified
			String otherTags = "DESCRIPTION|SECDOCUMENT|META|SEQUENCE|FILENAME|IMSDOCUMENT|LOSSPROVISION|EPSDILUTED|MULTIPLIER|LEGEND|PERIODTYPE|FISCALYEAREND|PERIODSTART"
					+ "|PERIODEND|CASH|SECURITIES|RECEIVABLES|ALLOWANCES|INVENTORY|CURRENTASSETS|DEPRECIATION|TOTALASSETS|OTHEREXPENSES|EXTRAORDINARY|OTHERSE|DISCONTINUED"
					+ "|F1|F2|F3|F4|F5|F6|F7|F8|F9|EPSPRIMARY|strike|NETINCOME|EPSBASIC|NAME|OTHEROPERATINGEXPENSES|OPERATINGINCOMELOSS|RETAINEDEARNINGS"
					+ "|EARNINGSAVAILABLEFORCOMM|OTHERPROPERTYANDINVEST|LONGTERMDEBTNET|OTHERASSETS|LONGTERMDEBTCURRENTPORT|LEASESCURRENT|OTHERITEMSCAPITALANDLIAB"
					+ "|OTHERINCOMENET|GROSSOPERATINGREVENUE|LONGTERMNOTESPAYABLE|RESTATED";
			StringTokenizer otherTagsTokenizer = new StringTokenizer(otherTags,
					"|");
			while (otherTagsTokenizer.hasMoreElements()) {
				String tokenTemp = otherTagsTokenizer.nextToken().toString()
						.trim();
				// logger.info("tokenTemp ="+ tokenTemp);
				// logger.info("tokenTemp=" + tokenTemp);
				fileContentsStr = fileContentsStr.replaceAll("(?i)<"
						+ tokenTemp + "((.|\n+)*?)>", "");
				fileContentsStr = fileContentsStr.replaceAll("(?i)</"
						+ tokenTemp + ">", "");
			}
			logger.info("Removed unclassified extra tags");

			// logger.info("Removed point unclassified extra tags"+fileContentsStr);

			// TODO Checking what all tags left
			/*Set<String> tagsSet = new HashSet();
			String searchTagRegex = "<((.|\n+)*?)>";
			Pattern pattern = Pattern.compile(searchTagRegex);
			Matcher matcher = pattern.matcher(fileContentsStr);
			Set<String> tagSet = new HashSet();
			logger.info("No. of matches = " + matcher.groupCount());
			while (matcher.find()) {
				tagSet.add(matcher.group());
			}
			logger.info("Tags lefts----- " + tagSet);*/

			// Remove SEC-HEADER and generate Header
			fileContentsStr = setHeaderInfo(fileContentsStr);

			// Parse the calcualations regarding text analysis

			netChars = fileContentsStr.length();
			netFileSize = writeToFile(fileContentsStr);
			calculationsStr.append(totalGrossChars + "|" + netChars + "|"
					+ asciiEncChars + "|" + htmlChars + "|" + xbrlChars + "|"
					+ tableChars + "|" + numPages + "|" + grossFileSize + "|"
					+ netFileSize + "|");
			// logger.info("*****index of Filed Date ="+ indexFiledDate);

			
			logger.info("Total characters in Gross file=" + totalGrossChars);
			time2 = System.currentTimeMillis();
			logger.info("Time taken for initial processing the file="
					+ (time2 - time1));
			processingTime = (time2 - time1) + "";			
			fc.close();
			logger.info("Completeinformation|" + compFileName + "|" + newParsedFileName + "|" + gvKey + "|"
					+ cik + "|" + fyear + "|" + fyear_end.trim() + "|" + gvKey + "_" + cik + "_" + fyear
					+ "|" + secHeaderLogStr.toString() + calculationsStr.toString()
					+ processingTime);
			logger.info("***************************************************");
		} catch (Exception e) {
			logger.error("Error is main fileParseCore for file " + compFileName
					+ "  |" + e);
			e.printStackTrace();
		}		
		return secHeaderLogStr.toString() + calculationsStr.toString()
				+ processingTime;
	}

	String setHeaderInfo(String rawText) {
		StringBuilder finalText = new StringBuilder();
		finalText.append("<HEADER><FileStatsLabels>" + grossFileSize + ","
				+ netChars + "," + asciiEncChars + "," + htmlChars + ","
				+ xbrlChars + "," + tableChars + "</FileStatsLabels>"
				+ "<FileStats>" + grossFileSize + "," + netChars + ","
				+ asciiEncChars + "," + htmlChars + "," + xbrlChars + ","
				+ tableChars + "</FileStats>" + "<SEC-HEADER>"
				+ secHeaderContents + "</SEC-HEADER>/<HEADER>" + rawText);

		return finalText.toString();
	}

	static String removeTableTag(String fileContentsStr) {

		String htmlstring = fileContentsStr;
		

		Pattern p = Pattern
				.compile("(?i)<table((.|\n+)*?)>((.|\n+)*?)</table>");
		// System.out.println(htmlstring.toLowerCase());
		Matcher m = p.matcher(htmlstring.toLowerCase());

		int counter = 1;
		int removedChars = 0;
		int startIndex = 0;
		int endIndex = 0;

		while (m.find()) {
			// get the matching group
			String codeGroup = m.group();

			// print the group
//			logger.info("Printing tables detected ======================================================= : "
//							+ counter++);
			// System.out.format("'%s'\n", codeGroup);
			if (!(codeGroup.toLowerCase().contains("item 7") || codeGroup
					.toLowerCase().contains("item 8"))) {
				// htmlstring.replace(codeGroup, "");
				// m.replaceAll("");
				startIndex = m.start();
				endIndex = m.end();
				//System.out.println("htmlString left : " + htmlstring.length());
				//System.out.println("start : " + startIndex + " end : " + endIndex);
				//System.out.println("removed chars here ===== : " + removedChars);
				int updatedStart = (startIndex - removedChars);
				int updatedEnd = (endIndex - removedChars);
				//System.out.println("updated start : " + updatedStart + " updated end : " + updatedEnd);
				//System.out.println("test table : " + htmlstring.substring(updatedStart, updatedStart+6));
				//String toBeReplaced = htmlstring.substring(startIndex - removedChars, endIndex - removedChars);
				//System.out.println("toBeReplaced : " + toBeReplaced);
				//String tempString = htmlstring.replaceFirst(toBeReplaced, "");
				String temp1 = htmlstring.substring(0, updatedStart);
				String temp2 = htmlstring.substring(updatedEnd,htmlstring.length());
				String tempString = temp1+temp2;
				removedChars += htmlstring.length() - tempString.length();
//				logger.info("removed chars: " + removedChars);
				htmlstring = tempString;
			}
		}
		return htmlstring;

	}

	static boolean checkForHtml() {
		boolean isPresent = false;

		return isPresent;
	}

	String escapeFileTypeSegments(String textRaw) {
		logger.info("inside escapeFileTypeSegments");
		String finalText = "";
		String availableFileTypes = "EXCEL|ZIP|GRAPHIC";
		StringTokenizer fileTypeTokens = new StringTokenizer(
				availableFileTypes, "|");
		while (fileTypeTokens.hasMoreElements()) {
			String tokenTemp = fileTypeTokens.nextToken().toString();
			int indexExcel = textRaw.indexOf("<TYPE>" + tokenTemp);
			while (indexExcel >= 0) {
				String finalTxtPart1 = textRaw.substring(0, indexExcel);

				String tempTxt = textRaw.substring(indexExcel);
				// logger.info("wth1  ..............."+finalTxtPart1);
				String finalTxtPart2 = tempTxt.substring(tempTxt
						.indexOf("</TEXT>") + "</TEXT>".length());
				// logger.info("wth2  ..............."+finalTxtPart2);
				// System.out.println("Removing " + tokenTemp + " \n " +
				// tempTxt);
				finalText = finalTxtPart1 + finalTxtPart2;
				textRaw = finalText;
				indexExcel = textRaw.indexOf("<TYPE>" + tokenTemp);
			}
		}

		// Removing PDF segments
		int indexPdf = textRaw.indexOf("<PDF>");
		if (indexPdf >= 0) {
			String finalTxtPart1 = textRaw.substring(0, indexPdf);
			String tempTxt = textRaw.substring(indexPdf);
			String finalTxtPart2 = tempTxt.substring(tempTxt.indexOf("</PDF>")
					+ "</PDF>".length());
			finalText = finalTxtPart1 + finalTxtPart2;
			textRaw = finalText;
		}

		// logger.info("removed ascii segments ="+ finalText);

		return textRaw;
	}

	static boolean checkForXBRL() {
		boolean isPresent = false;

		return isPresent;
	}

	static boolean checkForASCIIEnc() {
		boolean isPresent = false;

		return isPresent;
	}

	static double writeToFile(String byteBufferStr) {
		double size = 0;
		try {
			//System.out.println("tempFileName : " + compFileName);
			String[] tempNameArr = compFileName.split("_");
			// System.out.println("tempNameArr[0] : " + tempNameArr[0]);
			int lastIndex = tempNameArr[0].lastIndexOf("\\");
			tempNameArr[0] = tempNameArr[0].substring(lastIndex + 1);
			// System.out.println("tempNameArr[0] now : " + tempNameArr[0]);
			String newNameFile = "";
			for (int i = 0; i < tempNameArr.length - 1; i++) {
				newNameFile += tempNameArr[i] + "_";
				switch (i) {
				case 0:
					gvKey = tempNameArr[i];
					break;
				case 1:
					cik = tempNameArr[i];
					break;
				case 3:
					fyear = tempNameArr[i];
					break;
				}
			}
			newNameFile = newNameFile.substring(0, newNameFile.length() - 1);
			//System.out.println("newFileName : " + newNameFile);
			newNameFile = newNameFile.replace("__", "_");
			newParsedFileName = newNameFile;
			String dirPath = DEST_LOC + DEST_FOLDER + "/" + tempNameArr[1]
					+ "/";
			createDir(dirPath);
			final File fw = new File(dirPath + newNameFile + ".txt");
			BufferedWriter bfw = new BufferedWriter(new FileWriter(fw));
			// System.out.println("going to write : " + byteBufferStr);
			bfw.write(byteBufferStr);
			bfw.close();
			size = fw.length() / 1024.00;
			BigDecimal bd = new BigDecimal(size);
		    bd = bd.setScale(2, RoundingMode.HALF_UP);
		    size = bd.doubleValue();
		    logger.info("Net file size in KBs = " + size);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return size;
	}

	static String removeTags(String rawString) {
		String resultStr = "";

		return resultStr;
	}

	static HashMap<String, String> parseSecHeader(String secHeaderRaw) {
		HashMap<String, String> hmap = new HashMap<String, String>();
		char[] charArr = secHeaderRaw.toCharArray();
		StringBuilder tempStr = new StringBuilder();
		String isBusinessAddr = "false";
		String isMailAddr = "false";
		for (int i = 0; i < charArr.length; i++) {
			tempStr.append(charArr[i] + "");
			if (charArr[i] == '\n') {
				if (tempStr.toString().contains(":")) {
					String[] strArr = tempStr.toString().split(":");
					if (strArr.length > 1) {
						hmap.put(strArr[0].trim(), strArr[1].trim());
					}
				}
				tempStr.delete(0, tempStr.capacity() - 1);
			}
		}
		String businessAddr = "";
		if (secHeaderRaw.indexOf("BUSINESS ADDRESS") > -1) {
			isBusinessAddr = "true";
			if (secHeaderRaw.indexOf("BUSINESS PHONE") > -1) {
				int i = secHeaderRaw.indexOf("BUSINESS PHONE");
				char[] tempCharArr = secHeaderRaw.toCharArray();
				i++;
				while (tempCharArr[i] != '\n') {
					i++;
				}
				businessAddr = secHeaderRaw.substring(
						secHeaderRaw.indexOf("BUSINESS ADDRESS:"), i);
				logger.info(" Business address ------" + businessAddr);
			}
			else if (secHeaderRaw.indexOf("ZIP") > -1)
			{
				int i = secHeaderRaw.indexOf("ZIP");
				char[] tempCharArr = secHeaderRaw.toCharArray();
				i++;
				while (tempCharArr[i] != '\n') {
					i++;
				}
				businessAddr = secHeaderRaw.substring(
						secHeaderRaw.indexOf("BUSINESS ADDRESS:"), i);
				logger.info(" Business address ------" + businessAddr);
			
			}
		}

		// System.out.println("Business string="+businessAddr);
		hmap.put("isBusinessAddr", isBusinessAddr);
		hmap.putAll(convertStringtoHMap(businessAddr));

		String mailAddr = "";
		if (secHeaderRaw.indexOf("MAIL ADDRESS") > -1) {
			isMailAddr = "true";
			String mailAddrTemp = secHeaderRaw
					.substring(secHeaderRaw.indexOf("MAIL ADDRESS"),
							secHeaderRaw.length());
			if (mailAddrTemp.indexOf("ZIP") > -1) {
				int i = mailAddrTemp.indexOf("ZIP");
				char[] tempCharArr = mailAddrTemp.toCharArray();
				i++;
				while (tempCharArr[i] != '\n') {
					i++;
				}
				mailAddr = mailAddrTemp.substring(
						mailAddrTemp.indexOf("MAIL ADDRESS"), i);
				logger.info(" Mail address ------" + mailAddr);
			}
		}
		// System.out.println("Mail address string=" + mailAddr);
		hmap.put("isMailAddr", isMailAddr);
		hmap.putAll(convertStringtoHMap(mailAddr));

		logger.info("hashmap ----is" + hmap);
		return hmap;
	}

	static HashMap<String, String> convertStringtoHMap(String text) {
		HashMap<String, String> tempHMap = new HashMap<String, String>();
		text = text + "\n";
		try {
			char[] charArr = text.toCharArray();
			boolean isBusiness = false;
			boolean isMail = false;
			if (text.indexOf("BUSINESS ADDRESS:") >= 0)
				isBusiness = true;
			else if (text.indexOf("MAIL ADDRESS:") >= 0)
				isMail = true;
			StringBuilder tempStr = new StringBuilder();
			for (int i = 0; i < charArr.length; i++) {
				tempStr.append(charArr[i] + "");
				if (charArr[i] == '\n') {
					if (tempStr.toString().contains(":")) {
						String[] strArr = tempStr.toString().split(":");
						if (strArr.length > 1) {
							String prefix = "";
							if (isBusiness) {
								prefix = "BUSINESS_";
							} else if (isMail) {
								prefix = "MAIL_";
							}
							tempHMap.put(prefix + strArr[0].trim(),
									strArr[1].trim());
						}
					}
					tempStr.delete(0, tempStr.capacity() - 1);
				}
			}
			System.out.println("hashmap for subsections is" + tempHMap);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error in convertStringtoHMap for file "
					+ compFileName);
		}
		return tempHMap;
	}

	static void createDir(String directoryName) {
		File theDir = new File(directoryName);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			// System.out.println("creating directory: " + directoryName);
			boolean result = false;

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if (result) {
				logger.info("DIR created");
			}
		}
	}
	
	int getFyear2(String conformedYrStr)
	{
		int year = 0;
		try {
			int month = Integer.parseInt(conformedYrStr.substring(4, 6));
			year =  Integer.parseInt(conformedYrStr.substring(0, 4));
			if ( month<= 5 )
			{
				year = year - 1;
			}
			
			logger.info(" fyear2 is " + year);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return year;
	}

}