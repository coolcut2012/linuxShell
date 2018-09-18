package cn.com.servlet;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import cn.com.reachway.audit.script.ScriptService;
import cn.com.reachway.utils.FtpUtils;

public class RemoteShellTool2 {

	private Connection conn;
	private String ipAddr;
	private String charset = Charset.defaultCharset().toString();
	private String userName;
	private String password;

	public RemoteShellTool2(String ipAddr, String userName, String password,
			String charset) {
		this.ipAddr = ipAddr;
		this.userName = userName;
		this.password = password;
		if (charset != null) {
			this.charset = charset;
		}
	}

	public boolean login() throws IOException {
		conn = new Connection(ipAddr);
		conn.connect(); // ����
		return conn.authenticateWithPassword(userName, password); // ��֤
	}

	public String exec(String cmds) {
		InputStream in = null;
		String result = "";
		try {
			if (this.login()) {
				Session session = conn.openSession(); // ��һ���Ự
				session.execCommand(cmds);
				
				in = session.getStdout();
				//InputStream is = new FileInputStream("/test.sh");
				result = this.processStdout(in, this.charset);
				session.close();
				conn.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	public String processStdout(InputStream in, String charset) {
	
		byte[] buf = new byte[1024];
		StringBuffer sb = new StringBuffer();
		try {
			while (in.read(buf) != -1) {
				sb.append(new String(buf, charset));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//���Ϊ�ļ�
		ScriptService scriptService = new ScriptService();
		scriptService.mkFile("D:/uploadTest", "121212.sh", "#!/bin/bash echo 'firstTest' ");
		FtpUtils ftpUtils = new FtpUtils();
		ftpUtils.initFtpClient();
		ftpUtils.uploadFile("./ftp/pub", "first.sh", "D:/first.sh");

		RemoteShellTool2 tool = new RemoteShellTool2("192.168.199.146", "root",
				"123456", "utf-8");

		//String result = tool.exec("echo 'HelloWorld'");
		
		String result = tool.exec("sh /var/www/firstTest.sh ���Բ���");
		System.out.print(result);
		

	}

}