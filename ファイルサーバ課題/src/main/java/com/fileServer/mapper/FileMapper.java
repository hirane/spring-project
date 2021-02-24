package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.fileServer.entity.FileData;

@Mapper
public interface FileMapper {

	//テーブル内の全リストを検索する
	@Select("SELECT file_id AS fileId, file_name AS fileName, file_path AS filePath, "
			+ "create_date AS createDate, create_user AS createUser, update_date AS updateDate, update_user AS updateUser "
			+ "FROM files "
			+ "ORDER BY file_name ASC;"
			)
	public List<FileData> findAll();

	/*
	//filesテーブルの対象のIDのレコードを全データ取得
	@Select("SELECT file_id AS fileId, file_name AS fileName, file_path AS filePath, "
			+ "create_date AS createDate, create_user AS createUser, update_date AS updateDate, update_user AS updateUser "
			+ "FROM files "
			+ "WHERE file_id = #{fileId};"
			)
	public FileData findById(int fileId);
	*/

	//ファイルをテーブルに追加する
	@Insert("INSERT INTO files (file_name, file_path, create_date, create_user, update_date, update_user) "
			+ "VALUES (#{fileName}, #{filePath}, "
			+ "#{createDate}, #{createUser}, #{updateDate}, #{updateUser});"
			)
	public void insert(FileData fileData);


	//更新日と更新者を、file_pathから取得
	@Select("SELECT file_id AS fileId, file_name AS fileName, file_path AS filePath, "
			+ "create_date AS createDate, create_user AS createUser, update_date AS updateDate, update_user AS updateUser "
			+ "FROM files "
			+ "WHERE file_path = #{filePath} "
			+ "ORDER BY file_name ASC;"
			)
	public FileData findFileAllInfo(String filePath);


	//file_id、filr_name、file_athの情報を取得する
	@Select("SELECT file_id AS fileId, file_name AS fileName, file_path AS filePath "
			+ "FROM files "
			+ "WHERE file_path = #{filePath} "
			+ "ORDER BY file_path ASC;"
			)
	public FileData findFileIdNamePath(String filePath);


	//filesテーブルのすべてのデータを削除する
	@Delete("DELETE FROM files")
	public void clearFilesTable();


	//指定されたfile_pathのデータがDBにあるかを確認する
	@Select("SELECT EXISTS (SELECT * FROM files WHERE file_path = #{filePath});")
	public boolean isExistFile(String filePath);


	//移動・リネームの際に情報を更新する
	@Update("UPDATE files "
			+ "SET file_path = #{fileData.filePath}, file_name = #{fileData.fileName}, update_date = #{fileData.updateDate},  update_user = #{fileData.updateUser} "
			+ "WHERE file_path = #{oldFilePath};"
			)
	public void updateByRename(String oldFilePath, @Param("fileData") FileData fileData);


	//指定されたfile_pathのレコードを削除する
	@Delete("DELETE FROM files WHERE file_path = #{filePath};")
	public void delete(String filePath);







}
