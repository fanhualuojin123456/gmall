package com.ping.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.ping.gmall.user.mapper")
@ComponentScan("com.ping.gmall")
public class GamallUsermanageApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamallUsermanageApplication.class, args);
	}

}
