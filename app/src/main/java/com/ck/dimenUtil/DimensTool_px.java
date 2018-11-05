package com.ck.dimenUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * @author fei
 * @date on 2018/10/12 0012
 * @describe TODO :此文件为java的可执行文件，
 * 主要用于生成Dimens文件
 **/
public class DimensTool_px {

//    private final static String WTemplate = "<dimen name=\"x{0}\">{1}px</dimen>\n";
    private final static String HTemplate = "<dimen name=\"x{0}\">{1}px</dimen>\n";
    /**
     * {0}-HEIGHT
     */
    private final static String VALUE_TEMPLATE = "values-{0}x{1}";
    private static final String SUPPORT_DIMESION = "320,480;" +
            "480,800;" +
            "1080,2160;" +
            "1600,2560;";
    private int baseW;
    private int baseH;
//    private String dirStr = "E:\\WorkSpace\\Eshop\\elibrary\\src\\main\\res";//编写自动生成文件的存放地址
    private String dirStr = "./app/src/main/res";
    private String supportStr = SUPPORT_DIMESION;

    public DimensTool_px(int baseX, int baseY, String supportStr) {
        this.baseW = baseX;
        this.baseH = baseY;
        if (!this.supportStr.contains(baseX + "," + baseY)) {
            this.supportStr += baseX + "," + baseY + ";";
        }
        this.supportStr += validateInput(supportStr);
        File dir = new File(dirStr);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static float change(float a) {
        int temp = (int) (a * 100);
        return temp / 100f;
    }

    public static void main(String[] args) {
        /*
         * 常见android手机分辨率(4:3 5:3 16:9 *VGA *HD 720p 1080p )
         *
         * 4:3
         * VGA 640*480 (VideoGraphics Array)
         * QVGA 320*240 (Quarter VGA)
         * HVGA 480*320 (Half-size VGA)
         * SVGA 800*600 (Super VGA)
         *
         * 5:3
         * WVGA 800*480 (Wide VGA)
         *
         * 16:9
         * FWVGA 854*480 (Full Wide VGA)
         * HD 1920*1080 High Definition
         * QHD 960*540
         * 720p 1280*720 标清
         * 1080p 1920*1080 高清
         *
         * 手机:
         * iphone 4/4s 960*640 (3:2)
         * iphone5 1136*640
         * 小米1 854*480(FWVGA)
         * 小米2 1280*720
         *
         * 分辨率对应DPI
         * "HVGA    mdpi"
         * "WVGA    hdpi "
         * "FWVGA   hdpi "
         * "QHD     hdpi "
         * "720P    xhdpi"
         * "1080P   xxhdpi "
         */
        //以 720p作为标准  480*800 高清，填写ui标注的宽高
        int baseW = 480;
        int baseH = 800;
        String addition = "";
        try {
            if (args.length >= 3) {
                baseW = Integer.parseInt(args[0]);
                baseH = Integer.parseInt(args[1]);
                addition = args[2];
            } else if (args.length >= 2) {
                baseW = Integer.parseInt(args[0]);
                baseH = Integer.parseInt(args[1]);
            } else if (args.length >= 1) {
                addition = args[0];
            }
        } catch (NumberFormatException e) {
            System.err
                    .println("right input params : java -jar xxx.jar width height w,h_w,h_..._w,h;");
            e.printStackTrace();
            System.exit(-1);
        }

        new DimensTool_px(baseW, baseH, addition).generate();
    }

    /**
     * @param supportStr w,h_...w,h;
     * @return
     */
    private String validateInput(String supportStr) {
        StringBuffer sb = new StringBuffer();
        String[] vals = supportStr.split("_");
        int w = -1;
        int h = -1;
        String[] wh;
        for (String val : vals) {
            try {
                if (val == null || val.trim().length() == 0)
                    continue;
                wh = val.split(",");
                w = Integer.parseInt(wh[0]);
                h = Integer.parseInt(wh[1]);
            } catch (Exception e) {
                continue;
            }
            sb.append(w + "," + h + ";");
        }
        return sb.toString();
    }

    public void generate() {
        String[] vals = supportStr.split(";");
        for (String val : vals) {
            String[] wh = val.split(",");
            generateXmlFile(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
        }


    }

    private void generateXmlFile(int w, int h) {

//        StringBuffer sbForWidth = new StringBuffer();
//        sbForWidth.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
//        sbForWidth.append("<resources>");
//        float cellw = w * 1.0f / baseW;
//
//
////        System.out.println("width : " + w + "," + baseW + "," + cellw);
//        for (int i = 1; i < baseW; i++) {
//            sbForWidth.append(WTemplate.replace("{0}", i + "").replace("{1}",
//                    change(cellw * i) + ""));
//        }
//        sbForWidth.append(WTemplate.replace("{0}", baseW + "").replace("{1}",
//                w + ""));
//        sbForWidth.append("</resources>");


        StringBuffer sbForHeight = new StringBuffer();
        sbForHeight.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sbForHeight.append("<resources>");
        float cellh = h * 1.0f / baseH;
//        System.out.println("height : " + h + "," + baseH + "," + cellh);
        for (int i = 1; i < baseH; i++) {
            sbForHeight.append(HTemplate.replace("{0}", i + "").replace("{1}",
                    change(cellh * i) + ""));
        }
        sbForHeight.append(HTemplate.replace("{0}", baseH + "").replace("{1}",
                h + ""));
        sbForHeight.append("</resources>");


        File fileDir = new File(dirStr + File.separator
                + VALUE_TEMPLATE.replace("{0}", h + "")//
                .replace("{1}", w + ""));
        fileDir.mkdir();


        File layyFile = new File(fileDir.getAbsolutePath(), "lay.xml");
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(layyFile));
            pw.print(sbForHeight.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}