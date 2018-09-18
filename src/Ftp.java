package cn.com.servlet;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
/**
 * ����ftp
 * �ϴ�ftp�ļ�
 * �ر�ftpͨ��������
 * @author Administrator
 *
 */
public class Ftp {
	 
    //��ӡlog��־
    private static final Log logger = LogFactory.getLog(Ftp.class);
 
    private static Date last_push_date = null;
 
    private Session sshSession;
 
    private ChannelSftp channel;
 
    private static ThreadLocal<Ftp> sftpLocal = new ThreadLocal<Ftp>();
 
    private Ftp(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        jsch.getSession(username, host, port);
        //�����û��������룬�˿ںŻ�ȡsession
        sshSession = jsch.getSession(username, host, port);
        sshSession.setPassword(password);
        //�޸ķ�����/etc/ssh/sshd_config �� GSSAPIAuthentication��ֵyesΪno������û�����Զ�̵�¼
        sshSession.setConfig("userauth.gssapi-with-mic", "no");
 
        //Ϊsession��������properties,��һ�η��ʷ�����ʱ��������yes
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
        //��ȡsftpͨ��
        channel = (ChannelSftp)sshSession.openChannel("sftp");
        channel.connect();
        logger.info("����ftp�ɹ�!" + sshSession);
    }
 
    /**
     * �Ƿ�������
     *
     * @return
     */
    private boolean isConnected() {
        return null != channel && channel.isConnected();
    }
 
    /**
     * ��ȡ�����̴߳洢��sftp�ͻ���
     *
     * @return
     * @throws Exception
     */
    public static Ftp getSftpUtil(String host, int port, String username, String password) throws Exception {
        //��ȡ�����߳�
        Ftp sftpUtil = sftpLocal.get();
        if (null == sftpUtil || !sftpUtil.isConnected()) {
            //�������ӷ�ֹ�����̣߳�ʵ�ֲ�������
            sftpLocal.set(new Ftp(host, port, username, password));
        }
        return sftpLocal.get();
    }
 
    /**
     * �ͷű����̴߳洢��sftp�ͻ���
     */
    public static void release() {
        if (null != sftpLocal.get()) {
            sftpLocal.get().closeChannel();
            logger.info("�ر�����" + sftpLocal.get().sshSession);
            sftpLocal.set(null);
 
        }
    }
 
    /**
     * �ر�ͨ��
     *
     * @throws Exception
     */
    public void closeChannel() {
        if (null != channel) {
            try {
                channel.disconnect();
            } catch (Exception e) {
                logger.error("�ر�SFTPͨ�������쳣:", e);
            }
        }
        if (null != sshSession) {
            try {
                sshSession.disconnect();
            } catch (Exception e) {
                logger.error("SFTP�ر� session�쳣:", e);
            }
        }
    }
 
    /**
     * @param directory  �ϴ�ftp��Ŀ¼
     * @param uploadFile �����ļ�Ŀ¼
     *
     */
    public void upload(String directory, String uploadFile) throws Exception {
        try {       //ִ���б�չʾls ����
        channel.ls(directory);      //ִ���̷��л�cd ����
        channel.cd(directory);
        List<File> files = getFiles(uploadFile, new ArrayList<File>());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            channel.put(input, file.getName());
            try {
                if (input != null) input.close();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(file.getName() + "�ر��ļ�ʱ.....�쳣!" + e.getMessage());
            }
            if (file.exists()) {
                boolean b = file.delete();
                logger.info(file.getName() + "�ļ��ϴ����!ɾ����ʶ:" + b);
            }
        }
        }catch (Exception e) {
            logger.error("����Ŀ¼�����С���",e);
                        //������Ŀ¼
            channel.mkdir(directory);
        }
 
    }
    //��ȡ�ļ�
    public List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (null == last_push_date ) {
                        return true;
                    } else {
                        long modifyDate = file.lastModified();
                        return modifyDate > last_push_date.getTime();
                    }
                }
            });
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
                if (null == last_push_date) {
                    last_push_date = new Date(file.lastModified());
                } else {
                    long modifyDate = file.lastModified();
                    if (modifyDate > last_push_date.getTime()) {
                        last_push_date = new Date(modifyDate);
                    }
                }
            }
        }
        return files;
    }
}