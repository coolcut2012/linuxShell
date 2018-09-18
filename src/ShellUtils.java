package cn.com.servlet;

import java.io.File;
import java.io.FileOutputStream;
 
import org.apache.http.util.TextUtils;
 
/**
 * ��ȡshell�ű�������
 * ��shell�ű�����������Ŀ¼
 * @author lenovo
 *
 */
public class ShellUtils {
 
	/**
	 * ��ȡshell��String
	 * @param tomcatName tomcat��Ϣ
	 * @param pathName -w������ȷ������Ϣ
	 * @param seconds ������ִ��һ��shell
	 * @return
	 */
	public static String getShell(String content){
		StringBuffer sb=new StringBuffer();
		//sb.append("#!/bin/bash \n echo \"$1\"");
		sb.append(content);
		return winString2Linux(sb.toString());
	}
	
	/**
	 * ��shell�ļ�������̶�·����
	 * @param tomcatName
	 * @param pathName
	 * @param seconds
	 * @param path
	 * @throws Exception
	 */
	public static void getShellFile(String path,String content) throws Exception{
		String shellString=getShell(content);
		FileOutputStream fos = new FileOutputStream(new File(path));
		fos.write(shellString.getBytes());
		fos.close();
	}
	
	/**
	 * ��windows�µ�shell�ļ�ת��ΪLinux�µ�shell�ļ�
	 * @param content shell����
	 * @return
	 */
	public static String winString2Linux(final String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        final char[] chars = content.toCharArray();
        char curChar;
        for (int i =0 ; i < chars.length; i++) {
            curChar = chars[i];
            if ('\r' != curChar) {
                buffer.append(curChar);
            }
        }
        return buffer.toString();
}
	
	public static void main(String[] args) throws Exception {
		String winString2Linux = ShellUtils.winString2Linux("#!/bin/bash \n echo \"$1\"");
		ShellUtils.getShellFile("D:/uploadTest/aaa.sh","#!/bin/bash \n echo \"$1\"");
	}
}