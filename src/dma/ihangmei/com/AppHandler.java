package dma.ihangmei.com;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dma.ihangmei.com.utils.FileUtil;
import dma.ihangmei.com.utils.GsonUtil;
import dma.ihangmei.com.utils.Utils;

public class AppHandler {

	public static CheckResult checkResult = new CheckResult();
	public final static DateFormat dfSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public final static String[] paramItems = {"logo","file","picture1","picture2","picture3","picture4","picture5"};
	
	public static CheckResult checkApps(String portalRoot) {
		if (portalRoot == null) return checkResult;

		String apkTargetRoot = checkResult.portalIndexRoot = portalRoot + "/data/apps";
		// 统计增电影资源校验所耗时间
		checkResult.checkStartDate = dfSimple.format(new Date(System.currentTimeMillis()));

		// 判断目的 apk 路径
		if (!new File(apkTargetRoot).exists()) {
			checkResult.reports.add(String.format("目的路径  %s 下索引文件夹不存在", apkTargetRoot));
			return checkResult;
		} 

		try {
			checkListChildFolder(portalRoot, "apps", "1200","games");
			checkListChildFolder(portalRoot, "apps", "1201","apks");
		} catch (Exception e) {
			checkResult.reports.add(Utils.getExInfo("", e));
			// 恢复备份文件
			e.printStackTrace();
			return checkResult;
		}
		checkResult.checkEndDate = dfSimple.format(new Date(System.currentTimeMillis()));
		checkResult.isSuccess = true;
		return checkResult;
	}
	public static void checkListChildFolder(String portalRoot,String apkFolderName,String listChildName,String remark){
		if(checkResult.sourceNumbers.get(remark) == null){
			checkResult.sourceNumbers.put(remark, new int[2]);
		}
		File checkListChildFolder = new File(portalRoot + "/data/" + apkFolderName + "/list/" + listChildName);
		// 收集数据
		List<Map<String, Object>> listData = Utils.folderChildFilesToListMap(checkListChildFolder);
		int totalCount = listData.size();
		checkResult.sourceNumbers.get(remark)[0]  += listData.size();
		
		// 缓存设备APPS所以detail文件
		String apkTargetRoot = portalRoot + "/data/" + apkFolderName;
		Map<String, File> detailFiles = new HashMap<>();
		FileUtil.iteratorFile(detailFiles, new File(apkTargetRoot + "/detail/"), false,true);
		
		// 校验
		boolean needBackUp = false;
		Iterator<Map<String, Object>> iterator = listData.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> map = iterator.next();
			String[] pathNodes = (map.get("fullUrl") + "").split(File.separator);
			if(pathNodes.length <= 2) continue;
			File detailFile = detailFiles.get(pathNodes[pathNodes.length - 2] + File.separator + pathNodes[pathNodes.length - 1]);
			if (detailFile == null) {
				needBackUp = true;
				iterator.remove();
				continue;
			}

			Map<String, Object> detailMap = GsonUtil.getMap(GsonUtil.getStrFromFile(detailFile));
			String absolutePath = null;
			for(String paramKey : paramItems) {
				try{
					if(detailMap.get(paramKey) != null){
						absolutePath = Utils.getAbsolutePath(portalRoot, (String) detailMap.get(paramKey));
						if(!new File(absolutePath).exists()) {
							iterator.remove();
							needBackUp = true;
							break;
						}
					}
				}catch(Exception e){}
			}
		}
		checkResult.sourceNumbers.get(remark )[1] +=  totalCount - listData.size();
		checkResult.reports.add(listChildName + " listTotal:"+ totalCount + " remove:"+ (totalCount - listData.size()));
		// 重新写到设备中
		if (listData.size() > 0 && needBackUp){
			// 先备份一份,完成后删除
			Utils.backUp(apkTargetRoot, apkTargetRoot + "_bak", "apps");
			
			Utils.reWritePageList(apkTargetRoot + "/list/" + listChildName, 15, listData);
		}
	}
}
