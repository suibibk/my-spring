package cn.myforever;

import cn.myforever.bean.Dog;
import cn.myforever.bean.User;
import cn.myforever.core.MySpringCore;

public class AppSpring {

	public static void main(String[] args) {
		//String path = "spring.xml";
		String path = "cn.myforever.bean";
		try {
			MySpringCore app = new MySpringCore(path);
			User user = (User) app.getBean("user");
			user.myDog();
			Dog dog = (Dog) app.getBean("dog");
			dog.MyUser();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
