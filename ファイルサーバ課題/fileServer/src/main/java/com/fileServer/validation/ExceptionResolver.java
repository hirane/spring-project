package com.fileServer.validation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ExceptionResolver implements HandlerExceptionResolver {

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
		ModelAndView mav = new ModelAndView();

		//MultipartExceptionのexception、IllegalStateExceptionのexception、FileSizeLimitExceededExceptionのexceptionの場合にエラー表示内容をコントロールする
		//→ファイルサイズオーバーの場合
		if (e instanceof MultipartException && e.getCause() instanceof IllegalStateException
				&& e.getCause().getCause() instanceof FileSizeLimitExceededException) {
			mav.setViewName("redirect:/fileView/filesize-error");
		}
		//複数ファイルのサイズオーバーの場合
		else if(e instanceof MaxUploadSizeExceededException && e.getCause() instanceof IllegalStateException
				&& e.getCause().getCause() instanceof SizeLimitExceededException) {
			mav.setViewName("redirect:/fileView/maxsize-error");
		}
		return mav;
	}

}
