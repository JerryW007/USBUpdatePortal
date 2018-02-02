package dma.ihangmei.com;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dma.ihangmei.com.utils.FileUtil;
import dma.ihangmei.com.utils.StrUtil;
import dma.ihangmei.com.utils.Utils;

public class NewsHandler {

	public final static String[] paramItems = {"file","picture1"};
	
	/**
	 * 清理过老的新闻数据
	 * 暂设定7天以前的数据
	 */
	public static void cleanUpNewsData(String portalRoot) {
		String newsRoot = portalRoot + "/data/news";
		int count = 0; // 去除老旧新闻数量
		try{
			File newsListFile = new File(newsRoot + "/list");
			if( !newsListFile.exists() || newsListFile.listFiles().length <= 0 ) {
				System.out.println("can't find "+newsRoot + "/list");
				return;
			}
			// 缓存detail文件
			File newsDetailFile =  new File(newsRoot + "/detail");
			if( !newsDetailFile.exists() || newsDetailFile.listFiles().length <= 0 ){
				System.out.println("can't find "+newsRoot + "/detail");
				return;
			}
			Map<String,File> allDetailFiles = new HashMap<>();// key=文件名称,value=文件 直接可以按名字删除文件
			FileUtil.iteratorFile(allDetailFiles, newsDetailFile, false);
			// 七天前的时间戳
			long sevenDayAgoStamp = System.currentTimeMillis() - 7 * 24 * 60 * 60  * 1000L;
			// 新闻list子文件夹
			List<Map<String,Object>> listChildFileData = null;
			for( File listChildFolder : newsListFile.listFiles() ){
				listChildFileData = Utils.folderChildFilesToListMap(listChildFolder);
				if( listChildFileData == null || listChildFileData.size() <= 0 )
					continue;
				// 删除 list-->createTime 小于七天前的数据
				Iterator<Map<String,Object>> iter = listChildFileData.iterator();
				while(iter.hasNext()){
					Map<String,Object> item = iter.next();
					if( !StrUtil.isEmpty(item.get("createTime").toString()) ) {
						//&& Long.parseLong(item.get("createTime").toString()) < sevenDayAgoStamp 
						String timeStamp = item.get("createTime").toString();
						if(timeStamp.toUpperCase().contains("E") ) {
							timeStamp = new BigDecimal(timeStamp).toPlainString();
						}
						if(Long.parseLong(timeStamp) < sevenDayAgoStamp ) {
							if( item.get("id") != null && allDetailFiles.get(item.get("id")) != null )
								allDetailFiles.get(item.get("id")).delete();// 删除list相对应的老旧的detail文件
							iter.remove();
							count++;
						}
					}
				}
				// 重写设备文件
				Utils.reWritePageList(listChildFolder.getAbsolutePath(), 15, listChildFileData);//新闻模块暂定每页15条记录
			}
		}catch(Exception e){
			System.out.println("Exception:" + Utils.getExInfo("", e));
			return;
		}
		System.out.println(String.format("delete old news success! delete:%d,date:",count,Utils.dfSimple.format(new Date())));
	}
	
}
