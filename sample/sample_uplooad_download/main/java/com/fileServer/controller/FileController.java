package com.fileServer.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fileServer.entity.File;
import com.fileServer.mapper.FileMapper;

@Controller
public class FileController {

	/**
     * ファイルデータテーブル(file_data)へアクセスするMapper
     */
	@Autowired
	private FileMapper fileMapper;

	 /**
     * ファイルデータ一覧表示処理
     * @param model Modelオブジェクト
     * @return 一覧画面
     */
    @RequestMapping("/fileList")
    @Transactional(readOnly=false)
    public String dispFileList(Model model){
        //ファイルデータテーブル(file_data)を全件取得
        List<File> list = fileMapper.findAll();
        model.addAttribute("fileList", list);

        //一覧画面へ移動
        return "fileList_test";
    }

    /**
     * ファイルデータDB登録処理
     * @param uploadFile アップロードファイル
     * @param model Modelオブジェクト
     * @return ファイルデータ一覧表示処理
     */
    @PostMapping("/upload")
    @Transactional(readOnly = false)
    public String add(@RequestParam("upload_file") MultipartFile uploadFile, Model model){
        //最大値IDを取得
        int maxId = fileMapper.getMaxId();

        //追加するデータを作成
        File file = new File();
        //最大値IDの次の番号をIDとしてデータを作成
        file.setFileId(maxId + 1);
        file.setFileName(uploadFile.getOriginalFilename());
        try{
            file.setFileObj(uploadFile.getInputStream());
        }catch(Exception e){
            System.err.println(e);
        }
        //DBに1件追加
        fileMapper.insert(file);

        //更新したDBからファイルリストを生成しビューに表示
        return dispFileList(model);
    }

    /**
     * ファイルダウンロード処理
     * @param id ファイルID
     * @param response HttpServletResponse
     * @return 画面遷移先(nullを返す)
     */
    @RequestMapping("/download")
    @Transactional
    public String download(@RequestParam("download_id") int id, HttpServletResponse response){
        //ダウンロード対象のファイルデータを取得
        File file = fileMapper.findById(id);

        //レスポンスにダウンロードファイルの情報を設定
        //バイナリー形式を指定
        response.setContentType("application/octet-stream");
        //ダウンロード時のファイル名を指定
        //※エンコードがうまくいってないので文字化けします※
        response.setHeader("Content-Disposition","attachment;filename*=utf-8''" + file.getFileName());

        //ダウンロードファイルへ出力
        try{
	    	OutputStream os = response.getOutputStream();
	    	InputStream is = file.getFileObj();
	        byte[] buff = new byte[1024];
	        int len = 0;
	        //入力ストリームのバイトが終わりに達するまで取得し、出力ストリームに書き込む
	        //終わりに達したら-1を返す
	        while ((len = is.read(buff, 0, buff.length)) != -1) {
	            os.write(buff, 0, len);
	        }
	        //バッファにためたデータを書き込む
	        os.flush();
        }catch(Exception e){
            System.err.println(e);
        }

        //Responseを直接指定しているため、画面遷移先はnullを指定
        return null;
    }
}
