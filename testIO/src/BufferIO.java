import java.io.*;

public class BufferIO {
    public static String[] file={"Files/1.pdf","Files/2.pdf","Files/3.pdf"};

    // FileInputStream复制
    public void read(File file,int bufsize) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] buf = new byte[bufsize];
        int read=0;
        int len = 0;
        long time1=System.currentTimeMillis();
        while ((len = in.read(buf)) != -1) {
            read+=len;
        }
        long time2=System.currentTimeMillis();
        in.close();
        System.out.println("FileInputStream读取结束,共读取"+read/1024+"KB");
        System.out.println("FileInputStream用时："+(time2-time1)+"毫秒");
    }
    // BufferedStream复制
    public void readByBuffer(File file,int bufsize) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[bufsize];
        int len;
        int read=0;
        long time3=System.currentTimeMillis();
        while ((len = in.read(buf)) != -1) {
            read+=len;
        }
        long time4=System.currentTimeMillis();
        in.close();
        System.out.println("BufferedInputStream读取结束,共读取"+read/1024+"KB");
        System.out.println("BufferedInputStream用时："+(time4-time3)+"毫秒");
    }

    public static void readSum(File file,int bufsize)throws IOException{
        BufferIO copy=new BufferIO();
        copy.read(file,bufsize);
        copy.readByBuffer(file,bufsize);
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
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file[2]),10240 );
        byte[] buf = new byte[1024];
        int len;
        int read=0;
        long time1=System.currentTimeMillis();
        while ((len = in.read(buf)) != -1) {
            read+=len;
        }
        long time2=System.currentTimeMillis();
        in.close();
        System.out.println("BufferedInputStream读取结束,共读取"+read/1024+"KB");
        System.out.println("有10240字节的buffer的BufferedInputStream用时："+(time2-time1)+"毫秒");
    }
}
