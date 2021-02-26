package com.fileServer.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

//ConstraintValidator<A extends Annotation, T> → A: interface, T: 実際の検証対象のオブジェクト
public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile[]> {

	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;
//
	//initializeはisValidが呼ばれるための初期化処理
	@Override
	public void initialize(FileSize constraint) {
//		max = Long.parseLong(maxFileSize.replaceAll("[^\\d]", ""));
	}

	//isValidは実際のバリデーションロジックを実装
	@Override
	public boolean isValid(MultipartFile[] multipartFile, ConstraintValidatorContext context) {
		long max = Long.parseLong(maxFileSize.replaceAll("[^\\d]", ""));
		boolean bool = true;
		if(multipartFile == null) {
			return bool;
		}
		for(MultipartFile file: multipartFile) {
			//multipartFileのファイルサイズがsizeByMb以内の場合にtrueを返す
			double sizeByMb = file.getSize() / 1024 / 1024;
			if(max < sizeByMb) {
				bool = false;
				break;
			}
		}
		return bool;
	}


}
