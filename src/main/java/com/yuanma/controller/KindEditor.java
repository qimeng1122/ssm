package com.yuanma.controller;

import com.alibaba.fastjson.JSONObject;
import com.common.resultmodel.Result;
import com.common.resultmodel.ResultUtil;
import com.common.util.ByteStreams;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Controller
@RequestMapping("/kindEditor")
public class KindEditor {


    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> fileUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
            FileUploadException {
        Map<String, Object> msg = new HashMap<String, Object>();
        ServletContext application = request.getSession().getServletContext();
        String savePath = application.getRealPath("/") + "/images/"; //存储路径, 绝对路径
        System.out.println(savePath);

        //String savePath = "I:/img/";

        //显示url, 相对路径
        String saveUrl = "http://localhost:8080/images/";

        // 定义允许上传的文件扩展名
        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("image", "gif,jpg,jpeg,png,bmp");
        extMap.put("flash", "swf,flv");
        extMap.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb");
        extMap.put("file", "doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2");

        // 最大文件大小
        long maxSize = 1000000;

        response.setContentType("text/html; charset=UTF-8");

        if (!ServletFileUpload.isMultipartContent(request)) {
            return getError("请选择文件。");
        }
        // 检查目录
        File uploadDir = new File(savePath);
        if (!uploadDir.isDirectory()) {
            uploadDir.mkdir();
        }
        System.out.println(uploadDir.getAbsolutePath());
        savePath = uploadDir.getAbsolutePath();
        // 检查目录写权限
        if (!uploadDir.canWrite()) {
            return getError("上传目录没有写权限。");
        }

        String dirName = request.getParameter("dir");
        if (dirName == null) {
            //dirName = "image";
        }
        if (!extMap.containsKey(dirName)) {
            return getError("目录名不正确。");
        }
        // 创建文件夹
        //savePath += dirName + "/";
        //saveUrl += dirName + "/";
        File saveDirFile = new File(savePath);
        if (!saveDirFile.exists()) {
            saveDirFile.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String ymd = sdf.format(new Date());
        savePath += "/"+ymd;
        saveUrl += "/"+ymd;
        File dirFile = new File(savePath);
        savePath = dirFile.getAbsolutePath();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //http://localhost:8080/images/image/
       // E:\lihongwei\myProjects\ssm21\target\ssm2-1\imagesimage/
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");


        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Iterator item = multipartRequest.getFileNames();
        while (item.hasNext()) {
            String fileName = (String) item.next();
            MultipartFile file = multipartRequest.getFile(fileName);
            // 检查文件大小
            if (file.getSize() > maxSize) {
                return getError("上传文件大小超过限制。");
            }
            // 检查扩展名
            String fileExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
            if (!Arrays.asList(extMap.get(dirName).split(",")).contains(fileExt)) {
                return getError("上传文件扩展名是不允许的扩展名。\n只允许"
                        + extMap.get(dirName) + "格式。");
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String newFileName = df.format(new Date()) + "_" + new Random().nextInt(1000) + "." + fileExt;
            try {
                File file1 = ByteStreams.multipartFileToFile(file);
                File uploadedFile = new File(savePath, newFileName);
                ByteStreams.copy(new FileInputStream(file1), new FileOutputStream(uploadedFile));
            } catch (Exception e) {
                return getError("上传文件失败。");
            }
            System.out.println("绝对路径："+savePath);
            System.out.println(saveUrl);
            msg.put("error", 0);
            msg.put("url", saveUrl + newFileName);
            return msg;
        }
        return null;
    }

    private Map<String, Object> getError(String s) {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("msg", s);
        return stringObjectHashMap;
    }

    @RequestMapping("/upload")
    public Map<String, Object> upload(MultipartFile filename,HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            //文件上传
            filename.transferTo(new File(request.getSession().getServletContext().getRealPath("/img"),filename.getOriginalFilename()));
            //给返回值
            String url = "http://"+request.getServerName() +":"+request.getServerPort()+request.getContextPath()+"/img/"+filename.getOriginalFilename();
            System.out.println("url:"+url);
            map.put("error",0);
            map.put("url",url);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            map.put("error",1);
            return map;
        }
    }

    @RequestMapping("browser")
    public Map<String, Object> browser(HttpServletRequest request) {
        //获取当前文件夹中的文件
        File file = new File(request.getSession().getServletContext().getRealPath("img"));
        File[] files = file.listFiles();
        //http://localhost:8989/cmfz/img/
        String current_url = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/images/";
        //将map集合中的数据添加在list中
        ArrayList<Object> list = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("is_dir", false);
            map.put("has_file", false);
            map.put("filesize", files[i].length());
            map.put("is_photo", true);
            map.put("filetype", FilenameUtils.getExtension(files[i].getName()));
            map.put("filename", files[i].getName());
            map.put("datetime", new Date());
            list.add(map);
        }
        //将返回数据放入map集合中
        Map<String, Object> map = new HashMap<>();
        map.put("current_url", current_url);
        map.put("total_count", files.length);
        map.put("file_list", list);
        return null;
    }

    @RequestMapping(value = "/fileManager", method = RequestMethod.GET)
    public void fileManager(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {
        ServletContext application = request.getSession().getServletContext();
        ServletOutputStream out = response.getOutputStream();
        /*// 根目录路径，可以指定绝对路径，比如 /var/www/attached/
        String rootPath = application.getRealPath("/") + "images/";
        // 根目录URL，可以指定绝对路径，比如 http://www.yoursite.com/attached/
        String rootUrl = request.getContextPath() + "/images/";*/
        String rootPath = "E:/lihongwei/myProjects/ssm21/target/ssm2-1/images/20200320";
        String rootUrl = "http://localhost:8080/images/20200320";
        // 图片扩展名
        String[] fileTypes = new String[]{"gif", "jpg", "jpeg", "png", "bmp"};
        String dirName = request.getParameter("dir");
        if (dirName != null) {
            if (!Arrays.<String>asList(
                    new String[]{"image", "flash", "media", "file"})
                    .contains(dirName)) {
                out.println("Invalid Directory name.");
                return;
            }
            //rootPath += dirName + "/";
            //rootUrl += dirName + "/";
            File saveDirFile = new File(rootPath);
            if (!saveDirFile.exists()) {
                saveDirFile.mkdirs();
            }
        }
        // 根据path参数，设置各路径和URL
        String path = request.getParameter("path") != null ? request
                .getParameter("path") : "";
        String currentPath = rootPath + path;
        String currentUrl = rootUrl + path;
        String currentDirPath = path;
        String moveupDirPath = "";
        if (!"".equals(path)) {
            String str = currentDirPath.substring(0,
                    currentDirPath.length() - 1);
            moveupDirPath = str.lastIndexOf("/") >= 0 ? str.substring(0,
                    str.lastIndexOf("/") + 1) : "";
        }

        // 排序形式，name or size or type
        String order = request.getParameter("order") != null ? request
                .getParameter("order").toLowerCase() : "name";

        // 不允许使用..移动到上一级目录
        if (path.indexOf("..") >= 0) {
            out.println("Access is not allowed.");
            return;
        }
        // 最后一个字符不是/
        if (!"".equals(path) && !path.endsWith("/")) {
            out.println("Parameter is not valid.");
            return;
        }
        // 目录不存在或不是目录
        File currentPathFile = new File(currentPath);
        if (!currentPathFile.isDirectory()) {
            out.println("Directory does not exist.");
            return;
        }
        // 遍历目录取的文件信息
        List<Hashtable> fileList = new ArrayList<Hashtable>();
        if (currentPathFile.listFiles() != null) {
            for (File file : currentPathFile.listFiles()) {
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                String fileName = file.getName();
                if (file.isDirectory()) {
                    hash.put("is_dir", true);
                    hash.put("has_file", (file.listFiles() != null));
                    hash.put("filesize", 0L);
                    hash.put("is_photo", false);
                    hash.put("filetype", "");
                } else if (file.isFile()) {
                    String fileExt = fileName.substring(
                            fileName.lastIndexOf(".") + 1).toLowerCase();
                    hash.put("is_dir", false);
                    hash.put("has_file", false);
                    hash.put("filesize", file.length());
                    hash.put("is_photo", Arrays.<String>asList(fileTypes)
                            .contains(fileExt));
                    hash.put("filetype", fileExt);
                }
                hash.put("filename", fileName);
                hash.put("datetime",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file
                                .lastModified()));
                fileList.add(hash);
            }
        }

        if ("size".equals(order)) {
            Collections.sort(fileList, new SizeComparator());
        } else if ("type".equals(order)) {
            Collections.sort(fileList, new TypeComparator());
        } else {
            Collections.sort(fileList, new NameComparator());
        }
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("moveup_dir_path", moveupDirPath);
        msg.put("current_dir_path", currentDirPath);
        msg.put("current_url", currentUrl);
        msg.put("total_count", fileList.size());
        msg.put("file_list", fileList);
        response.setContentType("application/json; charset=UTF-8");
        Object o = JSONObject.toJSON(msg);
        out.println(o.toString());
    }

    class NameComparator implements Comparator {
        @Override
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable) a;
            Hashtable hashB = (Hashtable) b;
            if (((Boolean) hashA.get("is_dir"))
                    && !((Boolean) hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean) hashA.get("is_dir"))
                    && ((Boolean) hashB.get("is_dir"))) {
                return 1;
            } else {
                return ((String) hashA.get("filename"))
                        .compareTo((String) hashB.get("filename"));
            }
        }
    }

    class SizeComparator implements Comparator {
        @Override
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable) a;
            Hashtable hashB = (Hashtable) b;
            if (((Boolean) hashA.get("is_dir"))
                    && !((Boolean) hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean) hashA.get("is_dir"))
                    && ((Boolean) hashB.get("is_dir"))) {
                return 1;
            } else {
                if (((Long) hashA.get("filesize")) > ((Long) hashB
                        .get("filesize"))) {
                    return 1;
                } else if (((Long) hashA.get("filesize")) < ((Long) hashB
                        .get("filesize"))) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    class TypeComparator implements Comparator {
        @Override
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable) a;
            Hashtable hashB = (Hashtable) b;
            if (((Boolean) hashA.get("is_dir"))
                    && !((Boolean) hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean) hashA.get("is_dir"))
                    && ((Boolean) hashB.get("is_dir"))) {
                return 1;
            } else {
                return ((String) hashA.get("filetype"))
                        .compareTo((String) hashB.get("filetype"));
            }
        }
    }

    @ResponseBody
    @PostMapping("/uploadImg")
    public Result uploadPicture(@RequestParam(value="file",required=false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> map = new HashMap<String, Object>();
        File targetFile=null;
        String url="";//返回存储路径
        int code = 1;
        System.out.println(file);
        String fileName=file.getOriginalFilename();//获取文件名加后缀
        if(fileName!=null&&fileName!=""){
            String returnUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() +"/images/imgs";//存储路径
            String path = "E:/lihongwei/myProjects/ssm21/src/main/webapp/images/imgs"; //文件存储位置
            String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());//文件后缀
            fileName= Instant.now().toEpochMilli() +"_"+new Random().nextInt(1000)+fileF;//新的文件名
            //先判断文件是否存在
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            //String fileAdd = sdf.format(new Date());
            //获取文件夹路径
            File file1 =new File(path+"/");
            //如果文件夹不存在则创建
            if(!file1 .exists()  && !file1 .isDirectory()){
                file1 .mkdirs();
            }
            //将图片存入文件夹
            targetFile = new File(file1, fileName);
            try {
                //将上传的文件写到服务器上指定的文件。
                file.transferTo(targetFile);
                url=returnUrl+"/"+fileName;
                System.out.println(url);
                code=0;
                //假地址
                map.put("url", url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResultUtil.success(map);
    }
}
