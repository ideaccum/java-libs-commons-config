package org.ideaccum.libs.commons.config;

/**
 * プロパティリソース内容を提供する際の値パース処理インタフェースを提供します。<br>
 * <p>
 * 定義上の文字列をJava側で理想する際の目的の方にパースするための処理を提供します。<br>
 * </p>
 * 
 *<!--
 * 更新日      更新者           更新内容
 * 2018/07/06  Kitagawa         新規作成
 *-->
 */
public interface ConfigValueParser<T> {

	/**
	 * プロパティリソース定義文字列内容を実際に利用する際の型にパースして提供します。<br>
	 * @param value プロパティ定義値
	 * @return パース語定義値
	 */
	public T parse(String value);
}
