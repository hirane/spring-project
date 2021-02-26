package com.fileServer.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

public class RequestSizeValidator implements ConstraintValidator<RequestSize, MultipartFile[]> {

	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxRequestSize;

	@Override
	public void initialize(RequestSize constraint) {
//		max = constraint.max();
	}

	@Override
	public boolean isValid(MultipartFile[] multipartFile, ConstraintValidatorContext context) {
		long max = Long.parseLong(maxRequestSize.replaceAll("[^\\d]", ""));
		boolean bool = true;
		double sizeByMb = 0;
		if(multipartFile == null) {
			return bool;
		}
		for(MultipartFile file: multipartFile) {
			//multipartFileのファイルサイズがsizeByMb以内の場合にtrueを返す
			sizeByMb += file.getSize() / 1024 / 1024;
			if(max < sizeByMb) {
				bool = false;
				break;
			}
		}
		return bool;
	}

}
