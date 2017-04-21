package dma.ihangmei.com.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileUtil {

    public static boolean createFile(String destFileName) {
        File file = new File(destFileName);
        if (file.exists()) {
            return false;
        }
        if (destFileName.endsWith(File.separator)) {
            return false;
        }
        // 判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists()) {
            // 如果目标文件所在的目录不存在，则创建父目录
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        // 创建目标文件
        try {
            if (file.createNewFile()) 
                return true;
        } catch (Exception e) {}
        return false;
    }
	/**
	 * 根据路径和文件名查找文件
	 * @param root
	 * @param fileName
	 * @return 文件路径
	 */
	public static String searchForFile(String root, String fileName,boolean isFolder) {
		File file = new File(root);
		if( file.exists() && file.isDirectory() ) {
			Map<String, File> map = new HashMap<>();
			iteratorFile(map, file, isFolder);
			if( map != null && map.size()> 0 && map.get(fileName) != null)
				return map.get(fileName.toLowerCase()).getAbsolutePath();
		}
		return null;
	}

	/**
	 * 遍历文件夹,获取所有文件，放到HashMap里面, 如果文件名重复，只考虑最后一个，其他会被忽略
	 * @param map
	 * @param file
	 */
	public static void iteratorFile(Map<String, File> map,File file,boolean withFolder) {
		iteratorFile(map, file, withFolder,false);
	}
	
	/**
	 * 遍历文件夹,获取所有文件，放到HashMap里面, 如果文件名重复，只考虑最后一个，其他会被忽略
	 * @param map 遍历文件结果,file 要遍历的文件夹,withFolder 是否将文件夹遍历,addPathLevel key中包含的文件路径级别,0不包含路径,1包含一级路径...
	 * @param file
	 */
	public static void iteratorFile(Map<String, File> map,File file,boolean withFolder,boolean withPath) {
		try {
			if( file.exists() && file.isDirectory() ) 
				for( File f : file.listFiles() )
					if( !f.isDirectory() ){ 
//						String[] pathLevels = f.getAbsolutePath().split(File.separator);
//						if(addPathLevel > 0 && pathLevels.length > 0 && addPathLevel < pathLevels.length - 1){
//							String pathLevel = "";
//							for(int i=pathLevels.length;i>=pathLevels.length - addPathLevel;i--){
//								pathLevel = File.separator + pathLevels[i-1] + pathLevel;
//							}
						if(withPath){
							map.put(f.getParentFile().getName() + File.separator + f.getName(), f);
						}else
							map.put( f.getName(), f );
					}else{
						if( withFolder )
							map.put( f.getName(), f );
						iteratorFile( map, f, withFolder,withPath);
					}
		} catch (Exception e) {}
	}
	/**
	 * 遍历文件夹,获取所有文件
	 * @param map
	 * @param file
	 */
	public static void iteratorFile(List<File> list, File file, boolean takeFolder) {
		try {
			if( file.exists() && file.isDirectory() ) 
				for( File f : file.listFiles() )
					if( !f.isDirectory() ) 
						list.add(f);
					else{
						if( takeFolder )
							list.add(f);
						iteratorFile( list, f, takeFolder);
					}
		} catch (Exception e) {}
	}
	
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        // 创建目录
        if (dir.mkdirs()) {
            return true;
        } else {
            return false;
        }
    }
    
    // 检查文件里面有几行是包含了 args (一组string)
    // args 里面每个string只会被比较和加数一次
    public static int countLines(String fileName, Object...args) {
    	File f = new File(fileName); if ( !f.exists() || f.isDirectory() ) return 0;
    	
    	ArrayList<String> sl = new ArrayList<>();
    	for (Object o : args) sl.add(o.toString());
    	
		int count = 0;
    	try ( BufferedReader br = new BufferedReader(new FileReader(f))) {
    		String line;
    		while ( (line = br.readLine()) != null ) 
    			for ( int ix = 0; ix < sl.size(); ix++ ) 
    				if (line.contains(sl.get(ix))) {
    					count++;
    					sl.remove(ix);	// 不要重复计算
    					if ( sl.size() == 0 )
    						return count;
    					break;
    				}
    	} catch (Exception e) {}
    	return count;
    }

    public static boolean isSymlink(File file) { 
    	try {
	        if (file != null) {
		        File canon = new File(file.getParent() == null? file : file.getParentFile().getCanonicalFile(), file.getName());  
		        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile()); 
	        }
	    } catch (Exception e) {}
	    	
	    return false;
    }  
    
    public static String createTempFile(String prefix, String suffix, String dirName) {
        File tempFile = null;
        if (dirName == null) {
            try {
                // 在默认文件夹下创建临时文件
                tempFile = File.createTempFile(prefix, suffix);
                // 返回临时文件的路径
                return tempFile.getCanonicalPath();
            } catch (IOException e) { 
                return null;
            }
        } else {
            File dir = new File(dirName);
            // 如果临时文件所在目录不存在，首先创建
            if (!dir.exists()) {
                if (!FileUtil.createDir(dirName)) {
                    return null;
                }
            }
            try {
                // 在指定目录下创建临时文件
                tempFile = File.createTempFile(prefix, suffix, dir);
                return tempFile.getCanonicalPath();
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    private static int _delete(File file, boolean emptyDirOnly) throws IOException {
    	int c = 0;
    	if( !file.isDirectory() ) { 
    		if ( !emptyDirOnly ) {
    			c++;
    			file.delete();
    		}
    	} else {
    		File [] files = file.listFiles();
    		
    		//directory is empty, then delete it
	    	if( files.length == 0 ) 
	    		file.delete();
	    	else {
        	   for ( File f : files ) 
        		   c += _delete(f, emptyDirOnly);
        		
        	   //check the directory again, if empty then delete it
        	   if( file.list().length == 0 ) 
           	     file.delete();
	    	}
    	}
    	return c;
    }
        	   
	public static int removeAllFiles(String rootDir, boolean emptyDirOnly) {
		
		// 防止重要目录被删
		if ( rootDir == null || rootDir.length() == 0 ) return 0;
		if ( rootDir.equalsIgnoreCase("/") ) return 0;
		if ( !rootDir.startsWith("/") ) return 0;
		
		while ( rootDir.length() > 1 && rootDir.endsWith("/") ) 
			rootDir = rootDir.substring(0, rootDir.length() - 1);
				
		if ( rootDir.equalsIgnoreCase("/") ||
		     rootDir.equalsIgnoreCase("/etc") ||
		     rootDir.equalsIgnoreCase("/mnt") ||
		     rootDir.equalsIgnoreCase("/dev") ||
		     rootDir.equalsIgnoreCase("/proc") ||
		     rootDir.equalsIgnoreCase("/opt") ) 
			return 0;
		
		int c = 0;
		File d = new File(rootDir);
		if (d.exists()) {
			try {
				c += _delete(d, emptyDirOnly);
			} catch (Exception e) {}
			
			String [] l = d.list();
			if ( l == null || l.length == 0 ) { 
				d.delete();
			}
		}
		return c;
	}

	public static String getFile(String fileName) {
		File f = new File(fileName);
		if ( f.exists() ) 
			try (Scanner s = new Scanner(new FileInputStream(f), "UTF-8")) {
				s.useDelimiter("\\Z");
				if (s.hasNext()) 
					return s.next();
			} catch (Exception e) {}
		return "";
	}
	public static void main(String[] args){
		System.out.println(getFile("C:\\Users\\Jerry\\Desktop\\VACheck.jar"));
	}
	public static ArrayList<String> getFileWithLines(String fileName) {
		ArrayList<String> lines = new ArrayList<>();
		try (Scanner s = new Scanner(new FileInputStream(fileName), "UTF-8")) {
			s.useDelimiter("\n");
			while (s.hasNext() )
				lines.add(s.next());
		} catch (Exception e) {}
		return lines;
	}
	
	public static boolean addTextToFile(String mainFile, String backupFile, String searchText, String addText, boolean addOnce) {
		File main = new File(mainFile);
		if ( !main.exists() || StrUtil.isEmpty(addText)) return false;

		// 先找再决定是否写，要尽可能减少设备上的硬盘写
		boolean found = false;
		ArrayList<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(main))) {
			String line;
			while ( (line = br.readLine()) != null ) {
				if ( line.contains(addText)) 
					found = true;
				lines.add(line);
			}
		} catch(Exception e) { return false; }
		
		// 已有addText
		if ( found ) return found;
		
		// 创建备份文件，然后写文件
		File backup = new File(backupFile);
		FileUtil.copyFile(main, backup, true);
	
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(main))) {
			found = false;
			boolean added = false;
			for ( String line : lines ) {
				if ( StrUtil.isEmpty(searchText) || !line.contains(searchText) )
					bw.write(line);
				else {
					if ( !added || !addOnce ) {
						bw.write(addText);
						added = true;
					}
					found = true;
				}
				bw.write("\n");
			}
			
			if ( !found ) {
				bw.write(addText);
				bw.write("\n");
			}
		} catch ( Exception e ) { 
			main.delete();
			backup.renameTo(main);
			return false; 
		}
		return true;
	}
	
	
    // 在 mainFile 里找到 text 并替换为 replace (如果 replace 是 NULL 只返回查询结果，文件不会更改
    // backupFile 是 mainFile 的备份，可为空  (不备份)  tmpFile 为临时文件, 必须有
    // mainFile/backupFile/tmpFile 都必须在同一个文件系统里，File.renameTo 才会成功
	public static boolean replaceText(String mainFile, String backupFile, String tmpFile, String text, String replace) {
		File main = new File(mainFile);
		if ( !main.exists() || StrUtil.isEmpty(text) ) return false;

		// 先找再决定是否写，要尽可能减少设备上的硬盘写
		boolean found = false;
		try (BufferedReader br = new BufferedReader(new FileReader(main))) {
			String line;
			while ( (line = br.readLine()) != null ) 
				if ( line.contains(text)) {
					found = true;
					break;
				} 
		} catch(Exception e) { return false; }
		
		if ( !found || replace == null ) return found;
		
		// 创建备份文件，然后写新文件
		File tmp = new File(tmpFile);
		
		try (BufferedReader br = new BufferedReader(new FileReader(main))) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
			for ( ;; ) {
				String line = br.readLine();
				if ( line == null ) break;
				if ( line.contains(text)) 
					line = line.replace(text, replace);
				bw.write(line + "\n");
			}
		} catch(Exception e) { return false; }
		} catch(Exception e) { return false; }
		
		// 文件改名
		return doFileChange("ReplaceText", main, tmp, StrUtil.isEmpty(backupFile)? null : new File(backupFile));
	}
	
	   
    // 在 mainFile 里找到 text 并替换为 replace (如果 replace 是 NULL 只返回查询结果，文件不会更改
	// backupFile 是 mainFile 的备份，可为空  (不备份)  tmpFile 为临时文件, 必须有
	// mainFile/backupFile/tmpFile 都必须在同一个文件系统里，File.renameTo 才会成功
	public static boolean replaceAndAppendText(String mainFile, String backupFile, String tmpFile, String text, String replace,String append) {
		File main = new File(mainFile);
		if ( !main.exists() || StrUtil.isEmpty(text) ) return false;

		// 先找再决定是否写，要尽可能减少设备上的硬盘写
		boolean found = false;
		boolean appendFound = false;
		try (BufferedReader br = new BufferedReader(new FileReader(main))) {
			for (;;) {
				String line = br.readLine();
				if (line == null)
					break;
				if (line.contains(text)) {
					found = true;
				}
				if (!StrUtil.isEmpty(append)) {
					if (line.contains(append)) {
						appendFound = true;
					}
				}
			}
		} catch(Exception e) { return false; }
       
		if ( (!found || replace == null) && appendFound ) return found;
       
		// 创建备份文件，然后写新文件
		File tmp = new File(tmpFile);
       
		try (BufferedReader br = new BufferedReader(new FileReader(main))) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
          	String line;
          	while ( (line = br.readLine()) != null ) {
               if ( found && line.contains(text)) 
                   line = line.replace(text, replace);
               bw.write(new String((line + "\n").getBytes("utf-8"),"utf-8"));
          	}
           
          	if(!StrUtil.isEmpty(append) && !appendFound){
               bw.write(new String((append + "\n").getBytes("utf-8"),"utf-8"));
          	}
		} catch(Exception e) { return false; }
		} catch(Exception e) { return false; }
       
		// 文件改名
		return doFileChange("replaceAndAppendText", main, tmp, StrUtil.isEmpty(backupFile)? null : new File(backupFile));
	}

	private static boolean doFileChange(String title, File main, File tmp, File backup) {
       if ( backup != null ) {
           backup.delete();
           main.renameTo(backup);
       }
       
       // 保留可执行性
		boolean exec = main.canExecute();
		main.delete();
		tmp.renameTo(main);
		if ( main.exists() ) {
			main.setExecutable(exec);
			return true;
		}
		
		if ( backup != null ) {
			backup.renameTo(main);
		}
		return false;
	}

	public static boolean copyFile(File in, File out, boolean overwrite) {
		if ( in == null || out == null || !overwrite && out.exists() || !in.exists() ) return false;
		
		long sizeWritten = 0;
		
		try ( FileInputStream fis = new FileInputStream(in) ) {
		try ( FileOutputStream fos = new FileOutputStream(out) ) {
		try ( FileChannel inChannel = fis.getChannel() ) {
		try ( FileChannel outChannel =  fos.getChannel() ) {
			// in 文件有可能在不断增加 (比如 nginx 日志), 必须持续读这个文件
			while ( sizeWritten < in.length() ) {
				long sizeLeft = in.length() - sizeWritten;
				if ( sizeLeft <= 0 ) break;
				inChannel.transferTo(sizeWritten, sizeLeft, outChannel);
				sizeWritten += sizeLeft;
			}
		} catch (Exception e1) {  }
		} catch (Exception e2) {  }
		} catch (Exception e3) {  }
		} catch (Exception e4) {  }
		
		if ( !out.exists() ) {  }
		if ( sizeWritten != out.length() ) { 
			out.delete();
			return false; 
		}
		
		out.setExecutable(in.canExecute());
		out.setLastModified(in.lastModified());
		return true;
	}
	
	private static void deleteOrClearFile(File in, boolean deleteInFileOnSuccess, boolean setToZeroOnSuccess) {
		if ( setToZeroOnSuccess ) {
			try (FileWriter fw = new FileWriter(in)) { fw.write("");} catch (Exception e) {}
		} else {
			in.delete();
		}
	}

	// 如果源文件大小为0，默认返回true
	public static boolean appendFile(File in, File out, boolean deleteInFileOnSuccess, boolean setToZeroOnSuccess) {
		if ( in == null || out == null || !in.exists() ) return false;
		if ( in.length() == 0 ) return true;
		
		if ( !out.exists() ) {
			if ( deleteInFileOnSuccess || setToZeroOnSuccess ) {
				if ( deleteInFileOnSuccess ) {
					in.renameTo(out);	// 直接改名
					if ( out.exists() ) return true;
				}
				
				// 改名不成功，就复制
				if ( !copyFile(in, out, true) ) return false;
				if ( !out.exists() ) return false;
				
				deleteOrClearFile(in, deleteInFileOnSuccess, setToZeroOnSuccess);
				return true;
			} 
			
			if ( !copyFile(in, out, true) )	// 需要复制
				return false;
			return out.exists();
		}

		int inSize = (int)in.length();
		long oldSize = out.length();
		byte [] buff = new byte[16*1024];
		if ( inSize != 0 ) {
			long totalRead = 0;
			try ( FileInputStream fis = new FileInputStream(in) ) {
			try ( RandomAccessFile raf = new RandomAccessFile(out, "rw") ) {
				raf.seek(oldSize);

				int bsr;
				while ( (bsr = fis.read(buff)) != -1 ) {
					raf.write(buff, 0, bsr);
					totalRead += bsr;
				}
				if ( totalRead < inSize ) throw new Exception(String.format("Not enough read %,d wanted, %,d read", inSize, totalRead));
			} catch (Exception e1) {  return false; }
			} catch (Exception e2) {  return false; }
			
			if ( !out.exists() ) {  return false; }
			if ( totalRead + oldSize != out.length() ) { 
				return false; 
			}
		}
		
		deleteOrClearFile(in, deleteInFileOnSuccess, setToZeroOnSuccess);
		return true;
	}


	public static boolean isSamePath(String path1, String path2) {
		if ( StrUtil.isEmpty(path1) || StrUtil.isEmpty(path2) ) return false;
		
		if ( !path1.endsWith("/") ) path1 += "/";
		if ( !path2.endsWith("/") ) path2 += "/";
		
		return path1.equalsIgnoreCase(path2);
	}
	
	// 在 mainFile 里找到 text 并替换为 replace (如果 replace 是 NULL 只返回查询结果，文件不会更改
	// backupFile 是 mainFile 的备份，可为空  (不备份)  tmpFile 为临时文件, 必须有
	// mainFile/backupFile/tmpFile 都必须在同一个文件系统里，File.renameTo 才会成功
	public static boolean replaceAndAppendOnceText(String mainFile, String backupFile, String tmpFile, String text, String replace, String append) {
		File main = new File(mainFile);
		if ( !main.exists() || StrUtil.isEmpty(text) ) return false;

		// 先找再决定是否写，要尽可能减少设备上的硬盘写
		boolean found = false;
		int cAppend = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(main))) {
			String line;
			while ((line = br.readLine()) != null ) {
				if ( line.contains(text)) 
					found = true;
				
				if( !StrUtil.isEmpty(append) && line.contains(append) )
					cAppend++;
			}
       } catch(Exception e) { return false; }
       
       if ( (!found || replace == null) && cAppend == 1 ) return found;
       
       // 创建备份文件，然后写新文件
       File tmp = new File(tmpFile);
       
       try (BufferedReader br = new BufferedReader(new FileReader(main))) {
       try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
           String line;
           while ( (line = br.readLine()) != null ) {
               if ( found && line.contains(text)) 
                   line = line.replace(text, replace);
               else if( !StrUtil.isEmpty(append) && line.contains(append) ) 
            	   continue;
               
               bw.write(line + "\n");
           }
           
           if( !StrUtil.isEmpty(append) )
               bw.write(append + "\n");
       } catch(Exception e) { return false; }
       } catch(Exception e) { return false; }
       
       // 文件改名
       return doFileChange("replaceAndAppendOnceText", main, tmp, StrUtil.isEmpty(backupFile)? null : new File(backupFile));
   }

	public static String readFile(String fn, String searchText, String lineSeparator) {
		StringBuilder sb = new StringBuilder();
		File f = new File(fn);
		if ( f.exists() ) 
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ( (line = br.readLine()) != null ) 
					if ( searchText == null || line.contains(searchText))
						sb.append(line).append(lineSeparator == null? "" : lineSeparator);
			} catch(Exception e) { }
		return sb.toString();
	}
	
	// 把 obj 转换成 json 写到文件里
	public static boolean writeFile(String fileName, Object obj) {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		try ( OutputStreamWriter osw = new  OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "utf-8") ) { 
			GsonUtil.gson.toJson(obj, osw);
		} catch (Exception e) { return false; }
		return true;
	}

	public static String readLastLines(String fn, int count, String searchText, String lineSeparator) {
		StringBuilder sb = new StringBuilder();
		File f = new File(fn);
		if ( f.exists() && count > 0 ) {
			LinkedList<String> lines = new LinkedList<>();
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ( (line = br.readLine()) != null ) 
					if ( searchText == null || line.contains(searchText)) {
						if ( lines.size() >= count )
							lines.remove(0);
						lines.add(line);
					}
			} catch(Exception e) { }
			for ( String l : lines ) 
				sb.append(l).append(lineSeparator);
		}
		return sb.toString();
	}

	public static String makeBackupFileName(String fileName) {
		int ix = fileName.lastIndexOf(".");
		int jx = fileName.lastIndexOf("/");
		if ( ix < 0 || ix < jx ) 	// 没有扩展名
			return fileName + "." + System.currentTimeMillis();
		return fileName.substring(0, ix) + "." + System.currentTimeMillis() + fileName.substring(ix);
	}
	public static void removeEmptyDir(String dir) {
		if ( StrUtil.isEmpty(dir)) return;
		try {
			File d = new File(dir);
			if ( d.exists() && d.isDirectory() ) {
				for ( File subDir : d.listFiles() )
					if ( subDir.isDirectory() )
						removeEmptyDir(subDir.getAbsolutePath());
				if ( d.listFiles().length == 0 ) {
					d.delete();
				}
			}
		} catch (Exception e) {}	
	}
    
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public static void writeFileWithLock(String filePath,String data){
    	try(ReadLock rl = new ReadLock(lock)){
    		if( new File(filePath).exists() ) new File(filePath).createNewFile();
		try( OutputStream os = new FileOutputStream(new File(filePath),true) ){
			os.write((data+"\r\n").getBytes("UTF-8"));
		}catch(Exception e){}	
		}catch(Exception e){}
    }
    
	// 自动处理 ReadLock 的获得和释放
	public static class ReadLock implements AutoCloseable {
		private ReentrantReadWriteLock lock;
		
		public ReadLock(ReentrantReadWriteLock _lock) { (lock = _lock).readLock().lock(); }
		@Override public void close() { lock.readLock().unlock(); }
	}
}