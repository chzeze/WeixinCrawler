package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.util.UriEncoder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import model.HtmlParserTool;
import model.LinkFilter;

/**
 * 
 * @ClassName: crawlWeixinMain
 * @Description: 搜狗微信采集
 * @author zeze
 * @date 2017年4月1日 下午2:50:26
 *
 */
public class crawlWeixinMain {
	private static Logger logger = Logger.getLogger(crawlWeixinMain.class);
	private static WebClient webClient;
	private static String host = "http://weixin.sogou.com/";
	private static String savePath = "f:/saveWeixin/";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static int sleepTime = 8000;
	private static int randomTime = 3000;

	public static void main(String[] args) {
		String keyword = "xyzqfzfgs";
		int type = 2;// 1表示采集公众号,2表示采集文章
		if (type == 1)
			searchWeixinAccounts(keyword);
		else if (type == 2)
			searchWeixinArticles(keyword);
	}

	/**
	 * 初始化webclient header
	 */
	private static WebClient getWebClient() {
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getOptions().setTimeout(20000);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		// webClient.getOptions().setJavaScriptEnabled(false);
		webClient.addRequestHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		webClient.addRequestHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		webClient.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.8");
		webClient.addRequestHeader("Accept-Encoding", "gzip, deflate, sdch");
		webClient.addRequestHeader("Connection", "keep-alive");
		webClient.addRequestHeader("Upgrade-Insecure-Requests", "1");
		webClient.addRequestHeader("Cache-Control", "max-age=0");
		webClient.addRequestHeader("Host", "weixin.sogou.com");
		return webClient;
	}

	/**
	 * @Title: searchWeixinAccounts 根据关键词搜索微信公众号
	 */
	private static void searchWeixinAccounts(String keyword) {
		keyword = UriEncoder.encode(keyword);
		System.out.println("关键词：" + keyword);
		String url = "http://weixin.sogou.com/weixin?type=1&s_from=input&query=" + keyword
				+ "&ie=utf8&_sug_=n&_sug_type_=";
		// logger.info(url);
		WebClient webClient = getWebClient();
		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
			Thread.sleep(sleepTime + new Random().nextInt(randomTime));
		} catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (InterruptedException e) {
			logger.error(e);
		}
		HtmlParserTool htmlparser = new HtmlParserTool();
		// System.out.println(page.asXml());

		// 保存该页面page.asXml
		savePage(page.asXml(), keyword, 1, 0);

		Set<String> links = htmlparser.extracLinksByBody(page.asXml(), url, new LinkFilter() {
			public boolean accept(String url) {
				return true;
			}
		}, "utf-8");

		webClient.addRequestHeader("Host", "mp.weixin.qq.com");// 重新设置头文件
		for (String link : links) {

			if (link.contains("/mp.weixin.qq.com/profile")) {// 抽取得到一个微信公众号
				link = link.replaceAll("&amp;", "&");
				System.out.println("搜索得到的公众号URL:" + link);
				try {
					page = webClient.getPage(link);
					Thread.sleep(sleepTime + new Random().nextInt(randomTime));
				} catch (FailingHttpStatusCodeException e) {
					logger.error(e);
				} catch (MalformedURLException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				} catch (InterruptedException e) {
					logger.error(e);
				}
				// System.out.println(page.asXml());

				// 保存该页面page.asXml
				savePage(page.asXml(), keyword, 1, 1);

				int indexMsgList = page.asXml().indexOf("var msgList =");
				int indexSeajs = page.asXml().indexOf("seajs.use(");
				if (indexMsgList != -1 && indexSeajs != -1) {
					String msgList = page.asXml().substring(indexMsgList + 13, indexSeajs - 10);
					// System.out.println(msgList);
					try {
						JSONObject obj = new JSONObject(msgList);
						String listStr = obj.getString("list");
						// System.out.println("listStr:" + listStr);
						JSONArray listArray = new JSONArray(listStr);
						// System.out.println("list size=" +
						// listArray.length());
						for (int i = 0; i < listArray.length(); i++) {
							JSONObject listObj = listArray.getJSONObject(i);
							String app_msg_ext_info_Str = listObj.getString("app_msg_ext_info");
							// System.out.println("app_msg_ext_info_Str : " +
							// app_msg_ext_info_Str);

							JSONObject appObj = new JSONObject(app_msg_ext_info_Str);
							String appUrlStr = "http://mp.weixin.qq.com/"
									+ appObj.getString("content_url").replaceAll("&amp;", "&");
							;
							String appTitleStr = appObj.getString("title");
							System.out.println(i + " app_Title:" + appTitleStr + " " + appUrlStr);

							try {
								page = webClient.getPage(appUrlStr);
								Thread.sleep(sleepTime + new Random().nextInt(randomTime));
							} catch (FailingHttpStatusCodeException e) {
								logger.error(e);
							} catch (MalformedURLException e) {
								logger.error(e);
							} catch (IOException e) {
								logger.error(e);
							} catch (InterruptedException e) {
								logger.error(e);
							}
							// System.out.println(page.asXml());
							// 保存该页面page.asXml
							savePage(page.asXml(), keyword, 1, 2);

							String multi_app_msg_item_list_Str = appObj.getString("multi_app_msg_item_list");
							// System.out.println("multi_app_msg_item_list_Str :
							// "+multi_app_msg_item_list_Str);
							JSONArray multiArray = new JSONArray(multi_app_msg_item_list_Str);
							// System.out.println("multi size=" +
							// multiArray.length());
							for (int j = 0; j < multiArray.length(); j++) {
								JSONObject multiObj = multiArray.getJSONObject(j);
								String multiUrl = "http://mp.weixin.qq.com"
										+ multiObj.getString("content_url").replaceAll("&amp;", "&");
								String multiTitle = multiObj.getString("title");
								System.out.println(j + " multi_Title" + multiTitle + " " + multiUrl);
								try {
									page = webClient.getPage(multiUrl);
									Thread.sleep(sleepTime + new Random().nextInt(randomTime));
								} catch (FailingHttpStatusCodeException e) {
									logger.error(e);
								} catch (MalformedURLException e) {
									logger.error(e);
								} catch (IOException e) {
									logger.error(e);
								} catch (InterruptedException e) {
									logger.error(e);
								}
								// System.out.println(page.asXml());
								// 保存该页面page.asXml
								savePage(page.asXml(), keyword, 1, 2);
							}
						}

					} catch (JSONException e) {
						System.out.println(e);
					}
				} else {
					logger.error("异常页面：" + page.asXml());
				}

			}
		}

	}

	/**
	 * @Title: searchWeixinArticles 根据关键词搜微信文章
	 */
	private static void searchWeixinArticles(String keyword) {
		keyword = UriEncoder.encode(keyword);
		System.out.println("关键词：" + keyword);
		String url = "http://weixin.sogou.com/weixin?type=2&s_from=input&query=" + keyword
				+ "&ie=utf8&_sug_=n&_sug_type_=";
		WebClient webClient = getWebClient();
		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
//			Thread.sleep(sleepTime + new Random().nextInt(randomTime));
		} catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
//		} catch (InterruptedException e) {
//			logger.error(e);
//		}
		HtmlParserTool htmlparser = new HtmlParserTool();
		// System.out.println(page.asXml());

		// 保存该页面page.asXml
		savePage(page.asXml(), keyword, 2, 0);

		Set<String> links = htmlparser.extracLinksByBody(page.asXml(), url, new LinkFilter() {
			public boolean accept(String url) {
				return true;
			}
		}, "utf-8");

		webClient.addRequestHeader("Host", "mp.weixin.qq.com");// 重新设置头文件
		for (String link : links) {
			if (link.contains("/mp.weixin.qq.com/s?")) {// 抽取得到一个微信公众号
				link = link.replaceAll("&amp;", "&");
				System.out.println("搜索得到的文章URL:" + link);
				logger.info("搜索得到的文章URL:" + link);
				try {
					page = webClient.getPage(link);
					Thread.sleep(sleepTime + new Random().nextInt(randomTime));
				} catch (FailingHttpStatusCodeException e) {
					logger.error(e);
				} catch (MalformedURLException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				} catch (InterruptedException e) {
					logger.error(e);
				}
				// System.out.println(page.asXml());

				// 保存该页面page.asXml
				savePage(page.asXml(), keyword, 2, 1);
			}
		}
		
	}

	/**
	 * 保存目录：关键词/采集时间/type/deep/FormatDate.html 根据关键词采集深度和采集类型保存页面
	 * 
	 * @Title: savePage
	 * @param @param
	 *            page 页面
	 * @param @param
	 *            type 微信采集Type 1表示公众号，2表示采集文章
	 * @param @param
	 *            deep 根据采集深度保存页面
	 * @param @param
	 *            keyword 关键词
	 * @return void 返回类型
	 */
	private static void savePage(String page, String keyword, int type, int deep) {

		long start = System.currentTimeMillis();
		String path = null;
		File file2 = null;
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddHH");

		String outputpath = savePath + "KeyWord-" + keyword + "/";
		file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();
		outputpath = outputpath + "Time-" + dateFormat1.format(new Date()) + "/";
		file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();
		outputpath = outputpath + "Type-" + type + "/";
		file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();
		outputpath = outputpath + "Deep-" + deep + "/";
		file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();

		path = new String(outputpath + dateFormat.format(new Date()) + "_D." + deep + "_T" + type + ".html");

		file2 = new File(path);

		FileOutputStream outputStream;

		try {
			outputStream = new FileOutputStream(file2);
			outputStream.write(page.getBytes());
			start = System.currentTimeMillis();
			outputStream.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

}
