package org.ideaccum.libs.commons.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * プロパティアクセスキーを列挙形式のクラスとして提供します。<br>
 * <p>
 * プロパティキーのリファクタリング効率を上げるために文字列形式でのアクセスを隠蔽化してクラスフィールドとして提供します。<br>
 * このクラスはプロパティアクセスキークラスの上位抽象クラスであり、実際のプロパティキークラスはこれを継承して設置します。<br>
 * 継承関係のクラス構造を前提としたプロパティキークラスとしているため、列挙型クラスとして提供せず、列挙形式のクラスとして提供します。<br>
 * 尚、サブクラスとしてプロパティアクセスキークラスを実装する際は{@link #ConfigName(String)}コンストラクタをprotectedレベルで設置して下さい(あくまでも列挙フィールドインスタンス化の目的であり、publicとはしない)。<br>
 * </p>
 * 
 * @author Kitagawa<br>
 * 
 *<!--
 * 更新日		更新者			更新内容
 * 2010/07/03	Kitagawa		新規作成
 * 2018/05/02	Kitagawa		再構築(SourceForge.jpからGitHubへの移行に併せて全面改訂)
 * 2019/10/29	Kitagawa		ConfigNameに対してプロパティ定義値型を限定する仕様に変更
 *-->
 */
public abstract class ConfigName<T> implements Serializable {

	/** ロックオブジェクト */
	private static Object lock = new Object();

	/** プロパティキー */
	private String key;

	/** プロパティパーサークラス */
	private Class<? extends ConfigValueParser<?>> parserClass;

	/** インスタンスキャッシュ */
	private static Map<String, ConfigName<?>> instances = new HashMap<>();

	/**
	 * コンストラクタ<br>
	 * @param key プロパティキー
	 * @param parserClass プロパティパーサークラス
	 */
	protected ConfigName(String key, Class<? extends ConfigValueParser<T>> parserClass) {
		synchronized (lock) {
			this.key = key;
			this.parserClass = parserClass;
			instances.put(key, this);
		}
	}

	/**
	 * コンストラクタ<br>
	 * @param key プロパティキー
	 * @deprecated 当コンストラクタは将来的に削除予定となる為、{@link #ConfigName(String, Class)}を利用するようにしてください。
	 */
	@Deprecated
	protected ConfigName(String key) {
		this(key, null);
	}

	/**
	 * クラス情報を文字列で定義します。<br>
	 * @return クラス情報文字列
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return key;
	}

	/**
	 * 定義列挙型クラスインスタンスのプロパティキー文字列を取得します。<br>
	 * @return プロパティキー文字列
	 */
	public final String getKey() {
		return key;
	}

	/**
	 * プロパティパーサークラスを取得します。<br>
	 * @return プロパティパーサークラス
	 */
	protected final Class<? extends ConfigValueParser<?>> getParserClass() {
		return parserClass;
	}

	/**
	 * 指定されたプロパティキーのプロパティアクセスキーインスタンスを提供します。<br>
	 * 管理されていないプロパティキーの場合はnullが返却されます。<br>
	 * @param key プロパティキー文字列
	 * @return プロパティアクセスキーインスタンス
	 */
	public static final ConfigName<?> valueOf(String key) {
		if (!instances.containsKey(key)) {
			return null;
		}
		return instances.get(key);
	}
}
