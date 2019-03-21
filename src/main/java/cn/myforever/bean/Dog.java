package cn.myforever.bean;

import cn.myforever.annotation.Bean;
import cn.myforever.annotation.Source;

@Bean
public class Dog {
	@Source
	private User user;
	public void print() {
		System.out.println("我是狗子");
	}
	public void MyUser() {
		user.print();
	}
}
