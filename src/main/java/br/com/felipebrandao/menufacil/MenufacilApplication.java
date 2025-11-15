package br.com.felipebrandao.menufacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MenufacilApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenufacilApplication.class, args);
    }

}
