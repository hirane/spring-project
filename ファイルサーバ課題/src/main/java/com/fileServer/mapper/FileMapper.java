package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

	//filesテーブルの対象のIDのレコードを全データ取得
	@Select("SELECT file_id AS fileId, file_name AS fileName, file_path AS filePath, "
			+ "create_date AS createDate, create_user AS createUser, update_date AS updateDate, update_user AS updateUser "
			+ "FROM files "
			+ "WHERE file_id = #{fileId};"
			)
	public FileData findById(int fileId);

	//ファイルをテーブルに追加する
	@Insert("INSERT INTO files (file_name, file_path, create_date, create_user, update_date, update_user) "
			+ "VALUES (#{fileName}, #{filePath}, "
			+ "#{createDate}, #{createUser}, #{updateDate}, #{updateUser});"
			)
	public void insert(FileData fileData);


	//更新日と更新者を、file_pathから取得
	@Select("SELECT file_name AS fileName, file_path AS filePath, "
			+ "create_date AS createDate, create_user AS createUser, update_date AS updateDate, update_user AS updateUser "
			+ "FROM files "
			+ "WHERE file_path = #{filePath} "
			+ "ORDER BY file_name ASC;"
			)
	public FileData findByFilePath(String filePath);

	@Delete("DELETE FROM files")
	public void clearFilesTable();

	@Select("SELECT EXISTS (SELECT * "
//			+ "create_date AS createDate, create_user AS createUser, update_date AS updateDate, update_user AS updateUser "
			+ "FROM files WHERE file_path = #{filePath});"
			)
	public boolean isExistFile(String filePath);


	//file_idの最大値取得
	//file_idをSERIAL型にできなかったため、BIGINT型で設定したため
//	@Select("SELECT COALESCE(max(file_id), 0) AS maxId FROM files")
//	public Long getMaxId();

//	レコードを削除する
//	file_objを削除するメソッドも必要か
//	public void deleteById(long fileId);

}
