package cn.myforever.bean;

import cn.myforever.annotation.Bean;
import cn.myforever.annotation.Source;

@Bean
public class User {
	@Source
	private Dog dog;
	public void print(){
		System.out.println("我是主子");
	}
	public void myDog() {
		dog.print();
	}
}
