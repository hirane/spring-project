package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.fileServer.entity.File;

@Mapper
public interface FileMapper {

	/**
     * ファイルデータテーブル(file_data)から全件取得する
     * @return ファイルデータテーブル(file_data)のデータリスト
     */
    @Select("SELECT file_id as fileId, file_name as fileName, file_obj as fileObj "
            + " FROM file_data ORDER BY file_id asc")
    public List<File> findAll();

    /**
     * 指定したIDをもつファイルデータテーブル(file_data)のデータを取得する
     * @param id ID
     * @return ファイルデータテーブル(file_data)の指定したIDのデータ
     */
    @Select("SELECT file_id as fileId, file_name as fileName, file_obj as fileObj "
            + " FROM file_data WHERE file_id = #{fileId}")
    public File findById(int id);

    /**
     * ファイルデータテーブル(file_data)の最大値IDを取得する
     * @return ファイルデータテーブル(file_data)の最大値ID
     */
    //COALESCE:選択値がnullの場合、指定地に置き換える
    @Select("SELECT COALESCE(max(file_id), 0) as maxId FROM file_data")
    int getMaxId();

    /**
     * 指定したファイルデータテーブル(file_data)のデータを追加する
     * @param fileData　ファイルデータテーブル(file_data)
     */
    @Insert("INSERT INTO file_data ( file_id, file_name, file_obj ) "
            + " VALUES ( #{fileId}, #{fileName}, #{fileObj} )")
    void insert(File file);
}
