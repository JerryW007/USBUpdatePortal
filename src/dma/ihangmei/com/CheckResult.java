package dma.ihangmei.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckResult{
	public  String message;// 校验结果
	public  boolean isSuccess = false; // 是否校验成功
	public  String checkStartDate; // 校验开始时间
	public  String checkEndDate;// 校验结束时间
	public  String portalIndexRoot;// portal索引路径
	//public  int totalListNumber; // 资源索引总个数
	//public  int removeListNumber; // 资源缺失索引个数
	public Map<String,  int[]> sourceNumbers = new HashMap<>(); // "app","50,8":应用的总资源个数,资源不全的应用有8个
	public  List<String> reports = new ArrayList<>();// 校验步骤信息
}