package com.fileServer.controller;

import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/error")
public class MyErrorController implements ErrorController {

	/**
	 * エラーページのパスを返す。
	 *
	 * @return エラーページのパス
	 */
	@Override
	public String getErrorPath() {
		return "";
	}

	/**
	 * エラー情報を抽出する。
	 *
	 * @param req リクエスト情報
	 * @return エラー情報
	 */
	@SuppressWarnings("deprecation")
	private static Map<String, Object> getErrorAttributes(HttpServletRequest req) {
		// DefaultErrorAttributes クラスで詳細なエラー情報を取得する
		ServletWebRequest swr = new ServletWebRequest(req);
		DefaultErrorAttributes dea = new DefaultErrorAttributes(true);
		return dea.getErrorAttributes(swr, true);
	}

	/**
	 * レスポンス用の HTTP ステータスを決める。
	 *
	 * @param req リクエスト情報
	 * @return レスポンス用 HTTP ステータス
	 */
	private static ModelAndView getHttpStatus(HttpServletRequest req, ModelAndView mav) {
		// HTTP ステータスを決める
		// ここでは 404と403 以外は全部 500 にし、HTTPステータスと遷移先をセットして返す
		Object statusCode = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if (statusCode != null && statusCode.toString().equals("404")) {
			status = HttpStatus.NOT_FOUND;
			mav.setStatus(status);
			mav.setViewName("error/404");
		} else if(statusCode != null && statusCode.toString().equals("403")) {
			status = HttpStatus.FORBIDDEN;
			mav.setStatus(status);
			mav.setViewName("error/403");
		} else {
			mav.setStatus(status);
			mav.setViewName("error/500");
		}
		return mav;
	}

	/**
	 * HTML レスポンス用の ModelAndView オブジェクトを返す。
	 *
	 * @param req リクエスト情報
	 * @param mav レスポンス情報
	 * @return HTML レスポンス用の ModelAndView オブジェクト
	 */
	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView errorHtml(HttpServletRequest req, ModelAndView mav) {

		// エラー情報を取得
		Map<String, Object> attr = getErrorAttributes(req);
		log.info(attr.toString());
		// HTTP ステータスを決める
//		HttpStatus status = getHttpStatus(req, mav);
		mav = getHttpStatus(req, mav);

		// 出力したい情報をセットする
		mav.addObject("status", mav.getStatus().value());
//		mav.addObject("timestamp", attr.get("timestamp"));
//		mav.addObject("error", attr.get("error"));
//		mav.addObject("exception", attr.get("exception"));
//		mav.addObject("message", attr.get("message"));
//		mav.addObject("errors", attr.get("errors"));
//		mav.addObject("trace", attr.get("trace"));
//		mav.addObject("path", attr.get("path"));

		return mav;
	}


}
