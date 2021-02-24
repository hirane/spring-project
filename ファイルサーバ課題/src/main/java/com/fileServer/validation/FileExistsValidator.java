package com.fileServer.validation;

import java.io.File;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;

//ConstraintValidator<A extends Annotation, T> → A: interface, T: 実際の検証対象のオブジェクト
public class FileExistsValidator implements ConstraintValidator<FileExists, String> {

	@Value("${WORKING_DIRECTORY}")
	private String fileServerDirectory;

	//initializeはisValidが呼ばれるための初期化処理
	@Override
	public void initialize(FileExists constraint) {}

	//isValidは実際のバリデーションロジックを実装
	//trueが返れば問題なし、falseが返ればエラー
	@Override
	public boolean isValid(String path, ConstraintValidatorContext context) {
		boolean bool = true;
		//文字列が空(null)、もしくはファイルサーバ外のパスが設定されている、もしくは設定されたパスにファイルが存在しない
		if(path == null || path.matches("^[　|\\s]*$") || !path.contains(fileServerDirectory) || !new File(path).exists()) {
			bool = false;
			return bool;
		} else {
			return bool;
		}
	}
}
