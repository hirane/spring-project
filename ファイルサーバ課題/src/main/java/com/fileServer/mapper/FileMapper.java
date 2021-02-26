package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.fileServer.entity.FileData;

@Mapper
public interface FileMapper {

	/**
	 * INSERT処理
	 * 個別にファイルをテーブルに追加する
	 * @param FileData fileData
	 * @return
	 */
	public void insert(FileData fileData);

	/**
	 * INSERT処理
	 * リスト内の全ファイルをテーブルに追加する
	 * @param List<FileData> fileData
	 * @return
	 */
	public void insertAll(List<FileData> fileData);

	/**
	 * SELECT処理
	 * ファイルパスをキーに情報検索
	 * @param String filePath
	 * @return
	 */
	public FileData findFileAllInfo(String filePath);

	/**
	 * SELECT処理
	 * file_id、file_name、file_athの情報を取得する
	 * @param String filePath
	 * @return
	 */
	public FileData findFileIdNamePath(String filePath);

	/**
	 * SELECT処理
	 * 指定されたfile_pathのデータがDBにあるかを確認する
	 * @param String filePath
	 * @return
	 */
	public boolean isExistFile(String filePath);

	/**
	 * UPDATE処理
	 * 移動・リネームの際に情報を更新する
	 * @param String filePath
	 * @param @Param("fileData") FileData fileData
	 * @return
	 */
	public void updateByRename(String oldFilePath, @Param("fileData") FileData fileData);

	/**
	 * DELETE処理
	 * 指定されたfile_pathのレコードを削除する。パスの指定がnullの場合は全レコード削除
	 * @param String filePath
	 * @return
	 */
	public void delete(String filePath);


}
