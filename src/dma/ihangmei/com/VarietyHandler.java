package dma.ihangmei.com;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dma.ihangmei.com.utils.FileUtil;
import dma.ihangmei.com.utils.GsonUtil;
import dma.ihangmei.com.utils.Utils;

public class VarietyHandler {
	
	public static CheckResult checkResult = new CheckResult();
	public final static DateFormat dfSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static class ListJson{
		 public String createTime; //"20170327",
		 public String id; //"4962",
		 public String name; //"《真心英雄》第一季",
		 public String picture1; //"cms/variety/20170327/zxyx.jpg"
	}
	public static class Chapter{
		public String createTime; //"20170327",
		public String file; //"cms/variety/20170327/zxyx_1.mp4",
		public String name; //"真心英雄第一期"
	}
	
	public static class DetailJson{
		public List<Chapter> chapter = new ArrayList<>();
		public String createTime; //"20170327",
		public String description; //"《真心英雄》作为一档原创大型户外推理真人秀，设置了探秘、推理、竞技等多元对抗模式，明星将分队闯过重重难关争得胜利。六位固定成员分别是张杰、陈学冬、佟大为、郑元畅、杨坤、朱亚文，节目一般每一期会有不同的明星嘉宾加盟。",
		public String name; //"《真心英雄》第一季",
		public String type; //"娱乐"
	}
	
	public static CheckResult checkVariety(String portalRoot){
		if (portalRoot == null)
			return checkResult;
		
		String varietyTargetRoot = checkResult.portalIndexRoot = portalRoot + "/data/variety";
		// 统计增电影资源校验所耗时间
		checkResult.checkStartDate = dfSimple.format(new Date(System.currentTimeMillis()));
		
		// 判断目的 variety 路径
		if (!new File(varietyTargetRoot).exists()) {
			checkResult.reports.add(String.format("目的路径  %s 下 data 文件夹找不到", varietyTargetRoot));
			return checkResult;
		}
		if(checkResult.sourceNumbers.get("variety") == null) checkResult.sourceNumbers.put("variety", new int[2]);
		
		try {
			// 收集数据
			List<Map<String, Object>> listData = new ArrayList<>();
			listData.addAll(GsonUtil.getListMap(GsonUtil.getStrFromFile(varietyTargetRoot + "/list/1")));
			int totalCount = checkResult.sourceNumbers.get("variety")[0] = listData.size();
			// 缓存设备电影所以detail文件
			Map<String, File> detailFiles = new HashMap<>();
			FileUtil.iteratorFile(detailFiles, new File(varietyTargetRoot + "/detail/"), false);
			// 校验
			boolean needBackUp = false;
			Iterator<Map<String, Object>> iterator = listData.iterator();
			while (iterator.hasNext()) {
				Map<String, Object> map = iterator.next();
				//判断list对应的detail文件存不存在,不存在,删除对应的list索引
				File detailFile = detailFiles.get(map.get("id") + "");
				if (detailFile == null) {
					needBackUp = true;
					iterator.remove();
					continue;
				}
				//判断综艺列表的展示图存不存在
				if(map.get("picture1") != null && !new File(Utils.getAbsolutePath(portalRoot, map.get("picture1").toString())).exists() ) {
					needBackUp = true;
					iterator.remove();
					continue;
				}
				//判断综艺的章节存不存在
				if(!checkChapters(portalRoot, detailFile)) {
					needBackUp = true;
					iterator.remove();
				}
			}
			checkResult.sourceNumbers.get("variety")[1] = totalCount - listData.size();
			// 重新写到设备中
			if ( listData.size() > 0 && needBackUp ){
				// 先备份一份,完成后删除
				Utils.backUp(varietyTargetRoot, varietyTargetRoot+"_bak", "variety");
				
				Utils.reWritePageList(varietyTargetRoot + "/list/", 0, listData);
			}
		} catch (Exception e) {
			checkResult.reports.add(Utils.getExInfo("", e));
			return checkResult;
		}

		checkResult.checkEndDate = dfSimple.format(new Date(System.currentTimeMillis()));
		checkResult.isSuccess = true;
		return checkResult;
	}
	
	
	public static boolean checkChapters(String portalRoot,File detailFile){
		if(portalRoot == null || detailFile == null  || !detailFile.exists()) return false;
		try{
			DetailJson detail = GsonUtil.gson.fromJson(GsonUtil.getStrFromFile(detailFile),DetailJson.class);
			for (Chapter chapter : detail.chapter) {
				if (chapter.file != null && !new File(Utils.getAbsolutePath(portalRoot, chapter.file)).exists()) 
					return false;
			}
		}catch(Exception e){checkResult.reports.add(Utils.getExInfo("", e));}
		return true;
	}
}
