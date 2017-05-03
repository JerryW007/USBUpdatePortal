package dma.ihangmei.com.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.reflect.TypeToken;

public class Utils {
	
	public final static DateFormat dfSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public final static DateFormat dfDayOnlyNoSeparator = new SimpleDateFormat("yyyyMMdd");
	private Utils(){};
	
	public static String getExInfo(String s, Throwable e) {
    	try (StringWriter sw = new StringWriter()) {  
            PrintWriter pw = new PrintWriter(sw);  
            e.printStackTrace(pw);  
        	return String.format("%s: %s\n%s\n", 
        			s == null? "" : s, e.getMessage() == null? "" : e.getMessage(), sw.toString());
        } catch (Exception ex) {}
		return s;
	}
	
	public static void reWritePageList(String parentFolderPath,int listNumber,List<Map<String,Object>> data){
		File parentFolder = new File(parentFolderPath);
		
		FileUtil.removeAllFiles(parentFolderPath, false);
		if( !parentFolder.exists() ) parentFolder.mkdirs();
		if(listNumber == 0){ //不分页
			FileUtil.writeFile(parentFolderPath + File.separator + "1", data);
		}else{
			for( int page=0;page * listNumber <= data.size();page++ ){
				int endIndex = (page + 1 ) * listNumber - 1 >= data.size() ? data.size() : (page + 1 ) * listNumber;
				List<Map<String,Object>> temp = data.subList(page * listNumber, endIndex);
				if( temp.size() > 0 )
					FileUtil.writeFile(parentFolderPath + File.separator + (page + 1), temp);
			}
		}
	}
	
	
	public static String getAbsolutePath(String portalRoot,String resourcePath){
		if(resourcePath.contains("\\")) 
			resourcePath = resourcePath.replace("\\", File.separator);
		if( !resourcePath.startsWith("/mnt") ) 
			if( resourcePath.startsWith("/resource") )
				return portalRoot + resourcePath; 
			else if(resourcePath.startsWith("http") && resourcePath.contains("cms") )
				return  new File(portalRoot).getParentFile().getAbsolutePath() + "/res/" + resourcePath.substring(resourcePath.indexOf("cms"), resourcePath.length());
		 	else if( resourcePath.startsWith("cms") )
				return portalRoot + File.separator + resourcePath; 
		return resourcePath;
	}
	
	
	/**
	 * 
	 * @param sourcePath 要备份的文件夹路径
	 * @param backUpPath 备份文件的存储路径
	 */
	public static boolean backUp(String sourcePath,String backUpPath,String prefix){
		try{
			File sourceFolder = new File(sourcePath);
			if( !sourceFolder.exists() ) return false;
			if( !new File(backUpPath).exists() ) new File(backUpPath).mkdirs();
			Map<String,String> recoveryMap = new HashMap<>(); // String1:文件移动前路径 String2:文件名
			
			List<File> sourceFiles = new ArrayList<>();
			if(sourceFolder.isFile()) 
				sourceFiles.add(sourceFolder);
			else
				FileUtil.iteratorFile(sourceFiles, sourceFolder, false);
			
			for(File item : sourceFiles){
				String fileName = System.currentTimeMillis() + "_" + item.getName();
				if( !StrUtil.isEmpty(prefix) )
					fileName = prefix + "_" + fileName;
				File backUpFile = new File(backUpPath + File.separator + fileName);
				FileUtil.copyFile(item,backUpFile,true);				
				recoveryMap.put(fileName,item.getAbsolutePath());
			}
			
			if(recoveryMap.size() > 0)
				FileUtil.writeFile(backUpPath + "/map.json", recoveryMap);
		}catch(Exception e){return false;}
		return true;
	}
	
	public static boolean recover(String backUpPath){
		if(backUpPath == null || !new File(backUpPath).exists()) return false;
		try{
			File mapFile = new File(backUpPath + File.separator + "map.json");
			if(!mapFile.exists()) return false;
			Map<String,String> recoveryMap = GsonUtil.gson.fromJson(GsonUtil.getStrFromFile(mapFile), new TypeToken<Map<String,String>>(){}.getType());
			if(recoveryMap.size() <= 0) return false;
		
			for(String fileName : recoveryMap.keySet()){
				File rFile = new File(backUpPath + File.separator + fileName);
				if( rFile.exists()) {
					File targetFile = new File(recoveryMap.get(fileName));
					if( targetFile.exists() ) targetFile.delete();
					if( !targetFile.getParentFile().exists() ) targetFile.getParentFile().mkdirs();
					rFile.renameTo(targetFile);
				}
			}
			mapFile.delete();
		}catch(Exception e){ e.printStackTrace();return false;}
		return true;
	}
	
	public static void main(String[] args){
//		String sourcePath = "C:\\Users\\Jerry\\Desktop\\data\\apps";
//		boolean isSucess = backUp(sourcePath, sourcePath+"_bak", "apps");
//		System.out.println(isSucess);
		
//		String backUpPath = "C:\\Users\\Jerry\\Desktop\\data\\apps_bak";
//		boolean isSuccess2 = recover(backUpPath);
//		System.out.println(isSuccess2);
		int a = 11;
		int b = 4;
		int c = 0;
		c = c + a - b;
		System.out.println(c);
	}
	
	
	public static List<Map<String,Object>> folderChildFilesToListMap(File listGroupFile){
		
		if( !listGroupFile.exists() ) return null;
		Map<String,File> checkFiles = new TreeMap<>(
			new Comparator<String>() {
				@Override public int compare(String o1, String o2) { try{
						return Integer.parseInt(o1) <= Integer.parseInt(o2) ? -1 : 1;
					}catch(Exception e){ return 1;}}
		});
		FileUtil.iteratorFile(checkFiles, listGroupFile,false);
		List<Map<String,Object>> allMapList = new ArrayList<>();
		for( Entry<String, File>  entry: checkFiles.entrySet() ){
			String data = GsonUtil.getStrFromFile(entry.getValue());
			if(data != null && data.length() > 10)
				allMapList.addAll(GsonUtil.getListMap(data));
		}
		return allMapList;
	}
	
	public static void clearFolder(){
		
	}
}
