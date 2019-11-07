package org.ideaccum.libs.commons.config;

/**
 * プロパティリソース内容を提供する際の値補正処理インタフェースを提供します。<br>
 * <p>
 * 定義文字列上において動的なバインド変数としての定義を行い、プロパティ取得時に実値をバインドするなどの処理を提供することができます。<br>
 * </p>
 * 
 *<!--
 * 更新日      更新者           更新内容
 * 2018/07/06  Kitagawa         新規作成
 *-->
 */
public interface ConfigValueRenderer {

	/**
	 * プロパティリソース定義内容を実際に提供する際の値補正処理を提供します。<br>
	 * @param name プロパティアクセスキー
	 * @param value プロパティ定義値
	 * @return 補正後の定義値
	 */
	public String render(ConfigName<?> name, String value);
}
