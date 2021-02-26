package com.fileServer.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class FileData implements Serializable {

//	シリアルバージョンID
	private static final long serialVersionUID = 1L;

//	ファイルID→SERIAL型のため自動採番
	private int fileId;

//	ファイル名
	private String fileName;

//	ファイルのフルパス→階層の取得に使用
	private String filePath;

//	登録日時
	private String createDate;

//	登録者
	private String createUser;

//	更新日時
	private String updateDate;

//	更新者
	private String updateUser;

}
