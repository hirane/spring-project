package com.fileServer.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

//ConstraintValidator<A extends Annotation, T> → A: interface, T: 実際の検証対象のオブジェクト
public class FileRequiredValidator implements ConstraintValidator<FileRequired, MultipartFile[]> {

	//initializeはisValidが呼ばれるための初期化処理
	@Override
	public void initialize(FileRequired constraint) {}

	//isValidは実際のバリデーションロジックを実装
	@Override
	public boolean isValid(MultipartFile[] multipartFile, ConstraintValidatorContext context) {
		//multipartFileの値がnullではなく、かつファイル名が空ではない場合にtrueを返す
		boolean bool = true;
		if(multipartFile == null) {
			bool = false;
			return bool;
		}
		for(MultipartFile file: multipartFile) {
			if(file == null) {
				bool = false;
				break;
			}
			if(file.getOriginalFilename().isEmpty()) {
				bool = false;
				break;
			}
		}
		return bool;
	}


}
