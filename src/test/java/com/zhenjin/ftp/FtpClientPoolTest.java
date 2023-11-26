package com.zhenjin.ftp;

/**
 * 测试ftp 上传.下载.删除
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = FtpApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FtpClientPoolTest {
//
//    @Autowired
//    private FtpClientTemplate ftpTemplate;
//
//    @Test
//    public void uploadFileTest() {
//        File file = new File("F:\\2017061315035721.txt");
//        boolean uploadResult = ftpTemplate.uploadFile(file, "/");
//        Assert.assertTrue(uploadResult);
//    }
//
//
//    @Test
//    public void uploadFileThreadTest() {
//        for (int i = 0; i < 100; i++) {
//            Runnable runnable = () -> {
//                File file = new File("F:\\2017061315035721.txt");
//                boolean uploadResult = ftpTemplate.uploadFile(file, "/");
//                String threadName = Thread.currentThread().getName();
//                System.out.println("Thread 1-" + threadName + ":" + uploadResult);
//            };
//            runnable.run();
//            new Thread(runnable).start();
//
//            Runnable runnable1 = () -> {
//                File file = new File("F:\\2019010115035721.txt");
//                boolean uploadResult = ftpTemplate.uploadFile(file, "/");
//                String threadName = Thread.currentThread().getName();
//                System.out.println("Thread 2-" + threadName + ":" + uploadResult);
//            };
//            runnable1.run();
//            new Thread(runnable1).start();
//        }
//    }
//
//
//    @Test
//    public void downloadFileTest() {
//        boolean downloadResult = ftpTemplate.downloadFile("/", "2017061315035721.txt", "F:\\");
//        Assert.assertTrue(downloadResult);
//    }
//
//    @Test
//    public void deleteFileTest() {
//        boolean deleteResult = ftpTemplate.deleteFile("/home/test", "2017061315035721.txt");
//        Assert.assertTrue(deleteResult);
//    }
//

}
