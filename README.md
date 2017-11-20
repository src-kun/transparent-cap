# transparent-cap
socks5 透明代理抓取api数据，生成html form表单模板

抓取数据直接保存redis数据库中，通过generating.py脚本输出html文件

# 使用
python socks5 监听8081端口，scan_host修改为需要抓取的ip/domain，浏览器开启socks5代理
python generating.py 生成html