package com.fileServer.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


//アノテーションをつける対象
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
/*
 * アノテーションが影響する範囲を記述
 * Retention は RetentionPolicy をリターンし、RetentionPolicy の種類は三つのみ
 * SOURCE : このアノテーションはコンパイル時に破棄される
 * CLASS(デフォルト) : class ファイルに記録はされるが、実行時保持はされない
 * RUNTIME : class ファイルに記録され、かつ、実行時に参照できる == 実行時 JVM にこのアノテーションの情報が読み込まれる
 *
 */
@Retention(RetentionPolicy.RUNTIME)
//制約(チェック)したい具体的なロジックを記述したクラスを指定
@Constraint(validatedBy = {FileSizeValidator.class})
public @interface FileSize {
	//不正入力時に警告として出したいメッセージ
	//キーは完全修飾クラス名で記述
	String message() default "{validation.FileSize.message}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
//	long max();

	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	//ConstraintValidatorの実装によってチェック可能な対象(複数可能)を定義
	@interface List {
		FileSize[] value();
	}

}
