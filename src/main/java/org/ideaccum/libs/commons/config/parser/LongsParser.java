package org.ideaccum.libs.commons.config.parser;

import java.util.LinkedList;
import java.util.List;

import org.ideaccum.libs.commons.config.ConfigValueParser;
import org.ideaccum.libs.commons.util.StringUtil;

/**
 * 定義値を長整数値配列としてパースする処理を提供します。<br>
 * <p>
 * プロパティ定義値を長整数値配列としてパースして提供します。<br>
 * </p>
 * 
 * @author Kitagawa<br>
 * 
 *<!--
 * 更新日		更新者			更新内容
 * 2019/10/29	Kitagawa		新規作成
 *-->
 */
public class LongsParser implements ConfigValueParser<Long[]> {

	/**
	 * プロパティリソース定義文字列内容を実際に利用する際の型にパースして提供します。<br>
	 * @param value プロパティ定義値
	 * @return パース語定義値
	 * @see org.ideaccum.libs.commons.config.ConfigValueParser#parse(java.lang.String)
	 */
	@Override
	public Long[] parse(String value) {
		if (value == null) {
			return new Long[0];
		}
		String[] tokens = new StringsParser().parse(value);
		List<Long> list = new LinkedList<>();
		for (String token : tokens) {
			list.add(new Long(StringUtil.toPLong(token)));
		}
		return list.toArray(new Long[0]);
	}
}
