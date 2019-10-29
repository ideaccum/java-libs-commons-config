package org.ideaccum.libs.commons.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ideaccum.libs.commons.config.exception.ConfigIOException;
import org.ideaccum.libs.commons.util.ClassUtil;
import org.ideaccum.libs.commons.util.CollectionUtil;
import org.ideaccum.libs.commons.util.PropertiesUtil;
import org.ideaccum.libs.commons.util.ResourceUtil;
import org.ideaccum.libs.commons.util.StringUtil;
import org.reflections.Reflections;

/**
 * 外部定義されたプロパティリソースへのアクセスを行うためのインタフェースを提供します。<br>
 * <p>
 * このクラスインスタンスはアプリケーション実行中に単一のインスタンスを保証するシングルトンクラスとなります。<br>
 * 利用者はクラスが参照するプロパティリソース情報を追加し、{@link org.ideaccum.libs.commons.config.ConfigName}又は、それを継承したアクセスキークラスでプロパティにアクセスします。<br>
 * </p>
 * 
 * @author Kitagawa<br>
 * 
 *<!--
 * 更新日		更新者			更新内容
 * 2010/07/03	Kitagawa		新規作成
 * 2018/05/02	Kitagawa		再構築(SourceForge.jpからGitHubへの移行に併せて全面改訂)
 * 2019/05/04	Kitagawa		ConfigName継承サブクラスを設置した際にサブクラスを参照する前にgetMapを利用するとキーセットが取得できないため、reflection,jarライブラリを利用して強制的にサブクラスをクラスロードするように修正
 * 2019/10/29	Kitagawa		ConfigNameに対してプロパティ定義値型を限定する仕様とし、get*****系のメソッドをDeplicatedに変更({@link #get(ConfigName)}を追加)
 *-->
 */
public final class Config implements Serializable {

	/** ロックオブジェクト */
	private static Object lock = new Object();

	/** クラスインスタンス */
	private static Config instance = new Config();

	/** 環境設定プロパティオブジェクト */
	private Properties properties;

	/** プロパティ定義内容レンダラオブジェクト */
	private ConfigValueRenderer renderer;

	/** プロパティパースオブジェクト */
	private Map<Class<? extends ConfigValueParser<?>>, ConfigValueParser<?>> parsers;

	static {
		Reflections reflections = new Reflections();
		for (Class<?> clazz : reflections.getSubTypesOf(ConfigName.class)) {
			//System.out.println("ConfigName sub types : " + clazz.getName());
			try {
				Class.forName(clazz.getName());
			} catch (Throwable e) {
				// Dummy class initialize for class load
			}
		}
	}

	/**
	 * コンストラクタ<br>
	 */
	private Config() {
		super();
		this.properties = new Properties();
		this.renderer = null;
		this.parsers = new HashMap<>();
	}

	/**
	 * プロパティリソース内容を読み込みクラスインスタンスに展開します。<br>
	 * @param properties プロパティリソースパス
	 * @param mode プロパティ読み込み時の挙動(現在管理されているプロパティ情報を破棄して新たに読み込みます)
	 */
	public static synchronized void load(String properties, ConfigLoadMode mode) {
		synchronized (lock) {
			try {
				/*
				 * 対象プロパティ読み込み
				 */
				Properties loaded = new Properties();
				if (!StringUtil.isEmpty(properties) && ResourceUtil.exists(properties)) {
					loaded = PropertiesUtil.load(properties);
				}

				/*
				 * 読み込みモードごと処理
				 */
				if (mode == ConfigLoadMode.REPLACE_ALL || mode == null) {
					// すべてのプロパティを置き換える場合は現状の保持情報をクリア
					instance.properties.clear();
					instance.properties.putAll(loaded);
				} else if (mode == ConfigLoadMode.REPLACE_EXISTS) {
					// 既存プロパティに対しては上書きする場合は読み込んだプロパティをプット
					instance.properties.putAll(loaded);
				} else if (mode == ConfigLoadMode.SKIP_EXISTS) {
					// 既存プロパティに対しては現状維持とする場合はプロパティごとに判定しながらプット
					for (Object key : loaded.keySet()) {
						if (instance.properties.containsKey(key)) {
							continue;
						}
						Object value = loaded.get(key);
						instance.properties.put(key, value);
					}
				}
			} catch (Throwable e) {
				throw new ConfigIOException(e);
			}
		}
	}

	/**
	 * プロパティリソース内容を読み込みクラスインスタンスに展開します。<br>
	 * @param properties プロパティリソースパス
	 * @param append 現状のプロパティ内容に追加する形でプロパティリソースを読み込む場合にtrueを指定
	 * @deprecated {@link #load(String, ConfigLoadMode)}を利用してください(2018/05/02 deplicated)
	 */
	@Deprecated
	public static synchronized void load(String properties, boolean append) {
		if (append) {
			load(properties, ConfigLoadMode.REPLACE_EXISTS);
		} else {
			load(properties, ConfigLoadMode.REPLACE_ALL);
		}
	}

	/**
	 * プロパティリソース内容を読み込みクラスインスタンスに展開します。<br>
	 * このメソッドによる読み込みは現在管理されているプロパティ情報を破棄して新たに読み込みます。<br>
	 * 読み込み方法を指定してプロパティを反映する場合は{@link #load(String, ConfigLoadMode)}を利用して下さい。<br>
	 * @param properties プロパティリソースパス
	 */
	public static synchronized void load(String properties) {
		load(properties, ConfigLoadMode.REPLACE_ALL);
	}

	/**
	 * 管理されているプロパティ情報を全てクリアします。<br>
	 */
	public static synchronized void destroy() {
		synchronized (lock) {
			instance.properties.clear();
		}
	}

	/**
	 * プロパティ定義内容レンダラオブジェクトを設定します。<br>
	 * レンダラオブジェクトを設定した場合、各種プロパティ値取得時にレンダラ処理で値補正が行われたうえで値が提供されます。<br>
	 * @param renderer プロパティ定義内容レンダラオブジェクト
	 */
	public static synchronized void setRenderer(ConfigValueRenderer renderer) {
		synchronized (lock) {
			instance.renderer = renderer;
		}
	}

	/**
	 * プロパティ上に管理されている値を必要に応じて補正した文字列で提供します。<br>
	 * @param name プロパティアクセスキー
	 * @param object プロパティ定義情報
	 * @return 必要に応じて補正した文字列
	 */
	private static String bind(ConfigName<?> name, Object object) {
		String value = object == null ? "" : object.toString();
		if (instance.renderer != null) {
			return instance.renderer.render(name, value);
		} else {
			return value;
		}
	}

	/**
	 * 管理されているプロパティ情報をマップ形式で取得します。<br>
	 * @return プロパティ情報マップオブジェクト
	 */
	public static Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		for (Object setkey : instance.properties.keySet()) {
			String key = setkey.toString();
			Object value = instance.properties.get(key);
			map.put(key.toString(), bind(ConfigName.valueOf(key), value));
		}
		return map;
	}

	/**
	 * 管理されているプロパティ情報をピリオド(".")をデリミタとして見なした階層形式のマップ形式として取得します。<br>
	 * foo.barというキーで定義されている場合、fooというキーで保持されたMap内にbarというキーで定義値が保持されます。<br>
	 * 当メソッドはVelocity等でEL表記アクセスを行うために設けられました。<br>
	 * @return 階層化されたプロパティ定義マップオブジェクト
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getTree() {
		Map<String, Object> map = new HashMap<>();
		// ↓既にバインド変数が埋め込まれたマップ情報を利用する
		//for (Object setkey : instance.properties.keySet()) {
		Map<String, String> rendered = getMap();
		for (String key : rendered.keySet()) {
			String[] tokens = key.split("\\.");
			Map<String, Object> active = map;
			for (int i = 0; i <= tokens.length - 2; i++) {
				String token = tokens[i];
				if (!active.containsKey(token)) {
					active.put(token, new HashMap<>());
				}
				Object entry = active.get(token);
				if (entry instanceof Map) {
					active = (Map<String, Object>) active.get(token);
				} else {
					// 階層途中のトークンで定義されているものはマップで上書き(外部からEL参照は不可となる)
					Map<String, Object> newmap = new HashMap<>();
					newmap.put("", entry);
					active.put(token, newmap);
					active = (Map<String, Object>) active.get(token);
				}
			}
			active.put(tokens[tokens.length - 1], instance.properties.get(key));
		}
		return map;
	}

	/**
	 * 管理されているプロパティキーを{@link org.ideaccum.libs.commons.config.ConfigName}形式で取得します。<br>
	 * 但し、{@link org.ideaccum.libs.commons.config.ConfigName}として提供されないキーは除外されて提供されます。<br>
	 * @return 管理されているプロパティキー
	 */
	public static Set<ConfigName<?>> getNames() {
		Set<ConfigName<?>> set = new HashSet<>();
		for (Object key : instance.properties.keySet()) {
			ConfigName<?> name = ConfigName.valueOf(key.toString());
			if (name != null) {
				set.add(name);
			}
		}
		return set;
	}

	/**
	 * プロパティ情報を個別に設定します。<br>
	 * 通常は{@link #load(String)}又は、{@link #load(String, boolean)}によってプロパティリソースを読み込んで利用を行う事を想定したクラスですが、
	 * 動的なプロパティ設定等を行う可能性を考慮して設置されたメソッドです。<br>
	 * @param name プロパティアクセスキー
	 * @param value プロパティ値
	 */
	public static void put(ConfigName<?> name, String value) {
		if (name == null) {
			throw new NullPointerException();
		}
		instance.properties.put(name.getKey(), value);
	}

	/**
	 * プロパティ情報を取得します。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(ConfigName<T> name) {
		if (name == null) {
			return null;
		}
		String value = instance.properties.getProperty(name.getKey());
		String render = bind(name, value);
		if (!instance.parsers.containsKey(name.getParserClass())) {
			instance.parsers.put(name.getParserClass(), ClassUtil.createInstance(name.getParserClass()));
		}
		return (T) instance.parsers.get(name.getParserClass()).parse(render);
	}

	/**
	 * プロパティ情報を文字列情報として取得します。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 */
	public static String getProperty(ConfigName<?> name) {
		if (name == null) {
			return null;
		}
		String value = instance.properties.getProperty(name.getKey());
		String render = bind(name, value);
		return render;
	}

	/**
	 * プロパティ情報を文字列値として取得します。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 */
	public static boolean isEmpty(ConfigName<?> name) {
		if (name == null) {
			return true;
		}
		return !instance.properties.containsKey(name.getKey()) || StringUtil.isEmpty(instance.properties.getProperty(name.getKey()));
	}

	/**
	 * プロパティ情報を文字列値として取得します。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static String getString(ConfigName<?> name) {
		if (name == null) {
			return "";
		}
		String value = instance.properties.getProperty(name.getKey());
		return bind(name, value);
	}

	/**
	 * プロパティ情報を文字列値配列として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static String[] getStrings(ConfigName<?> name, boolean enableComment) {
		String value = getString(name);
		String[] tokens = StringUtil.isEmpty(value) ? new String[0] : value.split(",");
		if (enableComment) {
			List<String> list = new LinkedList<>();
			for (String token : tokens) {
				if (token.startsWith("#")) {
					continue;
				}
				list.add(token);
			}
			return (String[]) list.toArray(new String[0]);
		} else {
			return tokens;
		}
	}

	/**
	 * プロパティ情報を文字列値配列として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getStrings(ConfigName, boolean)}を利用して下さい。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static String[] getStrings(ConfigName<?> name) {
		return getStrings(name, true);
	}

	/**
	 * プロパティ情報を真偽値として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static boolean getBoolean(ConfigName<?> name) {
		String value = getString(name);
		return StringUtil.toPBoolean(value);
	}

	/**
	 * プロパティ情報を真偽値として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static boolean[] getBooleans(ConfigName<?> name, boolean enableComment) {
		String[] values = getStrings(name, enableComment);
		List<Boolean> list = new LinkedList<>();
		for (String value : values) {
			list.add(StringUtil.toPBoolean(value));
		}
		return CollectionUtil.cast(list.toArray(new Boolean[0]));
	}

	/**
	 * プロパティ情報を真偽値として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getBooleans(ConfigName, boolean)}を利用して下さい。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static boolean[] getBooleans(ConfigName<?> name) {
		return getBooleans(name, true);
	}

	/**
	 * プロパティ情報を長整数値として取得します。<br>
	 * プロパティが未定義の場合は0が返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static long getLong(ConfigName<?> name) {
		String value = getString(name);
		return StringUtil.toPLong(value);
	}

	/**
	 * プロパティ情報を長整数値として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static long[] getLongs(ConfigName<?> name, boolean enableComment) {
		String[] values = getStrings(name, enableComment);
		List<Long> list = new LinkedList<>();
		for (String value : values) {
			list.add(StringUtil.toPLong(value));
		}
		return CollectionUtil.cast(list.toArray(new Long[0]));
	}

	/**
	 * プロパティ情報を長整数値として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getLongs(ConfigName, boolean)}を利用して下さい。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static long[] getLongs(ConfigName<?> name) {
		return getLongs(name, true);
	}

	/**
	 * プロパティ情報を整数値として取得します。<br>
	 * プロパティが未定義の場合は0が返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static int getInt(ConfigName<?> name) {
		String value = getString(name);
		return StringUtil.toPInt(value);
	}

	/**
	 * プロパティ情報を整数値として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static int[] getInts(ConfigName<?> name, boolean enableComment) {
		String[] values = getStrings(name, enableComment);
		List<Integer> list = new LinkedList<>();
		for (String value : values) {
			list.add(StringUtil.toPInt(value));
		}
		return CollectionUtil.cast(list.toArray(new Integer[0]));
	}

	/**
	 * プロパティ情報を整数値として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getInts(ConfigName, boolean)}を利用して下さい。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static int[] getInts(ConfigName<?> name) {
		return getInts(name, true);
	}

	/**
	 * プロパティ情報を整数値として取得します。<br>
	 * プロパティが未定義の場合は0が返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static short getShort(ConfigName<?> name) {
		String value = getString(name);
		return StringUtil.toPShort(value);
	}

	/**
	 * プロパティ情報を整数値として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static short[] getShorts(ConfigName<?> name, boolean enableComment) {
		String[] values = getStrings(name, enableComment);
		List<Short> list = new LinkedList<>();
		for (String value : values) {
			list.add(StringUtil.toPShort(value));
		}
		return CollectionUtil.cast(list.toArray(new Short[0]));
	}

	/**
	 * プロパティ情報を整数値として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getShorts(ConfigName, boolean)}を利用して下さい。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static short[] getShorts(ConfigName<?> name) {
		return getShorts(name, true);
	}

	/**
	 * プロパティ情報を浮動小数値として取得します。<br>
	 * プロパティが未定義の場合は0が返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static double getDouble(ConfigName<?> name) {
		String value = getString(name);
		return StringUtil.toPDouble(value);
	}

	/**
	 * プロパティ情報を浮動小数値として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static double[] getDoubles(ConfigName<?> name, boolean enableComment) {
		String[] values = getStrings(name, enableComment);
		List<Double> list = new LinkedList<>();
		for (String value : values) {
			list.add(StringUtil.toPDouble(value));
		}
		return CollectionUtil.cast(list.toArray(new Double[0]));
	}

	/**
	 * プロパティ情報を浮動小数値として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getDoubles(ConfigName, boolean)}を利用して下さい。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static double[] getDoubles(ConfigName<?> name) {
		return getDoubles(name, true);
	}

	/**
	 * プロパティ情報を浮動小数値として取得します。<br>
	 * プロパティが未定義の場合は0が返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static float getFloat(ConfigName<?> name) {
		String value = getString(name);
		return StringUtil.toPFloat(value);
	}

	/**
	 * プロパティ情報を浮動小数値として取得します。<br>
	 * このメソッドではカンマ(",")区切りされているプロパティを配列定義として取得します。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @param enableComment トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外する場合にtrueを指定
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static float[] getFloats(ConfigName<?> name, boolean enableComment) {
		String[] values = getStrings(name, enableComment);
		List<Float> list = new LinkedList<>();
		for (String value : values) {
			list.add(StringUtil.toPFloat(value));
		}
		return CollectionUtil.cast(list.toArray(new Float[0]));
	}

	/**
	 * プロパティ情報を浮動小数値として取得します。<br>
	 * また、トークン先頭に"#"がある場合はコメント扱いとして取得対象から除外された状態で提供されます。<br>
	 * トークン先頭の"#"をコメント扱いとせずに全てのトークンを取得する場合は{@link #getFloats(ConfigName, boolean)}を利用して下さい。<br>
	 * プロパティが未定義の場合はfalseが返却されます。<br>
	 * @param name プロパティアクセスキー
	 * @return プロパティ情報
	 * @deprecated 当メソッドは将来的に削除予定となる為、{@link #get(ConfigName)}を利用するようにしてください。
	 */
	@Deprecated
	public static float[] getFloats(ConfigName<?> name) {
		return getFloats(name, true);
	}
}
