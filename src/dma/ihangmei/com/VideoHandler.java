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

public class VideoHandler {

	public static CheckResult checkResult = new CheckResult();
	public final static DateFormat dfSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public final static String[] paramItems = {"file","picture1"};

	public static CheckResult checkVideo(String portalRoot) {
		if (portalRoot == null)
			return checkResult;

		String videoTargetRoot = checkResult.portalIndexRoot = portalRoot + "/data/video";
		// 统计增电影资源校验所耗时间
		checkResult.checkStartDate = dfSimple.format(new Date(System.currentTimeMillis()));


		// 判断目的 video 路径
		if (!new File(videoTargetRoot).exists()) {
			checkResult.reports.add(String.format("目的路径  %s 下 data 文件夹找不到", videoTargetRoot));
			return checkResult;
		}
		if(checkResult.sourceNumbers.get("video") == null) checkResult.sourceNumbers.put("video", new int[2]);
		
		try {
			// 收集数据
			List<Map<String, Object>> listData = new ArrayList<>();
			listData.addAll(GsonUtil.getListMap(GsonUtil.getStrFromFile(videoTargetRoot + "/list/1")));
			int totalCount = checkResult.sourceNumbers.get("video")[0] = listData.size();
			// 缓存设备电影所以detail文件
			Map<String, File> detailFiles = new HashMap<>();
			FileUtil.iteratorFile(detailFiles, new File(videoTargetRoot + "/detail/"), false);
			// 校验
			boolean needBackUp = false;
			Iterator<Map<String, Object>> iterator = listData.iterator();
			while (iterator.hasNext()) {
				Map<String, Object> map = iterator.next();
				File detailFile = detailFiles.get(map.get("id") + "");
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
			checkResult.sourceNumbers.get("video")[1] = totalCount - listData.size();
			// 重新写到设备中
			if ( listData.size() > 0 && needBackUp ){
				// 先备份一份,完成后删除
				Utils.backUp(videoTargetRoot, videoTargetRoot+"_bak", "video");
				
				Utils.reWritePageList(videoTargetRoot + "/list/", 0, listData);
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
