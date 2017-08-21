package logic;

/*
 * Classes associated with setup of the native file system/ common file system tasks
 *
 */

public class NativeSetup {
    //TODO KILL
    /*
    private static void copyFile(String assetPath, String localPath, Context context) {
				
		try {
			InputStream in = context.getAssets().open(assetPath);
			FileOutputStream out = new FileOutputStream(localPath);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();
			
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

	public static void write_string_file(String path, String data){
		try {
			Writer output = null;
			File file = new File(path);
			output = new java.io.BufferedWriter(new FileWriter(file));
			output.write(data);
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String read_string_file(String path){
		StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        char[] buf = new char[1024];
        int numRead=0;
        try {
			while((numRead=reader.read(buf)) != -1){
			    String readData = String.valueOf(buf, 0, numRead);
			    fileData.append(readData);
			    buf = new char[1024];
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return fileData.toString();
		
	}
	static void changePermission(String localPath) {
		
		try {
			Process chmod = Runtime.getRuntime().exec("chmod 777 " +localPath);
		    chmod.waitFor();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
	}
		
	public static void dropPermission(String localPath) {
			
			try {
				Process chmod = Runtime.getRuntime().exec("chmod 777 " +localPath);
			    chmod.waitFor();
			} catch (IOException e) {
			    throw new RuntimeException(e);
			} catch (InterruptedException e) {
			    throw new RuntimeException(e);
			}
		}
	
	private static void makeDirectory(String localPath) {
			
		try {
			Process mkdir = Runtime.getRuntime().exec("mkdir " +localPath);
		    mkdir.waitFor();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
	}
	
	private static String[] getFiles(String folderName, Context context){
		
		String[] files;
		
		try {
			files =context.getAssets().list(folderName);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}	
		return files;
	}
	
	public static boolean checkNativeSystem(Context context) {
		
		File file = new File("/data/data/com.openandid.core/nbis/bin/","nfiq");
		
		if(file.exists()){
			System.out.println("files found!");
			return true;
		}
		else{
			System.out.println("nfiq not found");
			return false;
		}
	}
	
	public static void setupNativeSystem(Context context) {
	
		String baseDir = "/data/data/com.openandid.core";
		//String[] directories = {"nbis","nbis/bin","nbis/include","nbis/include/mlp","nbis/lib","nbis/man","nbis/man/man1","nbis/nbis","nbis/nbis/an2k","nbis/nbis/an2k/chkan2k","nbis/nbis/nfiq","nbis/nbis/pcasys","nbis/nbis/pcasys/images","nbis/nbis/pcasys/parms","nbis/nbis/pcasys/weights","nbis/nbis/pcasys/weights/mlp","nbis/nbis/pcasys/weights/pnn"};
		String[] directories = {"nbis","nbis/bin","nbis/include","nbis/include/mlp","nbis/lib"};
		for(String directoryName:directories){
		     
			makeDirectory((baseDir+"/"+directoryName));
			changePermission((baseDir+"/"+directoryName));
		}
		
		makeDirectory((baseDir+"/nbis/img"));
		dropPermission((baseDir+"/nbis/img"));
		makeDirectory((baseDir+"/nbis/template"));
		dropPermission((baseDir+"/nbis/template"));
		makeDirectory((baseDir+"/nbis/search"));
		dropPermission((baseDir+"/nbis/search"));
		
		for(String directoryName:directories){
		     
			String[] currentFiles = getFiles((directoryName), context);
			
			for(String fileName:currentFiles){
				boolean skipFile = false;
				for(String falseFile:directories){
					String fullName = (directoryName+"/"+fileName);
					if(falseFile.equals(fullName)){
						skipFile = true;
					}
				}
				
				if(skipFile != true){
					System.out.println(directoryName+"/"+fileName+" >> "+baseDir+"/"+directoryName+"/"+fileName);
					copyFile((directoryName+"/"+fileName), (baseDir+"/"+directoryName+"/"+fileName), context);
					changePermission((baseDir+"/"+directoryName+"/"+fileName));
				}	
			}
		}
		System.out.println("Finished Native System Install");
	}
	*/
}
