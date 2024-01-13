package DEBUG;

import Config.ConfigurationManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class Debug {
	private static File file;
	public static String PROJECT_PATH = "C:/Users/kotmi/Documents/GitHub/GorBot/src/main/";
	public static boolean isDevBuild = true;
	private ConfigurationManager configurationManager = new ConfigurationManager("config");

	public Debug() {
		if (isDevBuild) {
			file = new File(Debug.PROJECT_PATH + "resources/logs.txt");
		}
	}

	public void errorMessageDebug(String logs) throws IOException {
		if (isDevBuild) {
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			Date date = new Date(System.currentTimeMillis());

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
				writer.write("-------------ERROR MESSAGE-----------------" + "\r\n");
				writer.write(formatter.format(date) + ":    " + logs + "\r\n");
				writer.write("------------/ERROR MESSAGE-----------------" + "\r\n");
			} catch (IOException e) {
				System.out.println("Warning: FileWriter does not start.");
				throw new RuntimeException(e);
			}
		}
	}

	public static void functionDebug(String logs){
		try {
			if (isDevBuild) {
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				Date date = new Date(System.currentTimeMillis());

				try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
					writer.write(formatter.format(date) + ":    " + logs + "\r\n");
				} catch (IOException e) {
					System.out.println("Warning: FileWriter does not start.");
					throw new RuntimeException(e);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void warningDebug(String logs) throws IOException {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			System.out.println("-------------------WARNING DEBUG------------------------");
			System.out.println(formatter.format(date) + ":    " + logs + "\r\n");
			System.out.println("------------------/WARNING DEBUG------------------------");
			writer.write("-------------------WARNING DEBUG------------------------" + "\r\n");
			writer.write(formatter.format(date) + ":    " + logs + "\r\n");
			writer.write("------------------/WARNING DEBUG------------------------" + "\r\n");
		} catch (IOException e) {
			System.out.println("Warning: FileWriter does not start.");
			throw new RuntimeException(e);
		}
	}

	public void cleanLogs() {
		if (isDevBuild) {
			try (FileWriter fileWriter = new FileWriter(file)) {
				fileWriter.write("");
				fileWriter.flush();
			} catch (IOException e) {
				System.out.println("Warning: FileWriter does not start.");
				throw new RuntimeException(e);
			}
		}
	}
}
