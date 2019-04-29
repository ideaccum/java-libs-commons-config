package org.ideaccum.libs.commons.config.exception;

/**
 * プロパティリソースに対する操作時の入出力例外が発生した場合にスローされる例外クラスです。<br>
 * 
 * @author Kitagawa<br>
 * 
 *<!--
 * 更新日		更新者			更新内容
 * 2010/07/03	Kitagawa		新規作成
 * 2018/05/02	Kitagawa		再構築(SourceForge.jpからGitHubへの移行に併せて全面改訂)
 *-->
 */
public final class ConfigIOException extends RuntimeException {

	/**
	 * コンストラクタ<br>
	 * @param cause ルート例外
	 */
	public ConfigIOException(Throwable cause) {
		super(cause);
	}
}
