import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerateIntArray
{
    private int     count   = 1000;             // 数组的个数,
    private int     size    = 3000;               // 每个数组的元素个数
    private int[][] dataArr;
    private Random  random  = new Random(1000);

    public GenerateIntArray() {
        dataArr = new int[count][size];
    }

    public GenerateIntArray(int count, int size) {
        this.count = count;
        this.size = size;
        this.dataArr = new int[count][size];
    }

    public int[][] getDataArr() {
        return dataArr;
    }
    /**
     * 刷新数组中的数据
     */
    public int[][] refreshDataArr() {
        int total = count * size;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < size; j++) {
                dataArr[i][j] = random.nextInt(total);
            }
        }
        return dataArr;
    }

    private class getIntTask implements Runnable {
        private int arrIndex;
        private CountDownLatch latch;
        public getIntTask(int arrIndex,CountDownLatch latch) {
            this.arrIndex = arrIndex;
            this.latch = latch;
        }
        @Override
        public void run() {
            int total = count * size;
            for(int i = 0;i < size;i++) {
                dataArr[arrIndex][i] = random.nextInt(total);
            }
            latch.countDown();
        }
    }

    /**
     * 多线程方式生成数组中的数据
     * 启动count个线程,每个线程中产生size个整数
     */
    public int[][] refreshDataArr_M() {
        CountDownLatch latch = new CountDownLatch(count);
        ExecutorService exec = Executors.newCachedThreadPool();
        for(int i=0;i<count;i++) {
            exec.execute(new getIntTask(i,latch));
        }
        try {
            latch.await();
            //保证在所有生成数据线程没有完成之前,当前方法不返回.
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        exec.shutdown();
        return dataArr;
    }

    /**
     * 写数组数据到文件,如果文件已经存在,则会被删除,然后重新生成文件
     * 每次写入数组中的一个数据
     * @param f
     * @throws IOException
     */
    public void writeData2File(File f) throws IOException {
        if (null != f && f.exists()) {
            f.delete();
        }
        RandomAccessFile rf = new RandomAccessFile(f, "rw");
        rf.seek(0);// 每次都从头开始些文件
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < size; j++) {
                rf.writeInt(dataArr[i][j]);
            }
        }
        rf.close();
    }

    /**
     * 写数据时,现将整数转换成字节数据保存,然后一次性写入字节数组到文件,
     * 避免频繁写入.
     * @param f
     * @throws IOException
     */
    public void writeData2File_B(File f) throws IOException {
        if (null != f && f.exists()) {
            f.delete();
        }
        RandomAccessFile rf = new RandomAccessFile(f, "rw");
        rf.seek(0);// 每次都从头开始些文件
        for (int i = 0; i < count; i++) {
            byte[] byteArr = new byte[4 * size];
            int iTmp = 0;
            for (int j = 0; j < size; j++) {
                byte[] tmpBytes = int2byte(dataArr[i][j]);
                byteArr[iTmp++] = tmpBytes[3];
                byteArr[iTmp++] = tmpBytes[2];
                byteArr[iTmp++] = tmpBytes[1];
                byteArr[iTmp++] = tmpBytes[0];
            }
            rf.write(byteArr);
        }
        rf.close();
    }

    /**
     * 多线程方式同时同时写文件
     * @param
     * @throws IOException
     */
    class WriteTask implements Runnable {
        private File f;
        private int dataIndex;
        public WriteTask(File f,int dataIndex) {
            this.f = f;
            this.dataIndex = dataIndex;
        }

        @Override
        public void run() {
            try {
                RandomAccessFile rf = new RandomAccessFile(f, "rw");
                rf.skipBytes(dataIndex * size * 4 );
                byte[] byteArr = new byte[4 * size];
                int iTmp = 0;
                for (int j = 0; j < size; j++) {
                    byte[] tmpBytes = int2byte(dataArr[dataIndex][j]);
                    byteArr[iTmp++] = tmpBytes[3];
                    byteArr[iTmp++] = tmpBytes[2];
                    byteArr[iTmp++] = tmpBytes[1];
                    byteArr[iTmp++] = tmpBytes[0];
                }
                rf.write(byteArr);
                rf.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void writeData2File_M(File f) throws IOException {
        if (null != f && f.exists()) {
            f.delete();
        }
        //先生成一个固定尺寸的文件,能够保存所有整数的
        RandomAccessFile rf = new RandomAccessFile(f, "rw");
        rf.setLength(count * size * 4 ); //设置尺寸(一个整型占4字节)
        rf.seek(0);
        rf.close();
        ExecutorService exec = Executors.newCachedThreadPool();
        for(int i=0;i<count;i++) {
            exec.execute(new WriteTask(f,i));
        }
        exec.shutdown();
    }

    // 将二进制数转换成字节数组
    private byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移
        return targets;
    }

    public static void main(String[] args)
    {
        int count = 2;
        int size = 100000;
        boolean bPrintData = false; //是否打印生成的数组,当数据量大是不打印,只在小数据量时打印以便测试
        System.out.printf("count = %d, size = %d \n\n",count,size);
        GenerateIntArray generator = new GenerateIntArray(count, size);
        File f;
        try {
            f = new File("test_data.txt");

            System.out.println("正在生成数据,请稍后——————————————————————————————————————");
            long startTmie = System.nanoTime();
            generator.refreshDataArr();
            long totalTime = (System.nanoTime() - startTmie);
            System.out.println("refreshDataArr 生成数据成功, 耗时:" + totalTime+"ns");

            System.out.println("正在生成数据,请稍后——————————————————————————————————————");
            startTmie = System.nanoTime();
            generator.refreshDataArr_M();
            totalTime = (System.nanoTime() - startTmie);
            System.out.println("refreshDataArr_M 生成数据成功, 耗时:" + totalTime+"ns");

            System.out.println("正在写入数据,请稍后——————————————————————————————————————");
            startTmie = System.nanoTime();
            generator.writeData2File_B(f);
            totalTime = (System.nanoTime() - startTmie);
            System.out.println("数据已写入文件" + f.getPath() + File.separator + f.getName());
            System.out.println("writeData2File_B写入数据耗时:" + totalTime+"ns");

            System.out.println("正在写入数据,请稍后——————————————————————————————————————");
            startTmie = System.nanoTime();
            generator.writeData2File(f);
            totalTime = (System.nanoTime() - startTmie);
            System.out.println("数据已写入文件" + f.getPath() + File.separator + f.getName());
            System.out.println("writeData2File 写入数据耗时:" + totalTime+"ns");

            System.out.println("正在写入数据,请稍后——————————————————————————————————————");
            startTmie = System.nanoTime();
            generator.writeData2File_M(f);
            totalTime = (System.nanoTime() - startTmie);
            System.out.println("数据已写入文件" + f.getPath() + File.separator + f.getName());
            System.out.println("writeData2File_M写入数据耗时:" + totalTime+"ns");
            if(bPrintData) {
                System.out.println("原始数组中生成的数据...");
                int[][] intArr = generator.getDataArr();
                for (int i = 0; i < count; i++) {
                    for (int j = 0; j < size; j++) {
                        System.out.printf("%d ", intArr[i][j]);
                    }
                    System.out.println();
                }
                System.out.println("从文件中读取出来的数据...");
                RandomAccessFile rf = new RandomAccessFile(f, "r");
                rf.seek(0);
                int iline = 1;
                while (true) {
                    System.out.printf("%d ",rf.readInt());
                    if(iline % size == 0) {
                        System.out.println();
                    }
                    iline ++;
                    // 判断已经到文件尾了
                    if (rf.getFilePointer() >= rf.length() - 1) {
                        break;
                    }
                }
                rf.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

