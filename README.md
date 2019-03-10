# Ideaccum Commons Config
Commons Configは、外部プロパティリソース定義情報を利用する際の操作を標準化するためのクラスが提供されるパッケージです。  


外部プロパティリソース定義に関する実装は単純なjava.util.Propertiesで実現できますが、このパッケージでは、実装の標準化を目的として、主に下記の機能を提供しています。
尚、このパッケージではアプリケーションにおける全体の環境定義情報を管理する位置づけとしているため、環境定義管理機構はシングルトンで提供されます。

- 複数プロパティリソースとするうえでの追加読み込み処理  
  複数に分割されたプロパティを追加読み込みする際に、全置換、プロパティ上書き、既存プロパティスキップのモード指定で読み込み可能。  

      Config.load("prop1.properties");
      Config.load("prop2.properties", ConfigLoadMode.REPLACE_EXISTS);
      Config.load("prop3.properties", ConfigLoadMode.SKIP_EXISTS);

- リファクタリングを想定したプロパティキーの列挙クラス化
  通常のプロパティアクセスでは文字列キーのインライン実装又は、定数実装となり、キーの変更が入った際のリファクタリングが困難になることを回避するため、プロパティアクセス時には列挙形式のクラスをキーとしてアクセスすることを標準化しています。  

      public class UserConfigName extends ConfigName {
        public ConfigName FOO = new UserConfigName("prop.foo");
        public ConfigName BAR = new UserConfigName("prop.bar");
        protected UserConfigName(String key) {
          super(key);
        }
      }
      ...
      public class UserLogic {
        public void logic() {
          String foo = Config.getString(UserConfigName.FOO);
          String bar = Config.getString(UserConfigName.BAR);
        }
      }

- プロパティ取得時のキャスト簡略化  
  通常のプロパティアクセスで発生する文字列型での値取得後の必要なキャスト処理を簡略化するために、予め目的型でのプロパティアクセスメソッドを提供します。  

    public class UserLogic {
      public void logic() {
        String foo = Config.getString(UserConfigName.FOO);
        int bar = Config.getInt(UserConfigName.BAR);
        boolean hoge = Config.getBoolean(UserConfigName.HOGE);
        ...
      }

- プロパティ値の複数値定義  
  プロパティ値に複数の値を定義する場合、多くはカンマ区切り等のデリミタ文字を利用した定義になります。このパッケージではカンマ区切りされたプロパティを予め複数の定義情報として取得するアクセッサを提供します。また、複数定義した値のトークン先頭に"#"がある場合、プロパティ定義と同様にコメント扱いにする仕様として提供します。  

      prop.foo=\
      value1,\
      value2,\
      #value3,\
      value4

      public class UserLogic {
        public void logic() {
          String[] foo = Config.getStrings(UserConfigName.FOO);
          // foo[0] ... value1
          // foo[1] ... value2
          // foo[2] ... value4
        }

## Documentation
ライブラリに関するAPI仕様は各クラスのJavadocにて記載しています。  

## Source Code
最新のプログラムソースはすべて[GitHub](https://github.com/ideaccum/org.ideaccum.libs.commons.config)で管理しています。  

## Dependent Libraries
このライブラリパッケージの依存ライブラリ及び、ライセンスは[LIBRARIES.md](https://github.com/ideaccum/org.ideaccum.libs.commons.config/blob/master/LIBRARIES.md)に記載しています。  

## License
プログラムソースは[MIT License](https://github.com/ideaccum/org.ideaccum.libs.commons.config/blob/master/LICENSE.md)です。  

## Copyright
Copyright (c) 2018 Hisanori Kitagawa  

## Other
2005年より[SourceForge.jp](https://osdn.net/projects/phosphoresce/)にて公開していたリポジトリから移行し、更新しているライブラリとなります。  
