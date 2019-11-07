package org.ideaccum.libs.commons.config.parser;

import java.util.LinkedList;
import java.util.List;

import org.ideaccum.libs.commons.config.ConfigValueParser;
import org.ideaccum.libs.commons.util.StringUtil;

/**
 * 定義値を文字列配列としてパースする処理を提供します。<br>
 * <p>
 * プロパティ定義値を文字列配列としてパースして提供します。<br>
 * </p>
 * 
 *<!--
 * 更新日      更新者           更新内容
 * 2019/10/29  Kitagawa         新規作成
 *-->
 */
public class StringsParser implements ConfigValueParser<String[]> {

	/**
	 * プロパティリソース定義文字列内容を実際に利用する際の型にパースして提供します。<br>
	 * @param value プロパティ定義値
	 * @return パース語定義値
	 * @see org.ideaccum.libs.commons.config.ConfigValueParser#parse(java.lang.String)
	 */
	@Override
	public String[] parse(String value) {
		if (value == null) {
			return new String[0];
		}
		String[] tokens = StringUtil.isEmpty(value) ? new String[0] : value.split(",");
		List<String> list = new LinkedList<>();
		for (String token : tokens) {
			if (token.startsWith("#")) {
				continue;
			}
			list.add(token);
		}
		return (String[]) list.toArray(new String[0]);
	}
}
