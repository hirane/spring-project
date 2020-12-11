package com.fileServer.entity;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class File {
	//ファイルID
	private int fileId;

	//テーブルに存在するIDの最大値
	private int maxFileId;

	//ファイル名
	private String fileName;

	//ファイルパス(バイナリーコード)
	private InputStream fileObj;

	//更新日
	private String updateDate;

	//更新者
	private String updateUser;
}
