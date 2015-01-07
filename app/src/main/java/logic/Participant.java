package logic;

import java.util.HashMap;
import java.util.Map;

public class Participant {

	public Map <String, Object> info;
	private Map <String, Object> biometrics;
	
	public Participant(){
		info = new HashMap<String, Object>();
		biometrics = new HashMap<String, Object>();
		info.put("known_user", "false");
		info.put("mobile_number", "penis");
		
	}
	
	public void download_user_info(String user_id){
		//Get User Info from cloud
	}
	
	public void save_user_info(){
		//Save User to cloud
	}
}
