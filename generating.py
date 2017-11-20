#! usr/bin/python 
#coding=utf-8 

from oredis import ORedis

class Generat:

	def __init__(self, headers):
		self.__html = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>api test</title>
	</head>
	<body >{form}
	</body>
</html>
"""
		#view = 1 开启视图模式，将header写到页面内方便查看
		self.view = 0
		self.__form = """
		{view}
		<form action="{action}" method="{method}">
			{input}<input type="submit">
		</form>"""
		self.__input = """<input type="text" name="{name}" value="{value}">
			"""
		self.__headers = headers
		self.__als_headers = []
		self.als_html = ''
	
	#获取解析结果
	def get_als_headers(self):
		if not self.__als_headers:
			self.__analysis()
		return self.__als_headers
	
	#解析请求头
	def __analysis(self):
		for header in self.__headers:
			hds = {'method':'', 'params':{}, 'action':'', 'header':''}
			params = []
			hds['header'] = header.values()[0]
			hd = header.values()[0].split('\r\n')
			#TODO GET POST DELETE PUT
			#GET
			if hd[0][0] == 'G':
				hds['method'] = 'GET'
				up = hd[0].split(' ')[1].split("?")
				hds['action'] = up[0]
				if len(up) == 2:
					params = up[1].split("&")
			else:
				hds['action'] = hd[0].split(' ')[1]
				hds['method'] = 'POST'
				params = hd[len(hd) - 1].split("&")
			
			for param in params:
				p = param.split('=')
				hds['params'][p[0]] = p[1]
			self.__als_headers.append(hds)
		return self.__als_headers
	
	#根据模板生成input
	def __tem_input(self, params):
		input = ''
		for param in params:
			input += self.__input.replace('{name}', param).replace('{value}', params[param])
		if not params:
			input += """<input value='null' type="text">
			"""
		return input
	    
	#根据模板生成form
	def __tem_form(self, inputs, action, method, header):
		form = self.__form.replace('{input}', inputs).replace('{action}', action).replace('{method}', method)
		if self.view:
			form = form.replace('{view}', header.replace('\r\n','<br>'))
		else:
			form = form.replace('{view}', action)
		return form
	
	#生产html页面
	def tem_html(self):
		self.__analysis()
		forms = ''
		for header in self.__als_headers:
			inputs = self.__tem_input(header['params'])
			forms += self.__tem_form(inputs, header['action'], header['method'],  header['header'])
		self.als_html = self.__html.replace('{form}', forms)
	
	#输出
	def output(self, path):
		f = open('test.html', 'w')
		f.write(self.als_html)
		f.close()

ors = ORedis('192.168.5.131', 6379)
generat = Generat(ors.iteritems())
generat.tem_html()
#print generat.als_html
generat.output('./test.html')
#print generat.get_als_headers()