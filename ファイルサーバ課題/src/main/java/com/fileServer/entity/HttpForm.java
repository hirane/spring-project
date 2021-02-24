package com.fileServer.entity;

import org.springframework.web.multipart.MultipartFile;

import com.fileServer.validation.FileRequired;
import com.fileServer.validation.FileSize;
import com.fileServer.validation.RequestSize;

import lombok.Data;

@Data
public class HttpForm {

	@FileRequired
	@FileSize
	@RequestSize
	private MultipartFile[] multipartFile;

}
