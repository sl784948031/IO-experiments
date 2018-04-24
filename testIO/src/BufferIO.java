import java.io.*;
/**
 * 分别用普通数据流和带缓冲区的数据流复制一个167M的数据文件
 * 通过用时比较两者的工作效率
 * @author Zues
 *
 */
public class BufferIO {
    public static String[] file={"Files/1.pdf","Files/2.pdf","Files/3.pdf"};

    // FileInputStream复制
    public void read(File file,int bufsize) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] buf = new byte[bufsize];
        int read=0;
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            read+=len;
        }
        in.close();
        System.out.println("FileInputStream读取结束,共读取"+read/1024+"KB");
    }
    // BufferedStream复制
    public void readByBuffer(File file,int bufsize) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[bufsize];
        int len;
        int read=0;
        while ((len = in.read(buf)) != -1) {
            read+=len;
        }
        in.close();
        System.out.println("BufferedInputStream读取结束,共读取"+read/1024+"KB");
    }

    public static void readSum(File file,int bufsize)throws IOException{
        BufferIO copy=new BufferIO();
        long time1=System.currentTimeMillis();
        copy.read(file,bufsize);
        long time2=System.currentTimeMillis();
        System.out.println("FileInputStream用时："+(time2-time1)+"毫秒");
        long time3=System.currentTimeMillis();
        copy.readByBuffer(file,bufsize);
        long time4=System.currentTimeMillis();
        System.out.println("BufferedInputStream用时："+(time4-time3)+"毫秒");
    }
    public static void main(String[] args) throws IOException {
        for (String src:file
             ) {
            System.out.println("读取"+src+"中————————————————————————————————————————");
            readSum(new File(src),1024);
        }
        System.out.println("改变bufsize————————————————————————————————————————");
        int []size={100,512,2048,4096};
        for (int s:size
             ) {
            for (String src:file
                 ) {
                System.out.println("读取"+src+"中,此时bufsize为"+s+"————————————————————————————————————————");
                readSum(new File(src),s);
            }
        }
    }
}
