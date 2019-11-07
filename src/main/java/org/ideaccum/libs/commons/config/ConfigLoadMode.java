package org.ideaccum.libs.commons.config;

/**
 * プロパティ定義情報読み込み時のモードを提供します。<br>
 * <p>
 * この列挙型で提供される読み込みモードは{@link org.ideaccum.libs.commons.config.Config#load(String, ConfigLoadMode)}で利用します。<br>
 * </p>
 * 
 *<!--
 * 更新日      更新者           更新内容
 * 2018/05/02  Kitagawa         新規作成
 *-->
 */
public enum ConfigLoadMode {

	/** 既に定義されているプロパティは置き換えずに追加読み込みします */
	SKIP_EXISTS, //

	/** 既に定義されているプロパティは置き換える形で追加読み込みします(ディフォルト) */
	REPLACE_EXISTS, //

	/** 存在するプロパティをクリアして読み込み対象のプロパティですべての定義を置き換えます */
	REPLACE_ALL, //
}
