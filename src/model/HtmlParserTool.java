package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlParserTool {
	private static Logger logger = Logger.getLogger(HtmlParserTool.class);
	
	public Set<String> extracLinksByBody(String body,String url,LinkFilter filter,String enCode) {
		String host = getHost(url);
		Set<String> links = new HashSet<String>();
		try {
			//Parser parser = new Parser(url);
			Parser parser = null;
			try {
				//parser = Parser.createParser(body, enCode);				
				parser = new Parser();
				parser.setInputHTML(body);
				parser.setEncoding(enCode);
				
			} catch (NullPointerException e) {
				parser=null;
				logger.error(e);
			}
			  
			//parser.setEncoding("utf-8");
			
			// 过滤 <frame >标签的 filter，用来提取 frame 标签里的 src 属性所表示的链接
			NodeFilter frameFilter = new NodeFilter() {
				public boolean accept(Node node) {
					if (node.getText().startsWith("frame src=")) {
						return true;
					} else {
						return false;
					}
				}
			};
			// OrFilter 来设置过滤 <a> 标签，和 <frame> 标签
			OrFilter linkFilter = new OrFilter(new NodeClassFilter(
					LinkTag.class), frameFilter);
			// 得到所有经过过滤的标签
			NodeList list = parser.extractAllNodesThatMatch(linkFilter);					
			for (int i = 0; i < list.size(); i++) {
				Node tag = list.elementAt(i);
				if (tag instanceof LinkTag)// <a> 标签
				{
					LinkTag link = (LinkTag) tag;
					String linkUrl = link.getLink();// url
					//String title = link.getStringText();
					String title = link.getLinkText();
					//System.out.println(title);
					title = title.trim();
					if(!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://") ) {
						if(linkUrl.startsWith("/")){
							linkUrl = host+linkUrl;
						}else {
							linkUrl = host+ "/" + linkUrl;
						}
					}
					if(filter.accept(linkUrl)&&!title.equals("官方微信"))//过滤搜狗官方微信link
						links.add(linkUrl);
				} else// <frame> 标签
				{
		        // 提取 frame 里 src 属性的链接如 <frame src="test.html"/>
					String frame = tag.getText();
					int start = frame.indexOf("src=");
					frame = frame.substring(start);
					int end = frame.indexOf(" ");
					if (end == -1)
						end = frame.indexOf(">");
					String frameUrl = frame.substring(5, end - 1);					
					if(filter.accept(frameUrl))
						links.add(frameUrl);
				}
			}
			parser=null;
		} catch (ParserException e) {
			//e.printStackTrace();
			logger.error(e);
		}
		return links;
	}
	private String getHost(String url) {
		int flag = -1;
		if(url.startsWith("http://")) {
			url = url.replace("http://", "");
			flag = 0;
		}
		if(url.startsWith("https://")) {
			url = url.replace("https://", "");
			flag = 1;
		}
		String host = "";
		int index = url.indexOf("/");
		if(index==-1) {
			host = url;
		} else {
			host = url.substring(0,index);
		}
		String addString = flag==1?"https://":"http://";
		host = addString + host;
		return host;
	}

	public static String readTxtFile(String filePath,String enCode){
		String body="";
        try {
        		
                String encoding=enCode;
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                        //System.out.println(lineTxt);
                        body+=lineTxt;
                    }
                    read.close();
                    
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return body;
    }

	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		String startUrl = "http://weibo.cn";
		String body ="";
		HtmlParserTool htmlparser = new HtmlParserTool();
		body=readTxtFile("6.html","ISO8859-1");
		System.out.println(body);
		
		
		/*String LongtextUrl="";
		System.out.println("var url = \"http://weibo.com");
		int indexOfLongtextUrlStringStart=body.indexOf("var url = \"http://weibo.com");
		int indexOfLongtextUrlStringEnd=-1;
		if( indexOfLongtextUrlStringStart>=0)
		{
			indexOfLongtextUrlStringEnd=body.indexOf("\";",indexOfLongtextUrlStringStart);
			if(indexOfLongtextUrlStringEnd>=0)
			{
				LongtextUrl=body.substring(indexOfLongtextUrlStringStart, indexOfLongtextUrlStringEnd);
			}
		}
		System.out.println(LongtextUrl);
		LongtextUrl=LongtextUrl.replaceAll("var url = \"", "");
		System.out.println(LongtextUrl);*/
		
		/*Set<String> links = htmlparser.extracLinksByBody(body,startUrl,new LinkFilter()
		{
			//提取以 http://www.twt.edu.cn 开头的链接
			public boolean accept(String url) {
				//if(url.startsWith("http://www.sina.com.cn/"))
					return true;
				//else
					//return false;
			}
			
		},"utf-8");
		for(String link : links)
			System.out.println(link);*/
	}

}
