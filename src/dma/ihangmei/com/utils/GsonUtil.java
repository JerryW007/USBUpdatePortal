package dma.ihangmei.com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class GsonUtil {

	public final static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	private GsonUtil(){}
	
	public static JsonArray getJsonArrayFromStr(String data){
		if(data == null || data == "") return null;
		JsonParser parser = new JsonParser();
		JsonArray array = parser.parse(data).getAsJsonArray();
		return array;
	}
	
	public static List<Object> arrayToModelList(JsonArray array,Class<?> cls){
		if( array == null || array.size() < 1) return null;
		List<Object> list = new ArrayList<>();
		Iterator<JsonElement> it = array.iterator();
		Gson gson = new Gson();
		while (it.hasNext()) { list.add(gson.fromJson(it.next(), cls)); }
		return list;
	}
	
	public static String getParameter(String json,String paramName){
		JsonObject  jsonObject  = new JsonParser().parse(json.toString()).getAsJsonObject();
		if(jsonObject.get(paramName) != null && jsonObject.get(paramName).getAsString() != "") 
			return jsonObject.get(paramName).getAsString();
		return null;
	}
	
	public static List<Object> getListFromData(String data,Class<?> cls){
		return  arrayToModelList(getJsonArrayFromStr(data),cls);
	}
	
	public static String toJson(Object model){
		return gson.toJson(model);
	}
	
	public static Object toObject(String json,Class<?> cls){
		return gson.fromJson(json, cls);
	}
	
	public static List<Map<String,Object>> getListMap(String data){
			List<Map<String,Object>> list = new ArrayList<>();
			JsonArray array = getJsonArrayFromStr(data);
			if( array == null || array.size() < 1) return null;
			Iterator<JsonElement> it = array.iterator();
			while (it.hasNext()) { 
				JsonObject json = it.next().getAsJsonObject();
				Map<String,Object> map = gson.fromJson(json, new TypeToken<Map<String,Object>>(){}.getType());
				list.add(map);
			}
			return list;
	}
	
	public static Map<String,Object> getMap(String data){
		if(data == null || data.length() <= 0) return null;
		Map<String,Object> map = new HashMap<>();
		try{
			JsonObject json =  new JsonParser().parse(data).getAsJsonObject();
			map = gson.fromJson(json, new TypeToken<Map<String,Object>>(){}.getType());
		}catch(Exception e){return null;}
		return map;
	}
	
	public static String getStrFromFile(File file){
		StringBuffer sb = new StringBuffer();
		try ( InputStream fr = new FileInputStream(file) ) {
			try ( BufferedReader bf = new BufferedReader(new InputStreamReader(fr, "UTF-8")) ) {
				String line = "";
				while ((line = bf.readLine()) != null) sb.append(line);
			}catch( Exception e ){e.printStackTrace();}
			}catch( Exception e ){e.printStackTrace();}
		return sb.toString();
	}
	
	public static String getStrFromFile(String filePath){
		if( !new File(filePath).exists() ) return null;
		return getStrFromFile(new File(filePath));
	}
		
}
