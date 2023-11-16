SemanticCutter，一个中文切词系统。

使用说明：

0. git clone https://github.com/tf-bao/SemanticCutter.git

1. cd SemanticCutter; mvn package // 构建系统

2. java -jar target/SemanticCutter-1.0.jar // 默认是把辞典转换成二进制trie文件

3. java -jar target/SemanticCutter-1.0.jar -mode server // 以服务方式启动

4. 访问浏览器: http://localhost:5353/cut?line=南京市长江大桥
