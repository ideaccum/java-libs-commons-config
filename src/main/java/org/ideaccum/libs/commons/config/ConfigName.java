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
 *-->
 */
public abstract class ConfigName implements Serializable {

	/** ロックオブジェクト */
	private static Object lock = new Object();

	/** プロパティキー */
	private String key;

	/** インスタンスキャッシュ */
	private static Map<String, ConfigName> instances = new HashMap<>();

	/**
	 * コンストラクタ<br>
	 * @param key プロパティキー
	 */
	protected ConfigName(String key) {
		synchronized (lock) {
			this.key = key;
			instances.put(key, this);
		}
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
	 * 指定されたプロパティキーのプロパティアクセスキーインスタンスを提供します。<br>
	 * 管理されていないプロパティキーの場合はnullが返却されます。<br>
	 * @param key プロパティキー文字列
	 * @return プロパティアクセスキーインスタンス
	 */
	public static final ConfigName valueOf(String key) {
		if (!instances.containsKey(key)) {
			return null;
		}
		return instances.get(key);
	}
}
