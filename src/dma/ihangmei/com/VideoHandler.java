package dma.ihangmei.com;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import dma.ihangmei.com.utils.FileUtil;
import dma.ihangmei.com.utils.GsonUtil;
import dma.ihangmei.com.utils.Utils;


public class VideoHandler {

	public static CheckResult checkResult = null;
	public final static DateFormat dfSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public final static String[] paramItems = {"file","image","image_hori","picture1"};

	public static CheckResult checkVideo(String portalRoot,String modelRoot) {
		if (portalRoot == null)
			return checkResult;
		checkResult = new CheckResult();
		String videoTargetRoot = checkResult.portalIndexRoot = portalRoot +"/data/" + modelRoot;
		// 统计增电影资源校验所耗时间
		checkResult.checkStartDate = dfSimple.format(new Date(System.currentTimeMillis()));


		// 判断目的 video 路径
		if (!new File(videoTargetRoot).exists()) {
			checkResult.reports.add(String.format("target path:%s/data is not exist!", videoTargetRoot));
			return checkResult;
		}
		if(checkResult.sourceNumbers.get(modelRoot) == null) checkResult.sourceNumbers.put(modelRoot, new int[2]);
		
		try {
			// 收集数据
			List<Map<String, Object>> listData =  Utils.folderChildFilesToListMap(new File(videoTargetRoot + "/list"));
			int totalCount = checkResult.sourceNumbers.get(modelRoot)[0] = listData.size();
			// 缓存设备电影所以detail文件
			Map<String, File> detailFiles = new HashMap<>();
			FileUtil.iteratorFile(detailFiles, new File(videoTargetRoot + "/detail/"), false,true);
			// 校验
			boolean needBackUp = false;
			Iterator<Map<String, Object>> iterator = listData.iterator();
			while (iterator.hasNext()) {
				Map<String, Object> map = iterator.next();
				if(map.get("createTime") == null || map.get("id") == null) continue;
				String detailKey = "";
				Utils.dfDayOnlyNoSeparator.setTimeZone(TimeZone.getTimeZone("GMT+8"));
				if((map.get("createTime")+"").toUpperCase().contains("E") || (map.get("createTime")+"").contains(".")){
					Date createTime = new Date(Long.parseLong(new BigDecimal((double)map.get("createTime")).toPlainString()));
					detailKey = Utils.dfDayOnlyNoSeparator.format(createTime) + File.separator + map.get("id");
				}else
					detailKey = Utils.dfDayOnlyNoSeparator.format(new Date(Long.parseLong(map.get("createTime") + ""))) + File.separator + map.get("id");
				File detailFile = detailFiles.get(detailKey);
				if (detailFile == null) {
					needBackUp = true;
					iterator.remove();
					continue;
				}

				Map<String, Object> detailMap = GsonUtil.getMap(GsonUtil.getStrFromFile(detailFile));
				for (String key : paramItems) {
					if (detailMap.get(key) != null && !new File(Utils.getAbsolutePath(portalRoot, (String) detailMap.get(key))).exists()) {
						iterator.remove();
						needBackUp = true;
						break;
					}
				}

			}
			checkResult.sourceNumbers.get(modelRoot)[1] = totalCount - listData.size();
			// 重新写到设备中
			if ( listData.size() > 0 && needBackUp ){
				// 先备份一份,完成后删除
				Utils.backUp(videoTargetRoot, videoTargetRoot+"_bak", modelRoot);
				
				Utils.reWritePageList(videoTargetRoot + "/list/", 30, listData);
			}
		} catch (Exception e) {
			checkResult.reports.add(Utils.getExInfo("", e));
			return checkResult;
		}

		checkResult.checkEndDate = dfSimple.format(new Date(System.currentTimeMillis()));
		checkResult.isSuccess = true;
		return checkResult;
	}
}
