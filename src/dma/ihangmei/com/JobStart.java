package dma.ihangmei.com;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import dma.ihangmei.com.utils.GsonUtil;
import dma.ihangmei.com.utils.Utils;

public class JobStart {

	public static final DateFormat dfSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	public class PublicParams{
		public String portalRoot;
		public String portalCmsRoot;
		public String portalDataRoot;
		public PublicParams(String portalRoot){
			this.portalRoot = portalRoot;
			this.portalDataRoot = portalRoot + "/data";
			this.portalCmsRoot = portalRoot + "/cms";
		}
	}
	public static void main(String[] args){
		if(args.length == 0) return;
		String portalRoot = args[0];
		
		// 开始校验电影
		CheckResult videoResult = VideoHandler.checkVideo(portalRoot);
		if( !videoResult.isSuccess ) {
			System.out.println("video unsuccess:" + GsonUtil.toJson(videoResult));
			Utils.recover(portalRoot + "/data/video_bak");
			return;
		}else
			coutResult(videoResult);

		//开始校验游戏和应用
		CheckResult appsResult = AppHandler.checkApps(portalRoot);
		if( !appsResult.isSuccess ){
			System.out.println("app unsuccess:" + GsonUtil.toJson(appsResult));
			Utils.recover(portalRoot + "/data/apps_bak");
		}else
			coutResult(appsResult);
		
		CheckResult varietyResult = VarietyHandler.checkVariety(portalRoot);
		if( !varietyResult.isSuccess ){
			System.out.println("app unsuccess:" + GsonUtil.toJson(varietyResult));
			Utils.recover(portalRoot + "/data/variety_bak");
		}else
			coutResult(varietyResult);
		
		//NewsHandler.cleanUpNewsData(portalRoot);
	}
	
	public static void coutResult(CheckResult result){
		for(String key : result.sourceNumbers.keySet()) {
			int[] tmp = result.sourceNumbers.get(key);
			int lastNumber = tmp[0] -tmp[1];
			long nowStamp = System.currentTimeMillis() + 8 * 60 * 60 * 1000L;
			System.out.println("[" + dfSimple.format(Long.valueOf(nowStamp)) + "][Total "+key+" Number]:" + lastNumber);
		}
	}
}
