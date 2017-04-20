package dma.ihangmei.com.utils;

public class StrUtil {
	
	public static boolean isEmpty(String s) { return s == null || s.length() == 0; }
	
	// 从ix开始，收集所有非空白字符
	public static String collectWord(String s, int ix) {
		int jx;
		for ( jx = ix; jx < s.length() && s.charAt(jx) != ' '; jx++ );
		return s.substring(ix, jx);
	}

	public static String formatSeconds(long sec, boolean wantHour) {
		long h = sec / 3600;
		long m = (sec - h * 3600 ) / 60;
		long s = sec - h * 3600 - m * 60;
		if ( wantHour )
			return String.format("%1$02d:%2$02d:%3$02d", h, m, s);
		return String.format("%1$02d:%2$02d", m, s);
	}
	
	public static Object g(String text) { return text == null? "" : text; } 
	    

    /**
     * 从一个字符串中获取指定的列,会去掉首尾的空格
     * @param line 字符串
     * @param columnNum  列 从1开始计数 
     * @param defaultValue 
     * @return
     */
    public static String parseColumn(String line, int columnNum, String defaultValue) {
        return parseColumn(line," ",columnNum, defaultValue );
    }
	
    /**
     * 从一个字符串中获取指定的列,会去掉首尾的空格
     * @param line 字符串
     * @param spliter 分隔符
     * @param columnNum 列 从1开始计数 
     * @return
     */
    public static String parseColumn(String line,String spliter,int columnNum ){
        return parseColumn(line,spliter,columnNum," ");
    }
    
    /**
     * 从一个字符串中获取指定的列,会去掉首尾的空格
     * @param line 字符串
     * @param spliter 分隔符
     * @param columnNum 列 从1开始计数
     * @param defaultValue  没有取到，则取默认值
     * @return
     */
    public static String parseColumn(String line,String spliter,int columnNum, String defaultValue){
        // 空格区分时，多个空格合并一个
        if( spliter.equals(" ") ){
            line = line.trim().replaceAll("\\s{1,}", " ");
        }
        String[] strs = line.split(spliter);
        //空串的特例
        if(strs.length == 1 && columnNum >= 2){
            return defaultValue;
        }
        if(strs != null && strs.length >= (columnNum - 1)){
            return strs[columnNum -1];
        }else{
            return defaultValue;
        }
    }

	public static boolean isSame(String s1, String s2 ) {
		if ( s1 == null ) return s2 == null;
		if ( s2 == null ) return false;
		return s1.equalsIgnoreCase(s2);
	}
	
	public static boolean isSameWithCase(String s1, String s2 ) {
		if ( s1 == null ) return s2 == null;
		if ( s2 == null ) return false;
		return s1.equals(s2);
	}

	public static String addColonsToMac(String t) {
		// 加冒号到mac地址，必须是12位长度 （GD200...就不可以加)
		return t.length() != 12? t : t.substring(0,2) + ":" + t.substring(2,4) + ":" + t.substring(4,6) + ":" + t.substring(6,8) + ":" + t.substring(8,10) + ":" + t.substring(10,12);
	}

	
	/*
	public static void main(String[] args) {
		String s = collectWord("/c  /abc/def ", 3);
		System.out.println("|" + s + "|");
	}
	*/
}
